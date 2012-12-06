import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "giraffe_server"

version := "1.0"

scalaVersion := "2.9.2"

//extra library
libraryDependencies ++= Seq(
   "org.scala-tools" % "scala-tools-parent" % "1.6",
   "net.liftweb" % "lift-mapper_2.9.2" % "2.5-M3",
   "net.java.dev.jna" % "jna" % "3.5.1",
   "com.h2database" % "h2" % "1.3.170"
) 

//main class
mainClass in assembly := Some("jnaTest")
