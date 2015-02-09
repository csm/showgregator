package org.showgregator.core.geo

import java.io.File
import java.net.InetAddress

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.DatabaseReader.Builder
import com.spatial4j.core.context.jts.JtsSpatialContext
import com.spatial4j.core.distance.DistanceUtils
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.{MatchAllDocsQuery, Sort, IndexSearcher}
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
  private val geoLiteDb:DatabaseReader = null // new Builder(getClass.getResourceAsStream("maxmind/GeoLite2-City.mmdb")).build()

  def findByAddress(addr: InetAddress): Future[Option[City]] = {
    try {
      Future(Try(geoLiteDb.city(addr))).map {
        case Success(city) => USLocales.Cities.countyFor(city.getCity.getName, city.getMostSpecificSubdivision.getName).map(c =>
          City(city.getCity.getName,
            County(c.name, States.forAbbrev(city.getMostSpecificSubdivision.getIsoCode).get)))
        case Failure(t) =>
          None
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
        States.forAbbrev(doc.get("state")).map(s => County(doc.get("county"), s))
      }
    }
  }
}
