package models

import play.api.Logging
import org.mongodb.scala.bson._
import org.joda.time._
import scala.util.{Failure, Success}
import scala.collection.mutable

import utilities._

case class User(email:String, name: String, role: Role.Value, hash: String, created: DateTime)
case class UserForm(name: String, email:String, role: String)
case class UserForgotForm(email:String)
case class UserHashForm(token: String, password: String)
case class UserAuthenticateForm(email: String, password: String)

object UserOperations extends Logging {
  val collection = "users"
  val log = Log.get

  def create(user: User) = {

    log.debug(s"Creating a new user $user")

    // Compose Bson document
    val document = Document(
      "email" -> BsonString(user.email),
      "name" -> BsonString(user.name),
      "role" -> BsonString(user.role.toString),
      "hash" -> new BsonNull,
      "created" -> BsonDateTime(user.created.toDate)
    )

    log.trace(s"New user document: $document")

    // Start database insert operation thread
    Database.insert(document,collection)

    // Create a password reset key
    val key = KeyOperations.create(user, false) // does not expire

    // Compose welcome email
    val message =
      s"""
         |<html><body>
         |<p>Welcome! Please follow the link below to create your password.</p>
         |<p><a href="${Server.url}/reset/${key.token}">Create Password</a></p>
         |</body></html>""".stripMargin

    // Send welcome email
    Mailer.send(user.email,message,"Welcome to LineDrop Code!")

  }

  def send_reset_link(user: User) = {

    log.debug(s"Sending password reset link")

    // Create a password reset key
    val key = KeyOperations.create(user,true) // expires after a configured number of hours

    // Compose email message
    val message =
      s"""
         |<html><body>
         |<p>Hi ${user.name},</p>
         |<p>Please follow the link below to reset your password.</p>
         |<p><a href="${Server.url}/reset/${key.token}">Reset Password</a></p>
         |</body></html>""".stripMargin

    // Send email
    Mailer.send(user.email,message,"LineDrop Code password assistance")

  }

  def hash(token: String, password: String): Option[User] = {

    log.debug(s"Hashing user's password")

    // Validate key by token
    KeyOperations.validate(token) match {
      case Some(key) => {
        // Token is valid
        log.trace(s"Hashing for user with email ${key.email}")
        // Encrypt the password
        val hash = Hash.create(password)
        log.trace(s"User's hash: $hash")

        // Get user by email
        read(key.email) match {
          case Some(user) => {
            // Found user
            log.trace("Making user copy")
            // Copy user replacing the hash with the new encrypted string
            val user_copy = user.copy(user.email, user.name, user.role,hash,user.created)
            log.trace(s"Trying to update user $user_copy")
            // Update user
            update(user_copy)
            // Delete key
            log.trace(s"Deleting key")
            KeyOperations.delete(user)
            // Return
            Some(user)

          }
          case None =>  None  // Cannot find user with this email
        }
      }
      case None => {
        // Key with this token does not exist
        log.trace(s" Invalid key")
        None
      }
    }
  }

  def authenticate(email: String, password: String): Option[User] = {

    log.debug(s"Authenticating user: $email")

    // Get user
    read(email) match {
      case Some(user) => {
        // Compare password with the hashed value
        if (Hash.validate(password, user.hash)) Some(user) else None
      }
      case None => {
        // Cannot find user with this email
        log.trace(s"User not found")
        None
      }
    }


  }

  def authenticate_from_cookie(cookie_session: Option[String]): Option[User] = {

    log.debug(s"Authenticating user from a current session")

    cookie_session match {
        // Session token is read from the cookie
      case Some(session_token) => {
        log.trace(s"User signed in")
        log.trace(s"User session: $session_token")

        // Validate session by token
        SessionOperations.validate(session_token) match {
          case Some(session) => {
            // Session is valid
            log.trace(s"User found by session $session")
            // Get user by email
            UserOperations.read(session.email)
          }
          case None => {
            // No session found with this token
            log.trace(s"Invalid session")
            None
          }
        }

      }
      case None => {
        // No session token found in the cookie
        log.trace(s"User is not sign")
        None
      }
    }

  }


  def read (email: String): Option[User] = {

    log.debug(s"Getting user by email $email")

    // Find user by email

    Database.find("email",email, collection) match {
      case Some(document) => {
        val email = document("email").asString.getValue
        val name = document("name").asString.getValue
        val role = Role.withName(document("role").asString.getValue)
        val hash = if( document("hash").isNull ) "" else document("hash").asString.getValue
        val created = TimeStamp.convert(document("created"))

        // Return user
        Some (User(email,name,role,hash,created))
      }
      case None => None
    }

  }

  def read_all : Map[String, User] = {

    log.debug(s"Reading all users")

    val document_seq = Database.find_all(collection)

    log.trace(s"All users: $document_seq")

    // Set up an empty return map
    val return_map : mutable.Map[String, User] = mutable.Map.empty

    // Translate data from each document into User object
    document_seq.foreach(_document => {
      return_map.put(
        _document("_id").asObjectId.getValue.toString,
        User(
          _document("email").asString.getValue,
          _document("name").asString.getValue,
          Role.withName(_document("role").asString.getValue),
          if( _document("hash").isNull ) "" else _document("hash").asString.getValue,
          TimeStamp.convert(_document("created"))
        )
      )
    })

    // Convert to immutable map and return
    return_map.toMap
  }

  def update (user: User) = {
    log.debug(s"Updating user $user")

    // Compose update document
    val updated_document = Document(
      "email" -> BsonString(user.email),
      "name" -> BsonString(user.name),
      "role" -> BsonString(user.role.toString),
      "hash" -> BsonString(user.hash),
      "created" -> BsonDateTime(user.created.toDate),
      "updated" -> BsonDateTime(TimeStamp.UTC.toDate)
    )

    log.trace(s"User update document: $updated_document")

    // Start database update operation thread
    Database.update("email",user.email,updated_document,collection)
  }

  def delete (user: User) = {
    log.debug(s"Deleting user $user")
    // Start database delete operation thread
    Database.delete("email",user.email,collection)
  }

}
