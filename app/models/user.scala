package models

case class User(id: Long, email: String)

object User extends cagette.Cageot[User,Long]
