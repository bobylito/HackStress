package models

import cagette._
import java.util.Date

case class Metrics (
    id: Long = Metrics.autoIncrement,
    date : String,
    repoName: String,
    additions : Int,
    deletions : Int,
    numberOfFiles: Int,
    committer: String) 

object Metrics extends cagette.Cageot[Metrics,Long]
