package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import utils.GitUtils
import java.io.File
import org.apache.commons.io.FileUtils

class GitUtilsSpec extends Specification {
  "Application" should {
    "be able to clone a repo" in {
      val dir = new File("/tmp/gitRepos")
      if(dir.exists)
        FileUtils.deleteDirectory(dir)
      dir.mkdir
      GitUtils.clone("https://github.com/bobylito/painterGame.git", dir)
      true must equalTo(true)
    }
  }
}
