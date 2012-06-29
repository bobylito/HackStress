package controllers

import play.api.data._
import play.api.data.Forms._
import play.api._
import play.api.mvc._
import models.Projet
import views._
import java.net.URLEncoder
import play.api.libs.ws._
import play.api.libs.json._
import play.api.Logger
import models._

object GitHub {

  val conf = Play.current.configuration
  val base = conf.getString("github.base").getOrElse("")

  implicit object JsArrayReads extends Reads[JsArray] {
    def reads(json: JsValue) = json match {
      case o: JsArray => o
      case _ => throw new RuntimeException("blabla")
    }
  }

  def commitMetrics (projectOwner : String, projectName : String, sha : String) = {
   val res = WS.url(base + "/repos/" + projectOwner + "/" + projectName + "/commits/" + sha).get.value
   val json = Json.parse(res.get.body.toString())

   Metrics(
    date = (json \ "commit" \ "committer" \ "date").asOpt[String].getOrElse(""),
    additions = (json \ "stats" \ "additions").asOpt[Int].getOrElse(0),
    deletions = (json \ "stats" \ "deletions").asOpt[Int].getOrElse(0),
    repoName = projectName,
    committer = (json \ "committer" \ "login").asOpt[String].getOrElse("Nc"),
    numberOfFiles = (json \ "files").asOpt[JsArray].map(_.value.size).getOrElse(0)
   )
  }
}