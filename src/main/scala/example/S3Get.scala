package example

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import awscala.s3._

object S3Get {

  def get(bucketName: String, key: String): (Option[File], String) = {
    implicit val s3 = S3()
    val out = for {
      bucket <- s3.bucket(bucketName)
      s3Object <- bucket.getObject(key)
    } yield {
      val outFile = File.createTempFile(key.replace('/', '+'), ".tmp");
      Files.copy(s3Object.getObjectContent, outFile.toPath, REPLACE_EXISTING)
      outFile
    }
    (out, key)

  }
}
