package cluster

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Address, Props}
import network.{AkkaConnection, AkkaConnectionArgument, AkkaServer}
import storage.Message
import tool.{ConfigLoader, FlexMQConstant}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by liwei
  */
class Topic(private val synchronizeTopic: SynchronizeTopic,private val client: Client){
  private val isStartServer = client.getIsStartServer
  private val actorSystem: AtomicReference[ActorSystem] = client.getActorSystem
  private val messageQueue = new LinkedBlockingQueue[Message]()

  def produce(msg: Message) = {
    msg.setTopicName(synchronizeTopic.getName)

    val synchronizeBackUp = synchronizeTopic.getRoute.routePartition(synchronizeTopic.getBackUps)
    val hostPartition = synchronizeBackUp.getHostPartition

    val actorRef = getActorRef(hostPartition)
    System.out.println("发送消息到:" + hostPartition.getPartitionName)
    actorRef ! msg
  }

  def consume(): Message = {
    val synchronizeBackUp = synchronizeTopic.getRoute.routePartition(synchronizeTopic.getBackUps)
    val hostPartition = synchronizeBackUp.getHostPartition
    val copyPartitions = synchronizeBackUp.getCopyPartitions

    if(isStartServer.compareAndSet(false,true)){
      startServer()
    }

    while(actorSystem.get() == null){

    }

    val pullActor = actorSystem.get().actorOf(Props(new pullActor()))
    try{
      pullActor ! hostPartition
    }catch {
      case e:Exception => {
        reliablePull(copyPartitions)
      }
    }

    messageQueue.take()
  }

  private def reliablePull(partitions: mutable.HashSet[SynchronizePartition]): Unit = {
    var successFlag = false

    partitions.foreach{
      partition => {
        try {
          val pullActor = actorSystem.get().actorOf(Props(new pullActor()))
          pullActor ! partition
          successFlag = true
        }catch {
          case e: Exception => {
            successFlag = false
          }
        }
      }
        if(successFlag){
          return
        }
    }
  }

  def getActorRef(synchronizePartition: SynchronizePartition): ActorRef = {
    if(isStartServer.compareAndSet(false,true)){
      startServer()
    }

    while(actorSystem.get() == null){

    }

    val akkaConnection = new AkkaConnection(synchronizePartition.getBrokerHost(), synchronizePartition.getBrokerPort().toInt, new AkkaConnectionArgument(FlexMQConstant.AkkaNetProtocol, FlexMQConstant.FilePartitionChannelAndBackUpReciveAkkaSystem), actorSystem.get())
    akkaConnection.connect("/user/" + synchronizePartition.getPartitionName)
  }

  private def startServer() = {
    val akkaServer = new AkkaServer(ConfigLoader.loadConfig().getConfig("akkaConfig"))
    actorSystem.set(akkaServer.service(FlexMQConstant.FilePartitionChannelAndBackUpReciveAkkaSystem))
  }


  private class pullActor extends Actor{
    override def receive: Receive = {
      case pullPartition: SynchronizePartition =>{
        val protocol = FlexMQConstant.AkkaNetProtocol
        val system = FlexMQConstant.FilePartitionChannelAndBackUpReciveAkkaSystem
        val pullPartitionAkkaPath = Address(protocol,system,pullPartition.getBrokerHost(),pullPartition.getBrokerPort().toInt).toString + "/user/" + pullPartition.getPartitionName

        Await.result(context.actorSelection(pullPartitionAkkaPath).resolveOne(FiniteDuration.apply(5,TimeUnit.MINUTES)),Duration.Inf) ! "pull@" + synchronizeTopic.getName

      }
      case massge: Message => {
        messageQueue.put(massge)
      }
    }
  }

  //  private class ActorReciver extends Actor{
  //
  //    def createPartitionAkkaPath(pullPartition: String): String = {
  //      ""
  //    }
  //
  //    override def receive: Receive = {
  //      case massge: Message => {
  //        messageQueue.put(massge)
  //      }
  //      case pullPartition: String =>{
  //        Await.result(context.actorSelection(createPartitionAkkaPath(pullPartition)).resolveOne(FiniteDuration.apply(5,TimeUnit.MINUTES)),Duration.Inf) ! "pull@" + "tea"
  //
  //
  //      }
  //    }
  //  }

}
