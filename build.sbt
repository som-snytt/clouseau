import ReleaseTransformations._

import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings

lazy val noPublish = Seq(publish := {}, publishLocal := {}, publishArtifact := false)

lazy val clouseauSettings = Seq(
  organization := "org.spire-math",
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.4"),
  libraryDependencies ++= (
    "org.scalacheck" %% "scalacheck" % "1.13.5" % Test ::
    Nil),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  ),
  // HACK: without these lines, the console is basically unusable,
  // since all imports are reported as being unused (and then become
  // fatal errors).
  scalacOptions in (Compile, console) ~= { _.filterNot("-Xlint" == _) },
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,

  // settings for packaging instrumentation correctly
  packageOptions in (Compile, packageBin) +=
    Package.ManifestAttributes("Premain-Class" -> "clouseau.Inst"),

  // settings needed for using instrumenation correctly
  javaOptions += "-javaagent:target/scala-2.12/clouseau_2.12-0.1-SNAPSHOT.jar",
  fork := true,

  // release stuff
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    //runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <url>https://github.com/non/clouseau</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
        <comments>A business-friendly OSS license</comments>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:non/clouseau.git</url>
      <connection>scm:git:git@github.com:non/clouseau.git</connection>
    </scm>
    <developers>
      <developer>
        <id>non</id>
        <name>Erik Osheim</name>
        <url>http://github.com/non/</url>
      </developer>
    </developers>
  ),
  coverageMinimum := 60,
  coverageFailOnMinimum := false
) ++ mimaDefaultSettings

def previousArtifact(suffix: String) =
  "org.spire-math" %% s"clouseau$suffix" % "0.2.0"

lazy val core = project
  .in(file("."))
  .settings(name := "clouseau")
  .settings(moduleName := "clouseau")
  .settings(clouseauSettings: _*)
  .settings(mimaPreviousArtifacts := Set(previousArtifact("")))

// lazy val docs = project
//   .in(file("docs"))
//   .dependsOn(core)
//   .settings(name := "clouseau-docs")
//   .settings(clouseauSettings: _*)
//   .settings(noPublish: _*)
//   .settings(tutSettings: _*)
//   .settings(tutScalacOptions := {
//     val testOptions = scalacOptions.in(test).value
//     val unwantedOptions = Set("-Xlint", "-Xfatal-warnings")
//     testOptions.filterNot(unwantedOptions)
//   })
