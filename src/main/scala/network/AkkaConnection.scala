package network

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Address}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by.LiWei
  */
class AkkaConnection(private val host: String, private val port: Int, private val akkaConnectionArgument: AkkaConnectionArgument, private val actorSystem: ActorSystem) extends Connection {

  def getHost = host
  def getPort = port
  def getAkkaConnectionArgument = akkaConnectionArgument
  def getActorSystem = actorSystem

  private def buildVisit(path: String): String = {
    val protocol = akkaConnectionArgument.getProtocol
    val system = akkaConnectionArgument.getSystem
    Address(protocol,system,host,port).toString + path
  }

  override def connect(path: String) : ActorRef = {
    val visit = buildVisit(path)
//    actorSystem.actorSelection(visit).resolveOne(FiniteDuration.apply(5,"MINUTES")).result(Duration.Inf)
    Await.result(actorSystem.actorSelection(visit).resolveOne(FiniteDuration.apply(5,TimeUnit.MINUTES)),Duration.Inf)
  }

  override def destroy() = {

  }

}
