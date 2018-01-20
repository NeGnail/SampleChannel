package network

import akka.actor.ActorSystem
import com.typesafe.config.Config
import tool.StringTool

/**
  * Created by LiangEn.LiWei on 2017/3/6.
  */
class AkkaServer(private val config: Config) extends Server {
  def getConfig = config

  override def service(actorSystemName: String): ActorSystem = {
    if (!StringTool.isValid(actorSystemName)) {
      throw new AkkaServerException("the system of akka need a valid name !")
    }
    ActorSystem(actorSystemName,config)
  }
}
