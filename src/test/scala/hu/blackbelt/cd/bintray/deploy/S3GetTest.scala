package hu.blackbelt.cd.bintray.deploy

import org.scalatest.FunSuite


class S3GetTest extends FunSuite with Creds {


  test("testGet") {

    val result = S3Get.get("bb-build-artifacts", "judo/1.0.638/judo_build_557_build.log.tar.gz"){
      is => assert(is.available()>0)
    }


  }

}
