package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import play.libs.Json

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def webhook = Action { implicit request =>

    request.body.asMultipartFormData.map({ f =>
      f.asFormUrlEncoded.get("payload").map({ json => 
        val parsedJson = Json.parse(json(0))
        println(parsedJson)
      }).getOrElse(
        InternalServerError
      )
    }) 
    Ok
  }

}