import sbt._
import Keys._

object build extends Build {

  val libVersion = "0.0.1"

  lazy val project = Project (
    "webapp-tools",
    file("."),
    settings = Seq(

      organization := "co.spendabit",
      name := "Webapp Tools",
      version := libVersion,

      scalaVersion := "2.11.6",
      scalacOptions ++= Seq("-feature", "-language:implicitConversions"),

      publishMavenStyle := true,
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      publishArtifact in Test := false,
      pomExtra :=
        <url>https://github.com/spendabit/webapp-tools</url>
        <licenses>
          <license>
            <name>The Unlicense</name>
            <url>http://unlicense.org/</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>https://github.com/spendabit/webapp-tools</url>
          <connection>scm:git:git@github.com:spendabit/webapp-tools.git</connection>
        </scm>
        <developers>
          <developer>
            <id>chris</id>
            <name>Chris Wagner</name>
            <organization>Spendabit</organization>
            <organizationUrl>https://spendabit.co/</organizationUrl>
          </developer>
        </developers>,

      resolvers ++= Seq(Classpaths.typesafeReleases),

      libraryDependencies ++= Seq(

        // Money
        "org.joda" % "joda-money" % "0.9.1",

        // For testing
        "org.scalatest" %% "scalatest" % "2.2.4" % "test"
      ),

      scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src"),
      scalaSource in Test <<= (baseDirectory in Test)(_ / "test"),

      // Use "-oF" switch to get full stack-traces.
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oS"),

      sourceGenerators in Compile += Def.task {
        val file = (sourceManaged in Compile).value / "co" / "spendabit" / "version.scala"
        IO.write(file,
          s"""package co.spendabit
            |object version { def get = "$libVersion" }""".stripMargin)
        Seq(file)
      }.taskValue
    )
  )
}
