package hu.blackbelt.cd.bintray.deploy

import java.io._

import org.scalatest.FunSuite

class TarGzExtractTest extends FunSuite {

  test("testExtract") {

    val archive = new File("src/test/resources/test.tar.gz")

    println(s"extracting $archive")

    val results = TarGzExtract.getArtifacts(new FileInputStream(archive))
    results foreach println

    assert(results.size == 10)




  }


}
