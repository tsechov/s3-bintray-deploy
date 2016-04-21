package hu.blackbelt.cd.bintray.deploy

import java.io.{FileInputStream, File}

import org.scalatest.FunSuite

class E2ETest extends FunSuite with Creds {

  ignore("download-extract-upload") {

    val project = "judo"
    val version = "1.0.638"


//    val archive = S3Get.get("bb-build-artifacts", s"${project}/${version}/${project}-${version}.tar.gz")
    val archive = new FileInputStream(new File("storage/judo+1.0.638+judo-1.0.638.tar.gz2519734373981498417.tmp"))


    val loc = StorageLocation("bb-build-artifacts", s"${project}/${version}/${project}-${version}.tar.gz")

    val deployer = new Deploy(Project(loc, project, version))

    deployer.upload(archive).failed.toOption.map(fail(_))

  }
}
