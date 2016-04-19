import com.typesafe.sbt.GitPlugin.autoImport._
import sbtassembly.AssemblyPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations.{setReleaseVersion => _, _}
import sbtrelease._

val requiredJavaVersion: String = "1.8"
initialize := {
  val required = requiredJavaVersion
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

resolvers += Resolver.bintrayRepo("jfrog", "bintray-tools")
resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

scalacOptions += "-target:jvm-" + requiredJavaVersion
javacOptions ++= Seq("-source", requiredJavaVersion, "-target", requiredJavaVersion, "-Xlint")


val bintrayClientVersion: String = "0.8.3"
val awsSdkVersion: String = "1.1.0"

val mavenVersion: String = "3.3.9"
val VersionRegex = "v([0-9]+.[0-9]+.[0-9]+)-?(.*)?".r
lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt, JavaAppPackaging).
  settings(
    name := "s3-bintray-deploy",
//    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.7",
    retrieveManaged := true,
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % awsSdkVersion,
      "com.amazonaws" % "aws-lambda-java-events" % awsSdkVersion,
      "com.github.seratch" %% "awscala" % "0.5.+",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.11",
      "com.typesafe.scala-logging" % "scala-logging_2.11" % "3.1.0",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "org.rauschig" % "jarchivelib" % "0.7.1",
      "com.jfrog.bintray.client" % "bintray-client-java-api" % bintrayClientVersion,
      "com.jfrog.bintray.client" % "bintray-client-java-service" % bintrayClientVersion,
      //      "io.reactivex" % "rxscala_2.11" % "0.26.0",
      "io.reactivex" % "rxjava" % "1.1.3",
      "org.apache.maven" % "maven-model" % mavenVersion,
      "org.apache.maven" % "maven-core" % mavenVersion,
      "org.apache.maven" % "maven-artifact" % mavenVersion,
      "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"

    ).map(_.exclude("commons-logging", "commons-logging")),


    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "hu.blackbelt.cd.bintray.deploy",

    publishTo := Some("temp" at "file:///tmp/repository"),

    assemblyJarName in assembly := s"${name.value}-${releaseVersion.value}.jar",

    mappings in Universal <<= (mappings in Universal, assembly in Compile) map { (mappings, fatJar) =>
      val filtered = mappings filter { case (file, name) => !name.endsWith(".jar") }
      filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    },
    scriptClasspath := Seq((assemblyJarName in assembly).value),

    git.useGitDescribe := true,
    git.baseVersion := "0.0.0",


    git.gitTagToVersionNumber := {
      case VersionRegex(v, "") => Some(v)
      case VersionRegex(v, "SNAPSHOT") => Some(s"$v-SNAPSHOT")
      case VersionRegex(v, s) => Some(s"$v-$s-SNAPSHOT")
      case _ => None
    },
    releaseVersion <<= (releaseVersionBump) (bumper => {
      ver => Version(ver)
        .map(_.withoutQualifier)
        .map(_.bump(bumper).string).getOrElse(versionFormatError)
    }),

    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      setReleaseVersion,
      runTest,
      tagRelease,
      // publishArtifacts,
      ReleaseStep(releaseStepTask(publish in Universal)),
      pushChanges
    )

  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

com.updateimpact.Plugin.openBrowser in ThisBuild := true


// we hide the existing definition for setReleaseVersion to replace it with our own
import sbtrelease.ReleaseStateTransformations.{setReleaseVersion => _, _}

def setVersionOnly(selectVersion: Versions => String): ReleaseStep = { st: State =>
  val vs = st.get(ReleaseKeys.versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))
  val selected = selectVersion(vs)

  st.log.info("Setting version to '%s'." format selected)
  val useGlobal = Project.extract(st).get(releaseUseGlobalVersion)
  val versionStr = (if (useGlobal) globalVersionString else versionString) format selected

  reapply(Seq(
    if (useGlobal) version in ThisBuild := selected
    else version := selected
  ), st)
}

lazy val setReleaseVersion: ReleaseStep = setVersionOnly(_._1)





