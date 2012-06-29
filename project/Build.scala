import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "HackStress"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "guillaume.bort" %% "cagette" % "0.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
