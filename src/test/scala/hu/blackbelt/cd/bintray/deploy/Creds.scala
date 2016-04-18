package hu.blackbelt.cd.bintray.deploy

import java.io.FileInputStream
import java.util.Properties

import org.scalatest.{BeforeAndAfter, Suite}

trait Creds extends BeforeAndAfter {
  this: Suite =>
  before {
    import scala.collection.JavaConverters._
    val prop = new Properties()
    prop.load(new FileInputStream("env.properties"))
    prop.entrySet().asScala.foreach {
      (entry) => {
        sys.props += ((entry.getKey.asInstanceOf[String], entry.getValue.asInstanceOf[String]))
      }
    }
  }


}
