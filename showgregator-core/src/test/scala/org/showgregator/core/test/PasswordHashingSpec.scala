package org.showgregator.core.test

import org.scalatest.{Matchers, FlatSpec}
import org.showgregator.core.PasswordHashing

class PasswordHashingSpec extends FlatSpec with Matchers {
  "generate password hash" should "complete without exception" in {
    val password = "password".toCharArray
    val salt = "salt".getBytes("UTF-8")
    val hash = PasswordHashing(password, 102400, Some(salt))
    println("alg: %s, iterations: %d, salt: %s, hash: %s".format(hash.alg,
      hash.iterations, hash.salt.map(b => "%02x".format(b & 0xff)).mkString,
      hash.hash.map(b => "%02x".format(b & 0xff)).mkString))
  }
}
