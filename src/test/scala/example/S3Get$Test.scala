package example

import java.io.FileInputStream
import java.util.Properties

import org.scalatest.{BeforeAndAfter, FunSuite}


class S3Get$Test extends FunSuite with BeforeAndAfter {

  before {
    val p = new Properties()
    p.load(new FileInputStream("env.properties"))
    sys.props.++=()
  }
  test("testGet") {
    val result = S3Get.get("bb-build-artifacts", "judo/1.0.638/judo_build_557_build.log.tar.gz")
    assert(result._1.isDefined)
    println(result._1)
    println(result._2)
  }

}
