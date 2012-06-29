package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.json._

import models._

object Application extends Controller {
  
    val ( output, channel ) = Concurrent.broadcast[Message]

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }

    def client = Action{
      Ok(views.html.client("Hi"))
    }
    
    def live (project : String) = Action {
        Ok.feed(
            output &> 
            Enumeratee.filter(m => m.projet != project) &>
            Enumeratee.map( m=> Json.obj( "projet" -> m.projet, "texte" -> m.texte ).as[JsValue] ) &>
            EventSource[JsValue]() ><> 
            Enumeratee.map(_.getBytes("UTF-8"))
        ).as("text/event-stream")
    }

    def push = Action {
        channel.push( Message("-1","toto","hello") )
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

        val id = json \ "id"
        val user = json \ "pusher" \ "name"
        val repo = json \ "repository" \ "name"
        val sizeRepo = json \ "repository" \ "size"
        val message = json \ "message"
        val timeStamp = json \ "timestamp"
        
        channel.push( Message(id.toString, repo.toString, 
            "Repo : " + repo.toString +" - Commiter : " + user.toString + " - " + message.toString ) )
    }

}