package utilities

import com.sendgrid._
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object SendGrid {

  private val log = Log.get
  private val sender = ConfigFactory.load.getString("sendgrid.sender")
  private val key = ConfigFactory.load.getString("sendgrid.key")

  def send (recipient: String, html_message: String, subject: String) = {

    log.debug("Starting email thread")

    // Start a thread to avoid waiting the email process
    sending_thread(recipient, html_message, subject).onComplete {
      case Success(value) => log.trace("Email message successfully sent")
      case Failure(exception) => log.error(s"Error sending email message: ${exception.getMessage}")
    }
  }

  def sending_thread (recipient: String, html_message: String, subject: String)  = Future {

    val from = new Email(sender)
    val to = new Email(recipient)
    val content = new Content("text/html", html_message)
    val mail = new Mail(from, subject, to, content)

    val sg = new SendGrid(key)
    val request = new Request
    request.setMethod(Method.POST)
    request.setEndpoint("mail/send")
    request.setBody(mail.build)
    val response = sg.api(request)

    log.trace(s"SendGrid response ${response.getStatusCode}")

  }
}
