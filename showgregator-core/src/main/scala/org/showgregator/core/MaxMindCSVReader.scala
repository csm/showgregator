package org.showgregator.core

import scala.io.Source

case class NetBlock(string: String) {
  private val parts = string.split("/")
  val base:Int = parts(0).split("\\.").map(_.toInt).foldLeft(0)((a, b) => (a << 8) | b)
  val hostmask:Int = Range(0, 32 - parts(1).toInt).map(1 << _).foldLeft(0)((a, b) => a | b)
  val netmask:Int = ~hostmask
  val bitmask = parts(1).toInt
  val broadcast = (base & netmask) | hostmask

  def contains(addr: Int): Boolean = {
    base == (addr & netmask)
  }
}

case class Entry(block: NetBlock, geonameId: Option[Long],
                 registeredCountryGeonameId: Option[Long],
                 representedCountryGeonameId: Option[Long],
                 isAnonymousProxy: Boolean,
                 isSatelliteProvider: Boolean,
                 postalCode: Option[String],
                 latitude: Option[Double],
                 longitude: Option[Double])

class MaxMindCSVReader {
  def parseLong(value: String): Option[Long] = {
    if (value.trim.isEmpty) None
    else Some(value.toLong)
  }

  def parseBool(value: String): Boolean = {
    value.trim.equals("1")
  }

  def parseDouble(value: String): Option[Double] = {
    if (value.trim.isEmpty) None
    else Some(value.toDouble)
  }

  def nonempty(s: String): Option[String] = {
    if (s.trim.isEmpty) None
    else Some(s.trim)
  }

  def readCSV(file: String): Iterator[Entry] = {
    val input = Source.fromFile(file)
    val lines = input.getLines()
    lines.next()
    lines.map(_.split(",")).map(
      parts => try {
        Entry(NetBlock(parts(0)),
        parseLong(parts(1)), parseLong(parts(2)), parseLong(parts(3)),
        parseBool(parts(4)), parseBool(parts(5)),
        if (parts.length >= 7) nonempty(parts(6)) else None,
        if (parts.length >= 8) parseDouble(parts(7)) else None,
        if (parts.length >= 9) parseDouble(parts(8)) else None)
    } catch {
      case t:Throwable => {
        println("failed on: " + parts.mkString(", "))
        throw t
      }
    })
  }
}
