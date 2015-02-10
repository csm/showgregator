package org.showgregator.core.tools

import java.io.File

import org.apache.lucene.document.{Field, StringField, Document}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.showgregator.core.geo.USLocales.Cities

object IndexCities extends App {
  val directory = new NIOFSDirectory(new File(args(1)))
  val iwConfig = new IndexWriterConfig(Version.LATEST, null)
  val iw = new IndexWriter(directory, iwConfig)

  try {
    Cities.loadCityData(new File(args(0))).foreach(city => {
      val doc = new Document
      doc.add(new StringField("city", city.name, Field.Store.YES))
      doc.add(new StringField("county", city.county.name, Field.Store.YES))
      doc.add(new StringField("state", city.county.state.name, Field.Store.YES))
      doc.add(new StringField("state_abbrev", city.county.state.abbrev, Field.Store.YES))
      iw.addDocument(doc)
    })
  } finally {
    iw.commit()
    iw.close()
    directory.close()
  }
}
