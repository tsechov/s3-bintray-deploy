package hu.blackbelt.cd.bintray.deploy

import java.net.URLDecoder

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._

class Main(context: Context) extends LazyLogging {
  private def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  def deploy(event: S3Event) = {


    event.getRecords.asScala.foreach(
      s3EventRecord => {
        val bucket = s3EventRecord.getS3.getBucket.getName
        val key = decodeS3Key(s3EventRecord.getS3.getObject.getKey)
        val loc = StorageLocation(bucket, key)

        logger.info(s"received event for $loc")

        val parts = key.split('/')
        require(parts.size > 1, s"cannot extract project name and version from $key")
        val project = parts(0)

        val version = parts(1)
        val deployer = new Deploy(Project(loc, project, version))
        logger.info(s"fetching $loc")
        val archive = deployer.fetch {
          is => {
            logger.info("fetched")
            logger.info("starting deployment to bintray")
            deployer.upload(is).failed.toOption.foreach(throw _)
            logger.info("deployment success")
          }
        }

      }
    )

  }
}
