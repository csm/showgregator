package org.showgregator.service.model

object CalendarPermissions {
  val Read        = 1 << 0
  val AddEvent    = 1 << 1
  val DeleteEvent = 1 << 2
  val Share       = 1 << 3
  val Admin       = 1 << 4

  // Leave the top 2 bytes ignored for now, so we can use them for other purposes in the future.
  val AllPermission:Int = 0xffff
}

object EventPermissions {
  val Read          = 1 << 0
  val Edit          = 1 << 1
  val AddToCalendar = 1 << 2
  val Comment       = 1 << 3

  val AllPermission:Int = 0xffff
}
