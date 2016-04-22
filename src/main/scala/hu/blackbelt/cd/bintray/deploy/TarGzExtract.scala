package hu.blackbelt.cd.bintray.deploy

import java.io.{BufferedInputStream, InputStream}
import java.nio.file.{Files, Path}
import java.util.UUID
import java.util.stream.Collectors

import hu.blackbelt.cd.bintray.VFS.FS
import org.apache.commons.compress.archivers.{ArchiveEntry, ArchiveInputStream, ArchiveStreamFactory}
import org.apache.commons.compress.compressors.{CompressorInputStream, CompressorStreamFactory}
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.rauschig.jarchivelib.IOUtils

import scala.annotation.tailrec
import scala.collection.JavaConverters._

case class Art(groupId: String, artifactId: String, version: String, artifact: Path, pomFile: Path)

object TarGzExtract {
  @tailrec
  private def entries(acc: Stream[ArchiveEntry], source: ArchiveInputStream): Stream[ArchiveEntry] = {
    Option(source.getNextEntry) match {
      case None => acc
      case Some(e) => entries(e #:: acc, source)
    }

  }

  def extract(archive: InputStream): Path = {
    val destDirName = s"/tmp/${UUID.randomUUID().toString}"

    val destination = FS.getPath(destDirName)

    val compressorInput = new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(archive))



    extractInternal(destination, compressorInput)


    destination
  }

  private def extractInternal2(destination: Path, compressorInput: CompressorInputStream): Unit = {
    var archiveInput: ArchiveInputStream = null
    try {
      archiveInput = new ArchiveStreamFactory().createArchiveInputStream("tar", compressorInput)
      entries(Stream.empty, archiveInput).foreach { entry =>
        val path: Path = destination.resolve(entry.getName)
        if (entry.isDirectory) {
          Files.createDirectories(path)
        }
        else {
          Files.createDirectories(path.getParent)
          Files.copy(archiveInput, path)
        }
      }
    } finally {
      IOUtils.closeQuietly(archiveInput)
    }

  }

  private def extractInternal(destination: Path, compressorInput: CompressorInputStream): Unit = {
    var archiveInput: ArchiveInputStream = null
    try {
      archiveInput = new ArchiveStreamFactory().createArchiveInputStream("tar", compressorInput)


      var entry: ArchiveEntry = null
      while ((({
        entry = archiveInput.getNextEntry;
        entry
      })) != null) {
        val path: Path = destination.resolve(entry.getName)

        if (entry.isDirectory) {
            Files.createDirectories(path)
        }
        else {
          Files.createDirectories(path.getParent)
          Files.copy(archiveInput, path)
        }
      }
    } finally {
      IOUtils.closeQuietly(archiveInput)
    }
  }

  def list(dir: Path, filter: Path => Boolean = _ => true): Seq[Path] = {


    Option(getFiles(dir)).map { paths =>
      paths.filter(p => Files.isRegularFile(p) && filter(p)) ++ paths.filter(Files.isDirectory(_)).flatMap(list(_, filter))
    }.getOrElse(Seq())

  }

  private def fileNameEndsWith(path: Path, suffix: String) = path.getFileName.toString.endsWith(suffix)

  private def isPomFile(file: Path) = fileNameEndsWith(file, ".pom")


  private def listPomDirs(dir: Path) = list(dir, isPomFile).map(_.getParent)

  private def getJarByPomFromDir(dir: Path) = {
    val files = getFiles(dir)
    val poms = files.filter(f => Files.isRegularFile(f) && isPomFile(f))
    poms.map { pomFile =>

      val model = new MavenXpp3Reader().read(Files.newBufferedReader(pomFile))

      val groupId = Option(model.getGroupId).getOrElse(model.getParent.getGroupId)
      val artifactId = model.getArtifactId
      val version = Option(model.getVersion).getOrElse(model.getParent.getVersion)

      val packaging = model.getPackaging match {
        case "bundle" => "jar"
        case "karaf-assembly" => "zip"
        case anythingElse => anythingElse
      }
      files.find(f => fileNameEndsWith(f, s".$packaging") && !fileNameEndsWith(f, s"-sources.$packaging")).map(
        Art(groupId, artifactId, version, _, pomFile)
      )

    }.filter(_.isDefined).map(_.get).headOption

  }


  def getArtifacts(archive: InputStream): Seq[Art] = {
    val pomDirs = TarGzExtract.listPomDirs _ compose TarGzExtract.extract
    pomDirs(archive).map(TarGzExtract.getJarByPomFromDir).filter(_.isDefined).map(_.get)
  }

  def getFiles(path: Path) = {
    Files.list(path).collect(Collectors.toList()).asScala.toSeq
  }
}
