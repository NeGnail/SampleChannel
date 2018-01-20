package network

import akka.actor.ActorRef

/**
  * Created by LiangEn.LiWei on 2017/3/3.
  */
trait Connection {
  def connect(path: String) : ActorRef
  def destroy()

}
