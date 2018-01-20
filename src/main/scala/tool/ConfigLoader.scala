package tool

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by LiangEn.LiWei on 2017/3/2.
  */
object ConfigLoader {

  def loadConfig(path: String): Config = {
    return ConfigFactory.load(path)
  }
  def loadConfig(): Config = {
    return ConfigFactory.load()
  }
}
