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

object GitHub extends Controller {

  val conf = Play.current.configuration
  val base = conf.getString("github.base").getOrElse("")

  def commit (projectOwner : String, projectName : String, sha : String) = {
   val res = WS.url(base + "/repos/" + projectOwner + "/" + projectName + "/commits/" + sha).get.value
   val json = Json.parse(res.get.body.toString())
   val additions = json \ "stats" \ "additions"
   val deletions = json \ "stats" \ "deletions"
   " Stats : Additions " + additions + " - Deletions " + deletions
  }
}