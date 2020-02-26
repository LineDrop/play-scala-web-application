package models

import com.typesafe.config.ConfigFactory
import play.api.{Logger, Logging}
import org.mongodb.scala.bson._
import org.joda.time._

import scala.collection.mutable
import utilities._

case class Subscriber(email: String, ip: String, created: DateTime)
case class SubscriberForm(email: String)

object SubscriberOperations extends Logging {
  val collection = "subscribers"
  val log = Log.get

  def create(subscriber: Subscriber): Unit = {
    log.debug(s"Creating a new subscriber $subscriber")

    // Compose Bson document
    val document = Document(
      "email" -> BsonString(subscriber.email),
      "ip" -> BsonString(subscriber.ip),
      "created" -> BsonDateTime(subscriber.created.toDate)
    )
    log.trace(s"New subscriber document: $document")

    // Start database insert operation thread
    Database.insert(document, collection)

    log.trace("Sending new subscriber notification to SA")

    val sa_email = ConfigFactory.load.getString("sa.email")
    val html_message = "<html><body><p>Greetings! You have a new subscriber.</p></body></html>"
    utilities.SendGrid.send(sa_email, html_message, "new subscriber")

  }

  def read: Map[String, Subscriber] = {

    log.debug(s"Getting subscribers")

    // Get subscribers from database
    val subscriber_document_seq = Database.find_all(collection)
    log.trace(s"Found subscribers: $subscriber_document_seq")

    // Set up an empty map to collect results
    val return_map: mutable.Map[String, Subscriber] = mutable.Map.empty

    // Iterate through results and populate return_map
    subscriber_document_seq.foreach(subscriber_document => {
      return_map.put(
        subscriber_document("_id").asObjectId.getValue.toString,
        Subscriber(
          subscriber_document("email").asString.getValue,
          subscriber_document("ip").asString.getValue,
          TimeStamp.convert(subscriber_document("created"))
        )
      )
    })

    // Convert to immutable map and return
    return_map.toMap
  }

  def count_by_date(subscribers: Map[String, Subscriber]): List[(DateTime, Int)] = {

    // This method flexes Scala's muscles a little by introducing compound operations

    log.debug(s"Aggregating subscriber count by date")

    // Set up an empty map to collect
    val aggregate_map: mutable.Map[DateTime, Int] = mutable.Map.empty

    // Make sure that the result is not empty, otherwise the reduce operation will fail
    if (subscribers.size > 0) {
      // Reduce the map by created date in ascending order,
      // maintain UTC time zone,
      // and get the day of the first subscription event
      val starting_date = subscribers.values.reduceLeft((x, y) => if (x.created.getMillis < y.created.getMillis) x else y)
        .created
        .withZoneRetainFields(DateTimeZone.UTC)
        .dayOfYear
        .roundFloorCopy

      // Get the day of today in UTC
      val current_date = DateTime.now.withZone(DateTimeZone.UTC)
        .dayOfYear
        .roundCeilingCopy

      // Calculate the number of days elapsed since the first subscription
      val days_since_starting_date = Days.daysBetween(starting_date, current_date).getDays

      log.trace(s"Subscriptions started on $starting_date")
      log.trace(s"Current date $current_date")
      log.trace(s"Days since starting date $days_since_starting_date")

      // Set up tracking variable counting_date
      var counting_date = starting_date

      // Loop through the time period between the first subscription
      // and today, day by day.
      while (counting_date.getMillis < current_date.getMillis) {

        // For each day, extract only subscribers for that day
        val subscribers_in_period = subscribers.values.filter(subscriber => {
          subscriber.created.getMillis >= counting_date.getMillis &&
            subscriber.created.getMillis < counting_date.plusDays(1).getMillis
        })

        log.trace(s"Period: $counting_date - ${counting_date.plusDays(1)}")
        log.trace(s"Subscribers in period: ${subscribers_in_period.size}")
        log.trace(s"Subscribers in period: $subscribers_in_period")

        // Add to the aggregate map: date -> subscribers
        aggregate_map.put(counting_date, subscribers_in_period.size)

        // Increment the counting_date variable by one day
        counting_date = counting_date.plusDays(1)

      }

    }

    // Order by time (ascending)
    aggregate_map.toList.sortBy(_._1.getMillis)
  }

}
