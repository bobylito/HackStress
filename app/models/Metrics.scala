package models

import cagette._
import java.util.Date

case class Metrics (
    date : Date,
    repoName: String,
    additions : Int,
    deletions : Int,
    numberOfFiles: Int,
    commiter: String) 
