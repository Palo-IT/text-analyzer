name := "PaloTextAnalyzer"
version := "1.0"
scalaVersion := "2.11.8"
libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % "latest.release",
  "org.apache.lucene" % "lucene-analyzers-common" % "latest.release",
  "org.apache.lucene" % "lucene-misc" % "latest.release",
  "org.apache.lucene" % "lucene-memory" % "latest.release",
  "org.apache.lucene" % "lucene-queries" % "latest.release",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.http4s" %% "http4s-dsl" % "latest.release",
  "org.http4s" %% "http4s-blaze-server" % "latest.release",
  "org.http4s" %% "http4s-blaze-client" % "latest.release",
  "org.annolab.tt4j" % "org.annolab.tt4j" % "1.2.1"
)
unmanagedResourceDirectories in Compile += baseDirectory.value / "src/webapp"
includeFilter in (Compile, unmanagedSources) := "*.html"