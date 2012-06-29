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
        Ok(views.html.index(""))
    }

    def stats = Action {
      Ok(views.html.stats("Statistiques"))
    }

    def client = Action{
      Ok(views.html.client("Hi"))
    }

    def liveAll = Action {
        Ok.feed(
            output &> 
            Enumeratee.map( m=> Json.obj( "projet" -> m.projet, "texte" -> m.texte ).as[JsValue] ) &>
            EventSource[JsValue]() ><> 
            Enumeratee.map(_.getBytes("UTF-8"))
        ).as("text/event-stream")        
    }
    
    def live( project : String ) = Action {
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

    def fake = Action {
        work(Json.parse(texteTest))
        Ok("Pushed")
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

    def ts( value: JsValue ): String = {
        value.asOpt[String].getOrElse("Nc")
    }

    def work( json: JsValue ) = {
        val id = ts( json \ "id" )
        val user = ts( json \ "pusher" \ "name" )
        val repo = ts( json \ "repository" \ "name" )
        val sizeRepo = ts( json \ "repository" \ "size" )
        val message = ts( json \ "head_commit" \ "message" )
        val timeStamp = ts( json \ "head_commit" \ "timestamp" )
        val repoOwner = ts( json \ "repository" \ "owner" \ "name" )

        Metrics.save(GitHub.commitMetrics(repoOwner,repo,id))

        Logger.info( message.toString)

        val msg = Message(id.toString, repo, "Repo : " + repo + " - Commiter : " + user + " - " + message )
        tweet(msg.texte + " #zen_hackstress")
        channel.push( msg )
    }

    import java.net.URLEncoder

    def pushTweet( msg: String ) = Authenticated { token => implicit request =>
        Async { WS.url(tweetUpdateUrl + "?status=%s".format(URLEncoder.encode(msg, "UTF-8")))
            //.withQueryString("status" -> msg)
            .sign(OAuthCalculator(consumerKey, token))
            .post("ignored")
            .map { resp =>
                Ok("Update Resp:%s".format(resp.json))
            }
        }
    }

    def tweet( msg: String ) = WS.url(tweetUpdateUrl + "?status=%s".format(URLEncoder.encode(msg, "UTF-8")))
        .sign(OAuthCalculator(consumerKey, accessToken))
        .post("ignored")
        .map { resp =>
            Ok("Update Resp:%s".format(resp.json))
        }

    def commitMetrics( projectOwner: String, projectName: String, sha: String ) = Action {
        Ok(GitHub.commitMetrics(projectOwner, projectName, sha).toString)
    }

    def oauthcallback = Authenticated { token => implicit request =>
        Ok("Authenticated")

    }

    val authenticateCall = routes.Application.authenticate
    val authenticatedCall = routes.Application.index

    val consumerKey = ConsumerKey("uRNd9XuEzmr207c1Vl38w", "W0YB20CjF0GWw538ikzSn2horFvL1hjwaduICKLdmVM")
    
    val accessToken = RequestToken(
        "621761912-F9e7sgsncC60TXMTo6rBsF0NxPlKAATD8dfYNqYw",
        "q1B0HTY4ArhDsDV7jZYnp30EIdCsh0cobIOidGlc4")

    val oAuth = OAuth(ServiceInfo("https://api.twitter.com/oauth/request_token",
        "https://api.twitter.com/oauth/access_token",
        "https://api.twitter.com/oauth/authorize",
        consumerKey))



    val tweetUpdateUrl = "https://api.twitter.com/1/statuses/update.json"



    val texteTest = """
            {"pusher":{"name":"Timshel","email":"knujunk@free.fr"},
            "repository":{"name":"WebHook","created_at":"2012-06-29T04:10:10-07:00","size":92,"has_wiki":true,"private":false,"watchers":1,"url":"https://github.com/Timshel/WebHook","fork":false,"pushed_at":"2012-06-29T04:49:29-07:00","open_issues":0,"has_downloads":true,"has_issues":true,"forks":1,"description":"","owner":{"name":"Timshel","email":"knujunk@free.fr"}},
            "forced":false,
            "head_commit":{
                "modified":["README"],
                "added":[],
                "removed":[],
                "author":{"name":"Jacques","email":"jba@zenexity.com"},
                "timestamp":"2012-06-29T04:49:20-07:00",
                "url":"https://github.com/Timshel/WebHook/commit/d8ad3e06ff3290284541db191a30b3ecfbe2eff1",
                "id":"d8ad3e06ff3290284541db191a30b3ecfbe2eff1",
                "distinct":true,
                "message":"YAHHHHHHH",
                "committer":{"name":"Jacques","email":"jba@zenexity.com"}
            },
            "after":"d8ad3e06ff3290284541db191a30b3ecfbe2eff1",
            "deleted":false,
            "commits":[{"modified":["README"],"added":[],"removed":[],
                "author":{"name":"Jacques","email":"jba@zenexity.com"},
                "timestamp":"2012-06-29T04:49:20-07:00",
                "url":"https://github.com/Timshel/WebHook/commit/d8ad3e06ff3290284541db191a30b3ecfbe2eff1",
                "id":"d8ad3e06ff3290284541db191a30b3ecfbe2eff1",
                "distinct":true,
                "message":"YAHHHHHHH",
                "committer":{"name":"Jacques","email":"jba@zenexity.com"}}
            ],
            "ref":"refs/heads/master",
            "before":"3d9e18c5b757851b8c64f3525975b8ee45c46090",
            "compare":"https://github.com/Timshel/WebHook/compare/3d9e18c5b757...d8ad3e06ff32",
            "created":false}
    """
}
