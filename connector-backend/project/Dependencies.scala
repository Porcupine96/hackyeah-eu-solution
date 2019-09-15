import DependencyVersions._
import sbt.{SbtExclusionRule, _}

object Dependencies {

  private val grpcDependencies = Seq(
    "io.grpc"                % "grpc-netty"            % grpcNettyVersion,
    "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % grpcRuntimeVersion
  )

  private val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor"       % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"       % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"      % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion
  )

  private val loggingDependencies = Seq(
    "ch.qos.logback"             % "logback-classic"  % logbackVersion,
    "ch.qos.logback"             % "logback-core"     % logbackVersion,
    "org.slf4j"                  % "jcl-over-slf4j"   % slf4jVersion,
    "org.slf4j"                  % "log4j-over-slf4j" % slf4jVersion,
    "org.slf4j"                  % "jul-to-slf4j"     % slf4jVersion,
    "com.typesafe.scala-logging" %% "scala-logging"   % scalaLoggingVersion
  )

  private val utilDependencies = Seq(
    "org.json4s" %% "json4s-native" % json4sVersion,
    "org.json4s" %% "json4s-core"   % json4sVersion
  )

  private val akkaHttpDependencies = Seq(
    "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core"   % akkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % akkaHttpJson4sVersion
  )

  private val mongoDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % mongoVersion
  )

  private val kafkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % kafkaVersion
  )

  val projectDependencies: Seq[ModuleID] = Seq(
    grpcDependencies,
    akkaDependencies,
    akkaHttpDependencies,
    utilDependencies,
    loggingDependencies,
    mongoDependencies,
    kafkaDependencies
  ).reduce(_ ++ _)

  val additionalResolvers = Seq(
    Resolver.typesafeRepo("releases")
  )

  val globalExcludes = Seq(
    SbtExclusionRule("log4j"),
    SbtExclusionRule("log4j2"),
    SbtExclusionRule("commons-logging")
  )

}
