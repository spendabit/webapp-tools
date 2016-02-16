import sbt._
import Keys._
import xerial.sbt.Sonatype.SonatypeKeys.sonatypeProfileName

object build extends Build {

  val libVersion = "0.0.46"

  lazy val project = Project (
    "webapp-tools",
    file("."),
    settings = Seq(

      organization := "co.spendabit",
      name := "Webapp Tools",
      version := libVersion,

      scalaVersion := "2.11.7",
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

      sonatypeProfileName := "co.spendabit",

      resolvers ++= Seq(Classpaths.typesafeReleases),

      libraryDependencies ++= Seq(
        "javax.mail" % "mail" % "1.4.7",
        "commons-validator" % "commons-validator" % "1.4.1",
        "org.jsoup" % "jsoup" % "1.8.2",
        "org.scalatest" %% "scalatest" % "2.2.4",
        "org.scalatra" %% "scalatra-scalatest" % "2.3.0",
        "org.scalatra" %% "scalatra" % "2.3.0" % "test"
      ),

      scalaSource in Compile <<= (baseDirectory in Compile)(_ / "src"),
      scalaSource in Test <<= (baseDirectory in Test)(_ / "test"),

      // Use "-oF" switch to get full stack-traces.
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oS"),

      sourceGenerators in Compile += Def.task {
        CodeGeneration.generatedFiles.map { f =>
          val file = (sourceManaged in Compile).value / f.path.toString
          IO.write(file, f.content)
          file
        }
      }.taskValue
    )
  )
}
