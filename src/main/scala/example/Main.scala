package example

import scala.collection.JavaConverters._
import java.net.URLDecoder
import com.amazonaws.services.lambda.runtime.events.S3Event

class Main {
  private def decodeS3Key(key: String): String = URLDecoder.decode(key.replace("+", " "), "utf-8")

  def getSourceBuckets(event: S3Event): java.util.List[String] = {
    val o = for {
      record <- event.getRecords.asScala.toSeq
      bucket = record.getS3.getBucket.getName
      key = decodeS3Key(record.getS3.getObject.getKey)
    } yield S3Get.get(bucket, key)


    val result = event.getRecords.asScala.map(record => decodeS3Key(record.getS3.getObject.getKey)).asJava
    println(s"results: $result")
    result
  }
}
