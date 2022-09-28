import xerial.sbt.Sonatype.SonatypeKeys.sonatypeProfileName

organization := "co.spendabit"

name := "Webapp Tools"

version := build.libVersion

crossScalaVersions := Seq("2.11.12", "2.12.14")

scalacOptions ++= Seq("-feature", "-language:implicitConversions")

resolvers ++= Seq(Classpaths.typesafeReleases)

libraryDependencies ++= Seq(
  "javax.mail" % "mail" % "1.4.7",
  "commons-validator" % "commons-validator" % "1.6",
  "org.jsoup" % "jsoup" % "1.11.2",
  "org.scalatest" %% "scalatest" % "3.1.4",
  "org.scalatra" %% "scalatra-scalatest" % "2.7.1",
  "org.scalatra" %% "scalatra" % "2.7.1",
  "commons-fileupload" % "commons-fileupload" % "1.3.3",
  "org.log4s" %% "log4s" % "1.6.1"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

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
  </developers>

sonatypeProfileName := "co.spendabit"

(Compile / scalaSource) := { (Compile / baseDirectory)(_ / "src") }.value

(Test / scalaSource) := { (Test / baseDirectory)(_ / "test") }.value

unmanagedClasspath in Test += baseDirectory.value / "test" / "resources"

// Use "-oF" switch to get full stack-traces.
(Test / testOptions) += Tests.Argument(TestFrameworks.ScalaTest, "-oS")

(Compile / sourceGenerators) += Def.task {
  CodeGeneration.generatedFiles.map { f =>
    val file = (Compile / sourceManaged).value / f.path.toString
    IO.write(file, f.content)
    file
  }
}.taskValue
