package org.showgregator.core

import java.nio.ByteBuffer

object ByteBuffers {
  implicit class AsByteArray(buffer: ByteBuffer) {
    def asBytes:Array[Byte] = {
      val a = new Array[Byte](buffer.remaining())
      buffer.get(a)
      a
    }
  }
}
