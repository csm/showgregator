package org.showgregator.core.util

import scala.ref.WeakReference

object Bytes {
  def apply(bytes: Array[Byte]) = new Bytes(WeakReference(bytes))
}

class Bytes(val bytes: WeakReference[Array[Byte]]) {
  override def toString: String = {
    bytes.get match {
      case Some(a) => a.map("%02x".format(_)).mkString
      case None => "(garbage collected bytes)"
    }
  }
}
