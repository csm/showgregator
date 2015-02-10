package org.showgregator.core.geo

import java.io.File
import java.net.InetAddress

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.DatabaseReader.Builder
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
  val directory = new NIOFSDirectory(new File("/Users/cmarshall/Source/showgregator/index"))
  val indexReader = DirectoryReader.open(directory)
  val indexSearcher = new IndexSearcher(indexReader)

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
