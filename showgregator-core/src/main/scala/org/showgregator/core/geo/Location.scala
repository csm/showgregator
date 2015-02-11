package org.showgregator.core.geo

import java.io.File
import java.net.InetAddress

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.DatabaseReader.Builder
import com.spatial4j.core.context.SpatialContext
import com.spatial4j.core.context.jts.JtsSpatialContext
import com.spatial4j.core.distance.DistanceUtils
import org.apache.lucene.index.{Term, DirectoryReader}
import org.apache.lucene.search._
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree
import org.apache.lucene.spatial.query.{SpatialOperation, SpatialArgs}
import org.apache.lucene.store.NIOFSDirectory
import org.showgregator.core.geo.USLocales._

import scala.concurrent._
import scala.util.{Failure, Success, Try}

object Location {
  import scala.concurrent.ExecutionContext.Implicits.global

  println(getClass.getResource("/maxmind/GeoLite2-City.mmdb"))
  val geoLiteDb:DatabaseReader = try {
    new Builder(getClass.getResourceAsStream("/maxmind/GeoLite2-City.mmdb")).build()
  } catch {
    case t:Throwable => {
      t.printStackTrace()
      null
    }
  }

  val cityDirectory = new NIOFSDirectory(new File("/Users/cmarshall/Source/showgregator/city-index"))
  val cityReader = DirectoryReader.open(cityDirectory)
  val citySearcher = new IndexSearcher(cityReader)

  def findByAddressString(addr: String): Future[Option[City]] = {
    findByAddress(InetAddress.getByName(addr))
  }

  def findByAddress(addr: InetAddress): Future[Option[City]] = {
    future {
      try {
        Option(geoLiteDb.city(addr)).flatMap(mmCity => {
          val query = new BooleanQuery()
          query.add(new TermQuery(new Term("city", mmCity.getCity.getName)), BooleanClause.Occur.MUST)
          query.add(new TermQuery(new Term("state_abbrev", mmCity.getMostSpecificSubdivision.getIsoCode)), BooleanClause.Occur.MUST)
          val results = citySearcher.search(query, 1)
          if (results.scoreDocs.isEmpty)
            None
          else {
            val doc = citySearcher.doc(results.scoreDocs(0).doc)
            States.forAbbrev(doc.getField("state_abbrev").stringValue())
              .map(st => City(doc.getField("city").stringValue(), County(doc.getField("county").stringValue(), st)))
          }
        })
      } catch {
        case t:Throwable => {
          print(s"exception: $t")
          t.printStackTrace()
          None
        }
      }
    }
  }

  val ctx = JtsSpatialContext.GEO
  val grid = new GeohashPrefixTree(ctx, 11)
  val strategy = new RecursivePrefixTreeStrategy(grid, "countyShape")
  val strategy2 = new RecursivePrefixTreeStrategy(grid, "location")
  val directory = new NIOFSDirectory(new File("/Users/cmarshall/Source/showgregator/index"))
  val indexReader = DirectoryReader.open(directory)
  val indexSearcher = new IndexSearcher(indexReader)

  // Here we have a trade-off: we can either find the city whose "primary" location is nearest the point
  // in question, OR we can find out (pretty accurately) what county that point is in. Measuring
  // distance doesn't work that great for very large metro areas, because a point on the outskirts of
  // a large city might be closer to the center of a nearby suburb instead of the city center. However,
  // keeping a database of all county shapes (let alone city shapes) is a couple of orders of magnitude
  // larger.
  //
  // Generally, I think we will prefer to find the "nearest city center". This works for us because most
  // of the time all we care about is figuring out the correct time zone for someone, which is most cases
  // is fine at the state level. And also, we can allow the user to override the city they are in.

  def findByGeolocationStrings(latStr: String, lonStr: String): Future[Option[City]] = {
    Try((latStr.toDouble, lonStr.toDouble)) match {
      case Success((lat, lon)) => findByGeolocation2(lat, lon)
      case _ => None
    }
  }

  def findByGeolocation2(lat: Double, lon: Double): Future[Option[City]] = {
    future {
      val point = ctx.makePoint(lon, lat)
      println(s"searching for $point")
      val args = new SpatialArgs(SpatialOperation.Intersects,
        ctx.makeCircle(point, DistanceUtils.dist2Degrees(30, DistanceUtils.EARTH_MEAN_RADIUS_MI)))
      val filter = strategy2.makeFilter(args)
      val valueSource = strategy2.makeDistanceValueSource(point, DistanceUtils.DEG_TO_KM)
      val sort = new Sort(valueSource.getSortField(false)).rewrite(citySearcher)
      val docs = citySearcher.search(new MatchAllDocsQuery, filter, 10, sort)
      if (docs.scoreDocs.isEmpty)
        None
      else {
        val doc = citySearcher.doc(docs.scoreDocs(0).doc)
        States.forAbbrev(doc.getField("state_abbrev").stringValue())
          .map(st => City(doc.getField("city").stringValue(), County(doc.getField("county").stringValue(), st)))
      }
    }
  }

  def findByGeolocation(lat: Double, lon: Double): Future[Option[County]] = {
    future {
      val point = ctx.makePoint(lon, lat)
      val args = new SpatialArgs(SpatialOperation.Contains, point)
      val valueSource = strategy.makeDistanceValueSource(point, DistanceUtils.DEG_TO_KM)
      val sort = new Sort(valueSource.getSortField(false)).rewrite(indexSearcher)
      val filter = strategy.makeFilter(args)
      val docs = indexSearcher.search(new MatchAllDocsQuery, filter, 1, sort)
      if (docs.scoreDocs.isEmpty)
        None
      else {
        val doc = indexSearcher.doc(docs.scoreDocs(0).doc)
        println(s"doc: ${doc.get("state")}, ${doc.get("county")}")
        States.forAbbrev(doc.get("state")).map(s => County(doc.get("county"), s))
      }
    }
  }
}
