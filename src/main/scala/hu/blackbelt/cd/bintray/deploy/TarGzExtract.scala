package hu.blackbelt.cd.bintray.deploy

import java.io.{BufferedInputStream, File, FileReader, InputStream}
import java.util.UUID

import org.apache.commons.compress.archivers.{ArchiveEntry, ArchiveInputStream, ArchiveStreamFactory}
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.rauschig.jarchivelib.IOUtils

case class Art(groupId: String, artifactId: String, version: String, artifact: File, pomFile: File)

object TarGzExtract {
  private def extract(archive: InputStream): String = {
    val destDir = sys.props.get("java.io.tmpdir").getOrElse("/tmp") + File.separator + UUID.randomUUID().toString
    val compressorInput = new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(archive))


    var archiveInput: ArchiveInputStream = null
    try {
      archiveInput = new ArchiveStreamFactory().createArchiveInputStream("tar", compressorInput)
      var entry: ArchiveEntry = null
      while ((({
        entry = archiveInput.getNextEntry;
        entry
      })) != null) {
        val file: File = new File(destDir, entry.getName)
        if (entry.isDirectory) {
          file.mkdirs
        }
        else {
          file.getParentFile.mkdirs
          IOUtils.copy(archiveInput, file)
        }
      }
    } finally {
      IOUtils.closeQuietly(archiveInput)
    }


    destDir
  }

  private def list(dir: File, filter: File => Boolean = _ => true): Seq[File] = {
    Option(dir.listFiles).map { files =>
      files.filter(f => f.isFile && filter(f)).toSeq ++ files.filter(_.isDirectory).flatMap(list(_, filter))
    }.getOrElse(Seq())
  }

  private def isPomFile(file: File) = file.getName.endsWith(".pom")

  private def listPomDirs(dir: String) = list(new File(dir), isPomFile).map(_.getParent)

  private def getJarByPomFromDir(dir: String) = {
    val files = new File(dir).listFiles()
    val poms = files.filter(f => f.isFile && isPomFile(f))
    poms.map { pomFile =>

      val model = new MavenXpp3Reader().read(new FileReader(pomFile))
      model.setPomFile(pomFile)

      val groupId = Option(model.getGroupId).getOrElse(model.getParent.getGroupId)
      val artifactId = model.getArtifactId
      val version = Option(model.getVersion).getOrElse(model.getParent.getVersion)

      val packaging = model.getPackaging match {
        case "bundle" => "jar"
        case "karaf-assembly" => "zip"
        case anythingElse => anythingElse
      }
      files.find(f => f.getName.endsWith(s".$packaging") && !f.getName.endsWith(s"-sources.$packaging")).map(
        Art(groupId, artifactId, version, _, pomFile)
      )

    }.filter(_.isDefined).map(_.get).headOption
  }


  def getArtifacts(archive: InputStream) = TarGzExtract.listPomDirs(TarGzExtract.extract(archive)).map(TarGzExtract.getJarByPomFromDir).filter(_.isDefined).map(_.get)
}
