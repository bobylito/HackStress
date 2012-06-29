package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.json._

object Application extends Controller {
  
    val ( output, channel ) = Concurrent.broadcast[JsValue]

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def client = Action{
      Ok(views.html.client("Hi"))
    }
    
    def live = Action {
        Ok.feed(
            output &> 
            EventSource[JsValue]() ><> 
            Enumeratee.map(_.getBytes("UTF-8"))
        ).as("text/event-stream")
    }

    def push = Action {
        channel.push( Json.obj( "type" -> "bonus", "name" -> "toto" ))
        Ok( "Pushed")
    }

    val webHookForm = Form( "payload" -> nonEmptyText )

    def webhook = Action { implicit request =>
        Logger.debug("Message reçu")

        webHookForm.bindFromRequest.fold(
            formWithErrors => { BadRequest },
            { case (payload) =>
                Logger.debug( "Contenu du message : " + payload )
                work( Json.parse(payload) )
                Ok
            }
        )
    }

    def work( json: JsValue ) = {
        println( json )
        channel.push( json )
    }

}