package hu.blackbelt.cd.bintray.deploy

import java.io.{InputStream, File}
import java.nio.file.{Paths, Files}

import com.typesafe.scalalogging.LazyLogging

case class StorageLocation(bucket: String, key: String){
  override def toString = s"$bucket::$key"
}

case class Project(location: StorageLocation, name: String, version: String)

class Deploy(project: Project) extends LazyLogging {

  logger.info("collecting access properties")
  Access.collect
  logger.info("access info in possession")


  def fetch = S3Get.get(project.location.bucket, project.location.key)_

  def upload(archive: InputStream, batchSize: Int = 30) = {
    val artifacts = TarGzExtract.getArtifacts(archive)
    val batches = artifacts.sliding(batchSize, batchSize).map(arts => {
      val mapped = arts.map { art =>
        val key = s"${art.groupId.replace('.', '/')}/${art.artifactId}/${art.version}/${art.artifact.getFileName}"
        (key, art.artifact)
      }
      Batch(mapped)
    }

    ).toList

    val b = new Btray
    val ver = b.version("releases", project.name, project.version)

    ver.map(
      b.uploadTo(_, batches)
    )
  }
}
