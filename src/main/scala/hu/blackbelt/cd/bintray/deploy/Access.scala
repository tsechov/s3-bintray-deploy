package hu.blackbelt.cd.bintray.deploy

import java.io.{File, FileInputStream}
import java.nio.file.{StandardCopyOption, Path, Files}
import java.util.{UUID, Properties}

import awscala.s3.S3
import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.s3.model.{GetObjectRequest}

object Access {
  val bintray_organization = "bintray.organization"
  val bintray_user = "bintray.user"
  val bintray_apikey = "bintray.apikey"
  val aws_accessKeyId = "aws.accessKeyId"
  val aws_secretKey="aws.secretKey"


  def collect = {
    implicit val s3 = S3()(com.amazonaws.regions.Region.getRegion(Regions.EU_CENTRAL_1))



    val destination = FS.getPath(s"/tmp/${UUID.randomUUID().toString}")
    Files.createDirectories(destination)
    val s3Object = s3.getObject(new GetObjectRequest("blackbelt-secrets", "bintray-deploy/access.properties"))
    Files.copy(s3Object.getObjectContent, destination, StandardCopyOption.REPLACE_EXISTING)

    import scala.collection.JavaConverters._
    val prop = new Properties()
    prop.load(Files.newInputStream(destination))
    prop.entrySet().asScala.foreach {
      (entry) => {
        sys.props += ((entry.getKey.asInstanceOf[String], entry.getValue.asInstanceOf[String]))
      }
    }
  }
}
