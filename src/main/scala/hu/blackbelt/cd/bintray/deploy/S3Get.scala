package hu.blackbelt.cd.bintray.deploy

import java.io.InputStream

import awscala.s3._
import com.amazonaws.services.s3.model.GetObjectRequest

object S3Get {

  def get(bucketName: String, key: String)(handler: (InputStream) => Unit) = {
    implicit val s3 = S3()

    val obj = s3.getObject(new GetObjectRequest(bucketName, key))
    handler(obj.getObjectContent)


  }
}
