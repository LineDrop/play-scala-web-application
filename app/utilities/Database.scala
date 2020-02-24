package utilities

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory
import scala.concurrent.{Await, Promise}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import com.typesafe.config.ConfigFactory
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import play.api.Logging

import scala.concurrent.duration.Duration

object Database extends Logging {

  // Each database operation opens and closes client connection
  // to avoid leaving connections open which will cause memory leaks

  // Set MongoDB driver logging to ERROR
  LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].getLogger("org.mongodb.driver").setLevel(Level.ERROR)

  // Read client and database from configuration
  private val config_client = ConfigFactory.load.getString("mongodb.server")
  private val config_database = ConfigFactory.load.getString("mongodb.database")

  private val log = Log.get

  def insert (document: Document, collection_name: String): Unit = {

    // Start the operation and do not wait for the result
    log.debug(s"Starting database insert operation thread")

    // Set up new client connection, database, and collection
    val _client: MongoClient = MongoClient(config_client)
    val _database: MongoDatabase = _client.getDatabase(config_database)
    val collection: MongoCollection[Document] = _database.getCollection(collection_name)

    // Start insert operation thread;
    // once the thread has finished, Observer reports the result
    collection.insertOne(document).subscribe(new Observer[Completed] {
      override def onNext(result: Completed): Unit = log.trace("Document inserted into database.")
      override def onError(e: Throwable): Unit = {
        _client.close // close client connection to avoid memory leaks
        log.error("Error inserting document into database")
      }
      override def onComplete(): Unit = {
        _client.close // close client connection to avoid memory leaks
        log.trace("Document insertion job completed.")
      }
    })
  }

  def find (search_key: String, search_value: String, collection_name: String): Option[Document] = {

    // The application will need to wait for the find operation thread to complete
    // in order to process the returned value.

    log.debug(s"Starting database find operation thread")

    // Setup new client connection, database, and collection
    val _client: MongoClient = MongoClient(config_client)
    val _database: MongoDatabase = _client.getDatabase(config_database)
    val collection: MongoCollection[Document] = _database.getCollection(collection_name)

    // Set up result sequence
    var result_seq : Seq[Document] = Seq.empty

    // Set up Promise container to wait for the database operation to complete
    val promise = Promise[Boolean]

    // Start insert operation thread; once the thread has finished, read resulting documents.
    // Essentially, the query returns the latest document in the collection.
    // The database query requests the results to be sorted by 'created' field in
    // descending order; the top document is selected.

    collection.find(equal(search_key, search_value)).sort(descending("created")).first()
      .collect().subscribe((results: Seq[Document]) => {
      log.trace(s"Found in collection: $results")

      // Append found documents to the results
      result_seq = result_seq ++ results

      log.trace(s" Result sequence: $result_seq")

      promise.success(true) // set Promise container
      _client.close // close client connection to avoid memory leaks
    })

    val future = promise.future // Promise completion result
    Await.result(future, Duration.Inf)  // wait for the promise completion result

    // Thread completed
    // return option of the first result
    // Some[Document] or None if the result is null
    result_seq.headOption

  }

  def find_all (collection_name: String): Seq[Document] = {

    // The application will need to wait for the find operation thread to complete
    // in order to process the returned value.

    log.debug(s"Starting database find_all operation thread")

    // Set up new client connection, database, and collection
    val _client: MongoClient = MongoClient(config_client)
    val _database: MongoDatabase = _client.getDatabase(config_database)
    val collection: MongoCollection[Document] = _database.getCollection(collection_name)

    // Set up result sequence
    var result_seq : Seq[Document] = Seq.empty

    // Set up Promise container to wait for the database operation to complete
    val promise = Promise[Boolean]

    // Start insert operation thread; once the thread has finished, read resulting documents.
    collection.find().collect().subscribe((results: Seq[Document]) => {
      log.trace(s"Found operation thread completed")
      // Append found documents to the results
      result_seq = result_seq ++ results
      log.trace(s" Result sequence: $result_seq")

      promise.success(true) // set Promise container
      _client.close // close client connection to avoid memory leaks
     })


    val future = promise.future // Promise completion result
    Await.result(future, Duration.Inf) // wait for the promise completion result

    // Return document sequence
    result_seq

  }


  def update (search_key: String, search_value: String, document: Document, collection_name: String): Unit = {

    log.debug(s"Starting database update operation thread")

    // Start the operation and do not wait for the result

    // Set up new client connection, database, and collection
    val _client: MongoClient = MongoClient(config_client)
    val _database: MongoDatabase = _client.getDatabase(config_database)
    val collection: MongoCollection[Document] = _database.getCollection(collection_name)

    // Start replaceOne operation thread to remove the original document and insert the
    // new one while keeping the document id.
    // Replace document with field 'search_key' equal to 'search_value'
    collection.replaceOne(equal(search_key,search_value),document).subscribe((result: UpdateResult) => {
      log.trace(s"Document updated with result: $result")
      _client.close // close client connection to avoid memory leaks
    })

  }

  def delete (search_key: String, search_value: String, collection_name: String): Unit = {

    log.debug(s"Starting database delete operation thread")

    // Start the operation and do not wait for the result

    // Set up new client connection, database, and collection
    val _client: MongoClient = MongoClient(config_client)
    val _database: MongoDatabase = _client.getDatabase(config_database)
    val collection: MongoCollection[Document] = _database.getCollection(collection_name)

    // Start deleteOne operation thread
    // Delete documents with field 'search_key' equal to 'search_value'
    collection.deleteOne(equal(search_key,search_value)).subscribe((result: DeleteResult) => {
      log.trace(s"Document delete result: $result")
      _client.close // close client connection to avoid memory leaks
    })

  }

  def delete_before (search_key: String, time: BsonDateTime, collection_name: String): Unit = {

    log.debug(s"Starting database delete operation thread")

    // Set up new client connection, database, and collection
    val _client: MongoClient = MongoClient(config_client)
    val _database: MongoDatabase = _client.getDatabase(config_database)
    val collection: MongoCollection[Document] = _database.getCollection(collection_name)

    // Start deleteMany operation thread
    // Delete documents with date/time field 'search_key' less then value 'time'
    collection.deleteMany(lt(search_key,time)).subscribe((result: DeleteResult) => {
      log.trace(s"Documents deleted with result: $result")
      _client.close // close client connection to avoid memory leaks
    })

  }

}
