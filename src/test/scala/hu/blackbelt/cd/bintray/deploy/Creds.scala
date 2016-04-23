package hu.blackbelt.cd.bintray.deploy

import java.io.{File, FileInputStream}
import java.util.Properties

import org.scalatest.{BeforeAndAfter, Suite}

trait Creds extends BeforeAndAfter {
  this: Suite =>

  def fillFromEnv(prop: Properties) = {
    def put(key: String) = sys.env.get(key.replace('.','_')).map(prop.put(key.replace('_','.'), _))
    put(Access.aws_accessKeyId)
    put(Access.aws_secretKey)
    put(Access.bintray_organization)
    put(Access.bintray_user)
    put(Access.bintray_apikey)
  }

  before {
    import scala.collection.JavaConverters._
    val prop = new Properties()
    val propsFile = new File("env.properties")
    if (propsFile.exists()) {
      prop.load(new FileInputStream(propsFile))
    } else {
      fillFromEnv(prop)
    }
    prop.entrySet().asScala.foreach {
      (entry) => {
        sys.props += ((entry.getKey.asInstanceOf[String], entry.getValue.asInstanceOf[String]))
      }
    }
  }


}
