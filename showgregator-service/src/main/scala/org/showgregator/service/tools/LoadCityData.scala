package org.showgregator.service.tools

import java.io.{FilenameFilter, File}

import com.datastax.driver.core.Cluster
import com.twitter.util.Await
import org.showgregator.service.model.{County, City, CountyRecord, CityRecord}

import scala.io.Source
import scala.util.parsing.json.{JSONObject, JSONArray, JSON}

/**
 * Created by cmarshall on 2/5/15.
 */
object LoadCityData extends App{
  def run(path: String, keySpace: String = "citydata") = {
    val cluster = Cluster.builder()
      .addContactPoint("127.0.0.1")
      .build()
    implicit val session = try {
      cluster.connect(keySpace)
    } catch {
      case _: Throwable => {
        cluster.connect().execute(s"CREATE KEYSPACE $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}")
        cluster.connect(keySpace)
      }
    }

    Await.result(CityRecord.create.execute())
    Await.result(CountyRecord.create.execute())

    val dir = new File(path)
    println(s"reading dir $dir, exists? ${dir.exists()} isdir? ${dir.isDirectory}")
    dir.list(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name.endsWith(".json")
    }).foreach(file => {
      println(s"reading $file...")
      JSON.parseRaw(Source.fromFile(new File(dir, file)).mkString) match {
        case Some(a:JSONArray) => a.list.foreach {
          case obj:JSONObject => {
            val county = County(obj.obj.get("county_name").get.asInstanceOf[String],
              obj.obj.get("state_abbreviation").get.asInstanceOf[String], "US")
            val city = City(obj.obj.get("name").get.asInstanceOf[String], county.name, county.state, county.country,
              obj.obj.get("primary_latitude").flatMap {
                case s:String => Some(s.toDouble)
                case d:Double => Some(d)
                case _ => None
              }, obj.obj.get("primary_longitude").flatMap {
                case s:String => Some(s.toDouble)
                case d:Double => Some(d)
                case _ => None
              })
            Await.result(CountyRecord.insertCounty(county))
            Await.result(CityRecord.insertCity(city))
          }

          case _ => println("unknown value read")
        }

        case _ => println(s"couldn't parse $file")
      }
    })
  }

  override def main(args: Array[String]): Unit = {
    run(args(0))
    System.exit(0)
  }
}
