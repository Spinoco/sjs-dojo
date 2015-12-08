import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._


object Build extends Build  {


  val commonSettings = Defaults.coreDefaultSettings ++ Seq(
    organization := "spinoco"
    , scalaVersion := "2.11.7"
    , version :=  "15.1.0"
    , scalacOptions += "-deprecation"
    , scalacOptions += "-unchecked"
    , scalacOptions += "-feature"
    , scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "off")
    , relativeSourceMaps := true
    , resolvers += Resolver.sonatypeRepo("releases")
    , crossScalaVersions := Seq("2.11.5")
    , libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.7.0"
      , "be.doeraene" %%% "scalajs-jquery" % "0.7.0"
      , "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.1.0-4"
    )
  )

  // Support, react, http etc
  lazy val support = Project(
    id = "support"
    , base = file("./support")
    , settings = commonSettings
  ).enablePlugins(ScalaJSPlugin)

  lazy val main = Project(
    id = "coding-dojo"
    , base = file(".")
    , settings = commonSettings ++ Seq(
      requiresDOM := true
    )
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(support)


}
