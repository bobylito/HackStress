package controllers

import play.api._
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

    def webhook = Action { implicit request =>
        Logger.debug("Message reçu")
        request.body.asMultipartFormData.map({ f =>
            f.asFormUrlEncoded.get("payload").map({ json => 
                work( Json.parse(json(0)) )
                
            }).getOrElse(InternalServerError)
        })
        Ok
    }

    def work( json: JsValue ) = {
        println( json )
        channel.push( json )
    }

}