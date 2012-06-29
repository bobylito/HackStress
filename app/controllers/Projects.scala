package controllers

import play.api.data._
import play.api.data.Forms._

import play.api.mvc._
import models.Projet
import views._

import play.api.Logger

object Projects extends Controller {

  val projectForm: Form[Projet] = Form(
    mapping(
      "name" -> text,
      "urlRepo" -> text,
      "plTrigram" -> text
    )(Projet.apply)(Projet.unapply)
  )

  def form = Action {
    Ok(html.projects.form(projectForm))
  }

  val submit = Action { implicit request =>
    projectForm.bindFromRequest.fold(
      errors => BadRequest(html.projects.form(errors)),
      project => {
        Projet.save(project)
        Logger.debug("Saved " + Projet.findOneBy(_.urlRepo == project.name).get.toString)
        Redirect(routes.Projects.list)
      }
    )
  }

  val list = TODO

}
