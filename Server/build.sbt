import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "seeken_server"

version := "1.0"

scalaVersion := "2.9.2"

//extra library
libraryDependencies ++= Seq(
   "org.scala-tools" % "scala-tools-parent" % "1.6", // scala tools
   "net.liftweb" % "lift-mapper_2.9.2" % "2.5-M3", //lift orm 
   "net.java.dev.jna" % "jna" % "3.5.1", //jna
   "com.h2database" % "h2" % "1.3.170", //h2
   "org.apache.httpcomponents" % "httpcore" % "4.2.2", //HttpComponents
   "org.apache.httpcomponents" % "httpclient" % "4.2.1", //HttpComponents
   "commons-io" % "commons-io" % "2.4" //commons io
) 

//unit test lib
libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "latest.integration" % "test",
  "org.specs2" %% "specs2-scalaz-core" % "latest.integration" % "test",
  "junit" % "junit" % "latest.integration" % "test",
  "org.scalatest" %% "scalatest" % "latest.integration" % "test"
)

//main class
mainClass in assembly := Some("jnaTest")
