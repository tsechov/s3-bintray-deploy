package hu.blackbelt.cd.bintray.deploy

import java.io.InputStream
import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging

case class StorageLocation(bucket: String, key: String) {
  override def toString = s"s3://$bucket/$key"
}

case class Project(location: StorageLocation, name: String, version: String)

class Deploy(project: Project) extends LazyLogging {

  logger.info("collecting access properties")
  Access.collect
  logger.info("access info in possession")


  def fetch = S3Get.get(project.location.bucket, project.location.key) _

  private def selectArt(art: Art)(selector: Art => Path) = {
    val subject = selector(art)
    val key = s"${art.groupId.replace('.', '/')}/${art.artifactId}/${art.version}/${subject.getFileName}"
    (key, subject)
  }

  def upload(archive: InputStream, batchSize: Int = 30) = {
    val artifacts = TarGzExtract.getArtifacts(archive)
    val batches = artifacts.sliding(batchSize, batchSize).map(arts => {
      val mapped = arts.flatMap { art =>
        val select = selectArt(art) _
        List(select(_.artifact), select(_.pomFile))
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
