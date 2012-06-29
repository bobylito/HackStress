package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.oauth._
import play.api.libs.ws.WS
import models._
import play.api.libs.concurrent.Promise

object Application extends Controller with OAuthAuthentication {
  
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
        
        val msg = Message(id.toString, repo.toString, 
            "Repo : " + repo.toString +" - Commiter : " + user.toString + " - " + message.toString )

        channel.push( msg )
    }

    import java.net.URLEncoder

    def tweet( msg: String ) = Authenticated { token => implicit request =>
        Async { WS.url(tweetUpdateUrl + "?status=%s".format(URLEncoder.encode(msg, "UTF-8")))
            //.withQueryString("status" -> msg)
            .sign(OAuthCalculator(consumerKey, 
                play.api.libs.oauth.RequestToken(
                    "621761912-F9e7sgsncC60TXMTo6rBsF0NxPlKAATD8dfYNqYw",
                    "q1B0HTY4ArhDsDV7jZYnp30EIdCsh0cobIOidGlc4")))
            .post("ignored")
            .map { resp =>
                Ok("Update Resp:%s".format(resp.json))
            }
        }
    }

    def oauthcallback = Authenticated { token => implicit request =>
        Ok("Authenticated")
    }

    val authenticateCall = routes.Application.authenticate
    val authenticatedCall = routes.Application.index

    val consumerKey = ConsumerKey("uRNd9XuEzmr207c1Vl38w", "W0YB20CjF0GWw538ikzSn2horFvL1hjwaduICKLdmVM")
    val oAuth = OAuth(ServiceInfo("https://api.twitter.com/oauth/request_token",
        "https://api.twitter.com/oauth/access_token",
        "https://api.twitter.com/oauth/authorize",
        consumerKey))

    val tweetUpdateUrl = "https://api.twitter.com/1/statuses/update.json"

}