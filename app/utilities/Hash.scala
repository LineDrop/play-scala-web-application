// Reference: https://github.com/t3hnar/scala-bcrypt

package utilities

import com.github.t3hnar.bcrypt._
import play.api.Logging
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object Hash extends Logging {

  private val log = Log.get

  def create(value: String): String = {

    log.debug("Encrypting a value")
    // Creating a salted hash
    val salt = generateSalt
    val hash = value.bcrypt(salt)
    // return hashed value
    hash

  }

  def validate(value: String, hash: String): Boolean = {

    // Validating the hash
    value.isBcryptedSafe(hash) match {
      case Success(result) => { // hash is valid - correct salt and number of rounds
        log.trace("Hash is safe")
        if (result) log.trace("Test hash matches stored hash") else log.trace("Test hash does not match stored hash")
        result // true if test hash matches the stored has, false if it does not
      }
      case Failure(failure) => {
        // Hash is invalid
        log.trace("Hash is not safe")
        false
      }
    }

  }

}
