package org.showgregator.core.tools

import java.io.File

import com.spatial4j.core.context.jts.JtsSpatialContext
import org.apache.lucene.document.{DoubleField, Field, StringField, Document}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.showgregator.core.geo.USLocales.Cities

object IndexCities extends App {
  val directory = new NIOFSDirectory(new File(args(1)))
  val iwConfig = new IndexWriterConfig(Version.LATEST, null)
  val iw = new IndexWriter(directory, iwConfig)
  val ctx = JtsSpatialContext.GEO
  val grid = new GeohashPrefixTree(ctx, 11)
  val strategy = new RecursivePrefixTreeStrategy(grid, "location")

  try {
    Cities.loadCityData(new File(args(0))).foreach(city => {
      println(s"Adding city $city")
      val doc = new Document
      doc.add(new StringField("city", city.name, Field.Store.YES))
      doc.add(new StringField("county", city.county.name, Field.Store.YES))
      doc.add(new StringField("state", city.county.state.name, Field.Store.YES))
      doc.add(new StringField("state_abbrev", city.county.state.abbrev, Field.Store.YES))
      if (city.latitude.isDefined && city.longitude.isDefined) {
        val point = ctx.makePoint(city.longitude.get, city.latitude.get)
        doc.add(new DoubleField("longitude", city.longitude.get, Field.Store.YES))
        doc.add(new DoubleField("latitude", city.latitude.get, Field.Store.YES))
        println(s"location: $point")
        strategy.createIndexableFields(point).foreach(pt => {
          println(s"adding point: $pt")
          doc.add(pt)
        })
      }
      iw.addDocument(doc)
    })
  } finally {
    iw.commit()
    iw.close()
    directory.close()
  }
}
