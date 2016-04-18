package hu.blackbelt.cd.bintray.deploy

import org.scalatest.FunSuite

class AccessTest extends FunSuite with Creds{

  test("testCollect") {
    Access.collect
    assert(sys.props.get(Access.bintray_organization).isDefined)
    assert(sys.props.get(Access.bintray_user).isDefined)
    assert(sys.props.get(Access.bintray_apikey).isDefined)
    assert(sys.props.get(Access.aws_accessKeyId).isDefined)
    assert(sys.props.get(Access.aws_secretKey).isDefined)
  }

}
