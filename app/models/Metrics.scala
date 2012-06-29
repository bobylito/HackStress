package models

import cagette._
import java.util.Date

case class Metrics (
    date : String,
    repoName: String,
    additions : Int,
    deletions : Int,
    numberOfFiles: Int,
    committer: String) 
