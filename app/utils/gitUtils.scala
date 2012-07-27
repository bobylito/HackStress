package utils

import java.io.File
import org.eclipse.jgit.api.Git

object GitUtils {
  def projectNameRegex = "([a-zA-Z0-9]+).git".r
  
  def clone(url: String, outdir: File ) = {
    if(!outdir.exists || !outdir.isDirectory)
      throw new RuntimeException("Outdir doesn't exist")
    //val oldDir = System.getProperty("user.dir")
    val projectNameMatched = projectNameRegex.findFirstMatchIn(url)
    
    projectNameMatched.map( {projectName =>
        val projectDir = new File(outdir, projectName.group(1))
        
        if(projectDir.exists)
          org.apache.commons.io.FileUtils.deleteDirectory(projectDir)
        //System.setProperty("user.dir", outdir.getAbsolutePath)
        
        val cmd  = Git.cloneRepository().setURI(url).setDirectory(projectDir)
        
        try{
          cmd.call()
        }
        finally {
          //System.setProperty("user.dir", oldDir)
        }
    }).getOrElse( throw new RuntimeException("Can't find project name in : "+url))
  }
}
