package models

case class Message(id: Long, projet: String, texte: String)

object Message extends cagette.Cageot[Message,Long]