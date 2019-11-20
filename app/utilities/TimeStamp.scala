package utilities

import org.joda.time._
import org.mongodb.scala.bson._

object TimeStamp {

  def UTC : DateTime = {
    // Get Joda DateTime in UTC time zone
    DateTime.now(DateTimeZone.UTC)
  }

  def convert(datetime: BsonValue) : DateTime = {
    // Convert MongoDB Bson date time to Joda
    new DateTime(datetime.asDateTime.getValue).withZone(DateTimeZone.UTC)
  }
}
