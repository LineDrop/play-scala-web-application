package utilities

import com.github.t3hnar.bcrypt._
import play.api.Logging
import scala.util.Try

object Hash extends Logging {

  def encrypt(value: String): String = {

    // Encrypting value with a salted hash
    val salt = generateSalt
    val hash = value.bcrypt(salt)
    // return hashed value
    hash

  }

  def validate(value: String, hash: String): Try[Boolean] ={
    // Validating if the bcrypted value is save and if
    // the value matches the hash
    value.isBcryptedSafe(hash)
  }

}
