package models

case class Message(id: String, projet: String, texte: String)

object Message extends cagette.Cageot[Message,String]