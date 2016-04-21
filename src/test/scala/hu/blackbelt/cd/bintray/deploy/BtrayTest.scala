package hu.blackbelt.cd.bintray.deploy

import java.io.File
import java.nio.file.Paths

import org.scalatest.FunSuite

class BtrayTest extends FunSuite with Creds {


  test("testUpload") {

    val b = new Btray
    val ver = b.version("releases", "judo", "1.0.638")

    val api = List(
      ("hu/blackbelt/judo/common/common-context-api/1.0.638/common-context-api-1.0.638.jar", Paths.get("src/test/resources/common-context-api-1.0.638.jar"))
    )
    val regular = List(
      ("hu/blackbelt/judo/common/common-context-regular/1.0.638/common-context-regular-1.0.638.jar", Paths.get("src/test/resources/common-context-regular-1.0.638.jar"))
    )

    ver.map(
      b.uploadTo(_, List(Batch(api), Batch(regular)))
    )

    ver.failed.toOption.map(fail(_))

    ver.foreach(_.handle.delete())


  }


}
