package org.showgregator.core.tools

import java.io.File
import com.spatial4j.core.context.SpatialContext
import com.spatial4j.core.context.jts.JtsSpatialContext
import com.vividsolutions.jts.geom.{MultiPolygon, Geometry}
import org.apache.lucene.document.{Field, StringField, Document}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.geotools.data.shapefile.ShapefileDataStore
import org.showgregator.core.geo.USLocales.{State, States}

object IndexShapes extends App {
  val file = new File(args(0))
  val shp = new ShapefileDataStore(file.toURI.toURL)

  val directory = new NIOFSDirectory(new File(args(1)))
  val iwConfig = new IndexWriterConfig(Version.LATEST, null)
  val iw = new IndexWriter(directory, iwConfig)
  val ctx = JtsSpatialContext.GEO
  val grid = new GeohashPrefixTree(ctx, 11)
  val strategy = new RecursivePrefixTreeStrategy(grid, "countyShape")

  val typeName = shp.getTypeNames()(0)
  println(s"Reading $typeName...")
  val fs = shp.getFeatureSource(typeName)
  val fc = fs.getFeatures
  val it = fc.features

  try {
    while (it.hasNext) {
      val feature = it.next
      val name = feature.getAttribute("NAME").asInstanceOf[String]
      val state = States.forFipsCode(feature.getAttribute("STATEFP").asInstanceOf[String])
      if (state.isDefined) {
        println(s"Indexing ${feature.getAttribute("NAME")} County, ${state.get.name})")
        println(s"${feature.getDefaultGeometry.getClass}")
        feature.getDefaultGeometry match {
          case geo: Geometry => {
            val shape = ctx.makeShape(geo)
            val doc = new Document
            doc.add(new StringField("county", name, Field.Store.YES))
            doc.add(new StringField("state", state.get.abbrev, Field.Store.YES))
            strategy.createIndexableFields(shape).foreach(doc.add)
            iw.addDocument(doc)
          }
        }
      }
    }
  } finally {
    iw.commit()
    iw.close()
    directory.close()
  }
}
