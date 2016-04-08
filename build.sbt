import java.io.FileInputStream
import java.util.Properties

import sbtassembly.AssemblyPlugin.autoImport._

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val root = (project in file(".")).
  settings(
    name := "lambda-demo",
    version := "1.0",
    scalaVersion := "2.11.4",
    retrieveManaged := true,
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.amazonaws" % "aws-lambda-java-events" % "1.1.0",
      "com.github.seratch" %% "awscala" % "0.5.+",
      "org.codehaus.plexus" % "plexus-archiver" % "3.1",
      "org.apache.commons" % "commons-compress" % "1.11",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.18",
      "ch.qos.logback" % "logback-classic" % "1.1.6",
      "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"

    )
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

testOptions in Test += Tests.Setup(() => {
  import scala.collection.JavaConverters._
  val prop = new Properties()
  prop.load(new FileInputStream("env.properties"))
  prop.entrySet().asScala.foreach {
    (entry) => {
      ()

    }
  }

})
