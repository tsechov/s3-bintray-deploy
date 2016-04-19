resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.0")

addSbtPlugin("com.updateimpact" % "updateimpact-sbt-plugin" % "2.1.1")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.0.4")

addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.9.0")


