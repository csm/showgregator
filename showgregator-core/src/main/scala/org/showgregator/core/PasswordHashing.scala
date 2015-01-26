package org.showgregator.core

import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

case class HashedPassword(alg: String, salt: Array[Byte], iterations: Int, hash: Array[Byte])

object PasswordHashing {
  val Random = new SecureRandom()

  def apply(password: Array[Char], iterations: Int = 102400,
            salt: Option[Array[Byte]] = None,
            alg: String = "PBKDF2WithHmacSHA1",
            random: SecureRandom = Random): HashedPassword = {
    val _salt = salt match {
      case Some(a) => a
      case None => random.generateSeed(8)
    }
    val spec = new PBEKeySpec(password, _salt, iterations, 256)
    val skf = SecretKeyFactory.getInstance(alg)
    val key = skf.generateSecret(spec)
    HashedPassword(alg, _salt, iterations, key.getEncoded)
  }
}
