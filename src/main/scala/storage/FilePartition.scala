package storage

import java.util.Date
import java.util.concurrent.{ConcurrentHashMap, ExecutorService, ScheduledExecutorService, TimeUnit}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.locks.ReentrantLock

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import cluster.{Broker, MessageContext, SynchronizePartition}
import network.{AkkaConnection, AkkaConnectionArgument, AkkaServer}
import tool.{ConfigLoader, FlexMQConstant}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by LiangEn.LiWei on 2017/3/7.
  */
class FilePartition(private val name: String, private val storage: BaseStorage, private val broker: Broker, private val excuter: ExecutorService) {
  private val storageLock = new ReentrantLock()

  private val actorSystem: AtomicReference[ActorSystem] = new AtomicReference[ActorSystem](broker.getActorSystem)

  private val isStartSentFlag: AtomicBoolean = new AtomicBoolean(false)
  private val isStartReciveFlag: AtomicBoolean = new AtomicBoolean(false)

  private val channelTopicRead = new ConcurrentHashMap[String, MessageContext]()

  private val ReciveAkkaSystemPath = name

  def getName = name

  def getBroker = broker

  def isStartSent(): Boolean = {
    isStartSentFlag.get()
  }

  def isStartRecive(): Boolean = {
    isStartReciveFlag.get()
  }

  //  override def synchronize(): Unit = {
  //    ZookeeperClient.getZookeeper.getData(BrokerAddressMapper.partitionLocalToZk(this), new SynchronizeWatcher(), null)
  //  }
  //
  //  override def store(msg: Message): Int = ???
  //
  //  override def force(): Unit = ???
  //
  //  override def read(offset: Int): Message = ???
  //
  //  override def read(): Message = ???
  //
  //  override def overdue(): Unit = ???
  //
  //  private class SynchronizeWatcher extends Watcher {
  //
  //    def isHost(backUp: BackUp): Boolean = {
  //      backUp.getHost == this
  //    }
  //
  //    def isFollow(backUp: BackUp): Boolean = {
  //      backUp.getFollows.contains(FilePartition.this)
  //    }

  //    override def process(event: WatchedEvent): Unit = {
  //      if (EventType.NodeDataChanged == event.getType) {
  //        val data = ZookeeperClient.getZookeeper.getData(BrokerAddressMapper.partitionLocalToZk(FilePartition.this), new SynchronizeWatcher(), null)
  //        if (Serialization.deSerialize(data).isInstanceOf[BackUp]) {
  //          if (isHost(data.asInstanceOf[BackUp])) {
  //            val host = data.asInstanceOf[BackUp].getHost
  //            val follows = data.asInstanceOf[BackUp].getFollows
  //            host.sendData(follows,data.asInstanceOf[BackUp].getTopicName)
  //          } else if (isFollow(data.asInstanceOf[BackUp])) {
  //            receiveData(data.asInstanceOf[BackUp].getTopicName)
  //          }
  //        }
  //      }
  //      synchronize()
  //    }
  //  }

  def sendData(partitions: mutable.HashSet[SynchronizePartition], topicName: String): Unit = {
    System.out.println(ReciveAkkaSystemPath + " 正在向其他actor同步数据")
    receiveData(topicName)
    excuter.submit(new Runnable() {


      override def run(): Unit = {


        var flag = true

        var regionSequence = 1
        var offset = 0

        var msg: Message = null
        while (true) {
          try {
            msg = storage.read(topicName, regionSequence, offset)
            flag = true
          } catch {
            case e: FileStorageExceedValidLengthException => {
              regionSequence += 1
              offset = 0
              flag = false
            }
            case e: FileStorageNotExistException => {
              flag = false
            }
            case e: FileStorageRegionLengthNotEnoughException => {
              flag = false
            }
          }
          if (flag) {
            offset += 1
          }
          if (msg != null) {
            partitions.foreach {
              x =>
                val actorRef = getActorRef(x)
                actorRef ! msg
                System.out.println(ReciveAkkaSystemPath + " 已经同步了一个数据出去：" + msg.getData)
            }
            msg = null
          }
          cycle(FlexMQConstant.FilePartitionBackUpCycleTime)
        }
      }
    })


  }

  def cycle(cycleTime: Int): Unit = {
    var cycleTimeLong = cycleTime.toLong
    while (cycleTimeLong > 0) {
      val preTime = System.currentTimeMillis()
      Thread.`yield`()
      val afterTime = System.currentTimeMillis()
      cycleTimeLong = cycleTimeLong - afterTime + preTime
    }
  }

  def getActorRef(synchronizePartition: SynchronizePartition): ActorRef = {
    val host = synchronizePartition.getBrokerHost()
    val port = synchronizePartition.getBrokerPort()
    val akkaConnectionArgument = new AkkaConnectionArgument(FlexMQConstant.AkkaNetProtocol, FlexMQConstant.FilePartitionChannelAndBackUpReciveAkkaSystem)

    val akkaConnection = new AkkaConnection(host, port.toInt, akkaConnectionArgument, actorSystem.get())
    akkaConnection.connect("/user/" + synchronizePartition.getPartitionName)
  }

  private def startServer() = {


  }

  def receiveData(topicName: String): Unit = {
    if(isStartReciveFlag.compareAndSet(false,true)){
      actorSystem.get().actorOf(Props(new ActorReciver()), ReciveAkkaSystemPath)
      System.out.println(ReciveAkkaSystemPath + "actor已经存在于akka系统中，可以接收数据")
    }
  }

  private class ActorReciver extends Actor {

    def receive: Receive = {
      case massge: Message => {
        System.out.println(ReciveAkkaSystemPath  + "接收到了一个数据：" + massge.getData)
        storage.store(massge)
      }
      case pull: String => {
        if (pull.startsWith("pull@")) {
          System.out.println(ReciveAkkaSystemPath  + "接收到了一个拉取请求：" + pull)
          val topicName = pull.split("@").apply(1)
          var messageContext = channelTopicRead.get(topicName)
          if (messageContext == null) {
            messageContext = new MessageContext()
            messageContext.getRegionSequence.incrementAndGet()
            channelTopicRead.put(topicName, messageContext)
          }
          var regionSequence = messageContext.getRegionSequence.get()
          var offset = messageContext.getOffset.get()


          var isSuccess = false
          var msg: Message = null
          while (!isSuccess) {
            try {
              msg = storage.read(topicName, regionSequence, offset)
              isSuccess = true
            } catch {
              case e: FileStorageExceedValidLengthException => {
                regionSequence += 1
                offset = 0
                isSuccess = false
              }
              case e: FileStorageNotExistException => {
                isSuccess = false
              }
              case e: FileStorageRegionLengthNotEnoughException => {
                isSuccess = false
              }
            }
            if (isSuccess) {
              offset += 1
              messageContext.getRegionSequence.set(regionSequence)
              messageContext.getOffset.set(offset)

              context.sender() ! msg
              System.out.println(ReciveAkkaSystemPath  + "已经为拉取消息的请求发送了一个消息：" + msg.getData)

              //              Await.result(context.actorSelection("akka.tcp://FilePartitionChannelAndBackUpReciveAkkaSystem@127.0.0.1:9890/user/tea").resolveOne(FiniteDuration.apply(5,TimeUnit.MINUTES)),Duration.Inf) ! "hi ,wo lai le"
              //              System.out.println(ReciveAkkaSystemPath  + "已经为拉取消息的请求发送了一个消息：" + msg.getData)

            }

          }
        }
      }
    }
  }

}

