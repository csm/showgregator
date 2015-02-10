package org.showgregator.core.tools

import java.io.File

import com.spatial4j.core.context.jts.JtsSpatialContext
import com.vividsolutions.jts.geom.Geometry
import org.apache.lucene.document.{Field, StringField, Document}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.geotools.data.shapefile.ShapefileDataStore
import org.showgregator.core.geo.USLocales.States

/**
 * Index Hawaii, based on TIGER state outlines.
 *
 * This isn't perfect, since it will just index the entire state (I think...), but I couldn't find any shapes by county.
 * This will work for time zone resolution, but not for any real location searches, if they're aggregated "by county".
 */
object IndexHawaii extends App {
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
      if (feature.getAttribute("STATEFP").equals(States.Hawaii.fipsCode)) {
        println(s"Indexing Hawaii")
        feature.getDefaultGeometry match {
          case geo: Geometry => {
            val shape = ctx.makeShape(geo)
            val doc = new Document
            doc.add(new StringField("county", "Hawaii-All", Field.Store.YES)) // FIXME, this is a hack, but all of HI has one time zone.
            doc.add(new StringField("state", States.Hawaii.abbrev, Field.Store.YES))
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
