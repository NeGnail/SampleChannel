package cluster

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import akka.actor.ActorSystem

/**
  * Created by Administrator on 2017/5/31 0031.
  */
trait Client {
  private val isStartServer = new AtomicBoolean(false)
  private val actorSystem: AtomicReference[ActorSystem] = new AtomicReference[ActorSystem](null)

  def getIsStartServer = isStartServer
  def getActorSystem = actorSystem
}
