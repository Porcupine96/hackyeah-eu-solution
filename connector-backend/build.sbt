import sbt.Keys.baseDirectory

name := "chatplatform-plugin-dedicated-eu"
version := "0.3"
scalaVersion := "2.12.4"

enablePlugins(SbtNativePackager)
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerBaseImage := "java:openjdk-8"
daemonUser in Docker := "root"
dockerRepository := Some("docker.codeheroes.io")
dockerExposedPorts := Seq(8080)

lazy val `chatplatform-plugin-dedicated-eu` = project
  .in(file("."))
  .settings(resolvers ++= Dependencies.additionalResolvers)
  .settings(excludeDependencies ++= Dependencies.globalExcludes)
  .settings(libraryDependencies ++= Dependencies.projectDependencies)
  .settings(scalacOptions ++= CompilerOpts.all)
  .settings(PB.targets in Compile := Seq(
    scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
  ))
  .settings(
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile, doc) := Seq.empty,
    scalafmtTestOnCompile in ThisBuild := true,
    scalafmtOnCompile in ThisBuild := false
  )

PB.protoSources in Compile := Seq(
  baseDirectory.value / "other-protobufs",
  baseDirectory.value / "chatplatform-protobufs" / "protocol",
  baseDirectory.value / "chatplatform-protobufs" / "plugins" / "common"
)
