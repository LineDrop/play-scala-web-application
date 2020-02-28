package models

import com.typesafe.config.ConfigFactory
import play.api.Logging
import org.mongodb.scala.bson._
import org.joda.time._
import utilities._

case class Key(email:String, token: String, created: DateTime)

object KeyOperations extends Logging {

  private val collection = "keys"
  private val log = Log.get

  // Life span of a key, if the key can expire
  private val duration = ConfigFactory.load.getInt("key.duration")

  def create (user: User, expires: Boolean) : Key = {

    // Keys that expire are automatically deleted after configured duration.

    log.debug(s"Creating key for ${user.email}. Expires: $expires")

    // Create a key
    val key = Key(user.email, Random.alphanumeric, TimeStamp.UTC)

    // Compose Bson document
    val document = Document(
      "email" -> BsonString(key.email),
      "token" -> BsonString(key.token),
      "created" -> BsonDateTime(key.created.toDate),
      "expires" -> { if (expires) BsonDateTime(TimeStamp.UTC.plusHours(duration).toDate) else BsonNull() }
    )

    log.trace(s"New key document: $document")

    // Start database insert operation thread
    Database.insert(document,collection)

    // Return key
    key
  }

  def delete (user: User) = {
    log.debug(s"Deleting key for ${user.email}")
    // Start database delete operation thread
    Database.delete("email",user.email,collection)
  }

  def validate(token: String): Option[Key] = {

    log.debug(s"Validating key by token ${token}")

    // Find key by token

    Database.find("token", token, collection) match {
      case Some(document) => {
        val email = document("email").asString.getValue
        val token = document("token").asString.getValue
        val created = TimeStamp.convert(document("created"))

        // Return valid session with user email
        Some(Key(email,token,created))
      }
      case None => None
    }

  }

  def expire : Unit = {
    log.debug(s"Deleting keys with expiration date before ${TimeStamp.UTC.toDate}")

    // Start database delete_before operation thread
    Database.delete_before(
      "expires",
      BsonDateTime(TimeStamp.UTC.toDate)
      ,collection)
  }
}
