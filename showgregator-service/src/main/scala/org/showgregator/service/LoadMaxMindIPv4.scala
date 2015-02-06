package org.showgregator.service

import com.datastax.driver.core.Cluster
import com.twitter.util.Await
import org.showgregator.core.MaxMindCSVReader
import org.showgregator.service.model.{IPV4GeoMapRecord, IPV4GeoMap}

object LoadMaxMindIPv4 extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  def run(path: String, keySpace: String = "maxmind") = {
    val cluster = Cluster.builder()
      .addContactPoint("127.0.0.1")
      .build()
    implicit val session = try {
      cluster.connect(keySpace)
    } catch {
      case _:Throwable => {
        cluster.connect().execute(s"CREATE KEYSPACE $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}")
        cluster.connect(keySpace)
      }
    }

    Await.result(IPV4GeoMapRecord.create.execute())

    val entries = new MaxMindCSVReader().readCSV(path)
    entries.foreach(e => {

      IPV4GeoMapRecord.insertMap(IPV4GeoMap(e.block.base, e.block.netmask, e.geonameId,
        e.registeredCountryGeonameId, e.representedCountryGeonameId, e.isAnonymousProxy, e.isSatelliteProvider,
        e.postalCode, e.latitude, e.longitude))
    })
  }

  override def main(args: Array[String]): Unit = {
    run(args(0))
    System.exit(0)
  }
}
