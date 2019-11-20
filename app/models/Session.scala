package models

import play.api.Logging
import org.mongodb.scala.bson._
import org.joda.time._

import utilities._

case class Session(email:String, token: String, created: DateTime)

object SessionOperations extends Logging {

  val collection = "sessions"
  val log = Log.get

  def create (user: User) : Session = {
    log.debug(s"Creating a session for ${user.email}")
    val session = Session(user.email, Random.alphanumeric, TimeStamp.UTC)

    // Compose Bson document
    val document = Document(
      "email" -> BsonString(session.email),
      "token" -> BsonString(session.token),
      "created" -> BsonDateTime(session.created.toDate)
    )

    log.trace(s"New session document: $document")

    // Start database insert operation thread
    Database.insert(document,collection)

    // Return session
    session
  }

  def delete (created: DateTime) = {
    log.debug(s"Delete sessions before $created")
    // Start database delete operation thread
    Database.delete_before("created",BsonDateTime(created.toDate),collection)
  }

  def validate(token: String): Option[Session] = {

    log.debug(s"Validating session by token $token")

    // Find session by token
    Database.find("token", token, collection) match {
      case Some(document) => {
        val email = document("email").asString.getValue
        val token = document("token").asString.getValue
        val created = TimeStamp.convert(document("created"))

        // Return valid session with user email
        Some(Session(email,token,created))
      }
      case None => None
    }

  }
}
