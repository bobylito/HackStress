package models

import cagette._

case class Projet(urlRepo: String, name: String, plTrigram: String)
object Projet extends Cageot[Projet, String]()(Identifier(_.urlRepo))