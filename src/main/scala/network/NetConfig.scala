package network

import com.typesafe.config.{Config, ConfigFactory}


/**
  * Created by LiangEn.LiWei on 2017/3/1.
  */
class NetConfig {
  private val config: Config = ConfigFactory.load()

  def getConfig(key : String): Config ={
    return config.getConfig(key)
  }
}
