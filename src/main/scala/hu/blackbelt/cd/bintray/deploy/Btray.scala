package hu.blackbelt.cd.bintray.deploy

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.{Files, Path}

import com.jfrog.bintray.client.api.details.VersionDetails
import com.jfrog.bintray.client.api.handle.VersionHandle
import com.jfrog.bintray.client.impl.BintrayClient
import com.typesafe.scalalogging.LazyLogging
import rx.Completable
import rx.functions.{Action0, Action1}
import rx.schedulers.Schedulers

import scala.util.Try

case class Ver(handle: VersionHandle)

case class Batch(files: Seq[(String, Path)])

class Btray extends LazyLogging {
  val subject: String = sys.props.get(Access.bintray_organization).get
  val username: String = sys.props.get(Access.bintray_user).get
  val apiKey: String = sys.props.get(Access.bintray_apikey).get


  def uploader(ver: Ver) = {
    (file: File, path: String) => {
      ver.handle.upload(path, new BufferedInputStream(new FileInputStream(file)))
    }
  }

  def version(repository: String, pack: String, ver: String): Try[Ver] = {
    Try {
      val bintray = BintrayClient.create(username, apiKey)
      val pkg = bintray.subject(subject).repository(repository).pkg(pack)
      val version = pkg.version(ver)
      if (!version.exists()) {
        val details: VersionDetails = new VersionDetails(ver)
        details.setVcsTag(s"v$ver")
        Ver(pkg.createVersion(details))
      } else {
        Ver(version)
      }
    }

  }


  def uploadTo(v: Ver, batches: List[Batch]) = {
    import scala.collection.JavaConverters._

    val cs = batches.map(batch => Completable.fromAction(new Action0 {
      override def call(): Unit = {
        for (f <- batch.files) {
          logger.debug(s"[{}] uploading to version[{}]: {}", Thread.currentThread.getName, v.handle.get.name, f._1)
          v.handle.upload(f._1, new BufferedInputStream(Files.newInputStream(f._2)))
          logger.debug(s"[{}] uploaded: {}", Thread.currentThread.getName, f._1)
        }
      }
    }).subscribeOn(Schedulers.computation())).asJavaCollection

    Completable.merge(cs).doOnError(new Action1[Throwable] {
      override def call(t: Throwable): Unit = {
        logger.error("error during upload", t)
        logger.info(s"discarding version[{}]", v.handle.get.name)
        try {
          v.handle.discard()
          logger.info(s"version[{}] discarded", v.handle.get.name)
        } catch {
          case t: Throwable => logger.error("error during version discard", t)
        }
      }
    }).doOnCompleted(new Action0 {
      override def call(): Unit = {
        logger.info(s"publishing version[{}]", v.handle.get.name)
        v.handle.publish
        logger.info(s"version[{}] published", v.handle.get.name)
      }
    }).await()



  }


}
