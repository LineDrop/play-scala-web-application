package utilities

import java.util.Properties

import javax.mail.{Authenticator, Message, PasswordAuthentication, Session, Transport}
import javax.mail.internet.{InternetAddress, MimeMessage}
import play.api.Logging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure,Success}

import com.typesafe.config.ConfigFactory

object Mailer extends Logging {

  val log = Log.get

  val host = ConfigFactory.load.getString("mailer.host")
  val port = ConfigFactory.load.getString("mailer.port")
  val username = ConfigFactory.load.getString("mailer.username")
  val password = ConfigFactory.load.getString("mailer.password")

  def send(recipient: String, html_message: String, subject: String) = {

    log.debug("Starting email thread")

    // Start a thread to avoid waiting the email process
    sending_thread(recipient, html_message, subject).onComplete {
      case Success(value) => log.trace("Email message successfully sent")
      case Failure(exception) => log.error(s"Error sending email message: ${exception.getMessage}")
    }

  }

  private def sending_thread(recipient: String, html_message: String, subject: String) = Future {

    // Sending email thread
    log.trace("Setting email properties")

    val properties = new Properties
    properties.put("mail.smtp.host", host)
    properties.put("mail.smtp.port", port)
    properties.put("mail.smtp.ssl.enable","true")
    properties.put("mail.smtp.auth", "true")

    log.trace("Configuring session and message")

    val session = Session.getDefaultInstance(properties, new Authenticator {
      protected override def getPasswordAuthentication() = new PasswordAuthentication(username,password)
    })

    val message = new MimeMessage(session)
    message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient))
    message.setSubject(subject)

    log.trace(s"html_message: $html_message")

    message.setContent(html_message,"text/html")

    Transport.send(message)

  }

}
