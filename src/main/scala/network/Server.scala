package network

import akka.actor.ActorSystem

/**
  * Created by LiangEn.LiWei on 2017/3/6.
  */
trait Server {
  def service(actorSystemName: String): ActorSystem
}
