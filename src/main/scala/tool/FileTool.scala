package tool

import java.io.File

/**
  * Created by Administrator on 2017/4/16 0016.
  */
object FileTool {


  def getNextSequence(string: String): String = {
    val sequence = getSequence(string)
    if(sequence != -1){
      sequence + 1
    }else{
      "-1"
    }
  }

  def getSequence(string: String): String ={
    val charArray = string.toCharArray

    for( i <- 0 to charArray.length){
      if(charArray(i).isDigit){
        string.substring(i)
      }
    }

    "-1"

  }

  def getFolder(fileRoot: String): File = {
    val file = new File(fileRoot)
    if(!file.exists()) {
      file.mkdirs()
    }
    file
  }
  def getFile(fileRoot: String): File = {
    val file = new File(fileRoot)
    if(!file.exists()){
      file.createNewFile()
    }
    file
  }

}
