package org.showgregator.core.shaded.uk.gov.hmrc.emailaddress

trait ObfuscatedEmailAddress {
  val value: String
  override def toString: String = value
}

object ObfuscatedEmailAddress {
  final private val shortMailbox = "(.{1,2})".r
  final private val longMailbox = "(.)(.*)(.)".r

  import EmailAddress.validEmail

  implicit def obfuscatedEmailToString(e: ObfuscatedEmailAddress): String = e.value

  def apply(plainEmailAddress: String): ObfuscatedEmailAddress = new ObfuscatedEmailAddress {
    val value = plainEmailAddress match {
      case validEmail(shortMailbox(m), domain) =>
        s"${obscure(m)}@$domain"

      case validEmail(longMailbox(firstLetter,middle,lastLetter), domain) =>
        s"$firstLetter${obscure(middle)}$lastLetter@$domain"

      case invalidEmail =>
        throw new IllegalArgumentException(s"Cannot obfuscate invalid email address '$invalidEmail'")
    }
  }

  private def obscure(text: String) = "*" * text.length
}
