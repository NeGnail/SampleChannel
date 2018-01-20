package cluster

import java.util.concurrent.Executors

import network.AkkaServer
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.{CreateMode, WatchedEvent, Watcher}
import storage.FilePartition
import tool._

import scala.collection.mutable

/**
  * Created by LiangEn.LiWei on 2017/3/7.
  */
class Broker extends Watcher {
  private val host: String  = StringTool.deleteQuotation(ConfigLoader.loadConfig().getObject("akkaConfig").toConfig.getObject("akka").toConfig.getObject("remote").toConfig.getObject("netty.tcp").get("hostname").render())
  private val port: Int = ConfigLoader.loadConfig().getObject("akkaConfig").toConfig.getObject("akka").toConfig.getObject("remote").toConfig.getObject("netty.tcp").get("port").render().toInt

  private val zookeeperPath = ZookeeperTool.buildZookeeperPath(this)
  private val synchronizedTopic = new mutable.HashSet[SynchronizeTopic]()
  private val excuter = Executors.newFixedThreadPool(FlexMQConstant.BrokerThreadCount)
  private val actorSystem = new AkkaServer(ConfigLoader.loadConfig().getConfig("akkaConfig")).service(FlexMQConstant.FilePartitionChannelAndBackUpReciveAkkaSystem)


  def getHost = host

  def getPort = port

  def getZookeeperPath = zookeeperPath

  def getActorSystem = actorSystem

  private val partitions = new mutable.HashSet[FilePartition]()



  def addPartition(partition: FilePartition) = {
    partitions + partition
  }

  def removePartition(partition: FilePartition) = {
    partitions - partition

  }

  def sychronize() = {
    sychronizeBroker()
    sychronizePartition()
  }

  private def sychronizeBroker() = {
    ZookeeperTool.getZookeeper.create(zookeeperPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
    System.out.println(this.getHost + ":" + this.getPort + " 已将自己注册到Zookeeper")
  }

  private def sychronizePartition() = {
    ZookeeperTool.getZookeeper.getData(zookeeperPath, this,null)
  }

  override def process(event: WatchedEvent): Unit = {
    if(event.getType == EventType.NodeDataChanged){
      val synchronizeInfo = Serialization.deSerialize(ZookeeperTool.getZookeeper.getData(zookeeperPath,false,null))
      val topics = synchronizeInfo.asInstanceOf[SynchronizeInfo].getTopics


      topics.foreach{
        topic => {


          if(!synchronizedTopic.contains(topic)){

/*打印分区元数据变化start*/
            System.out.println(this.getHost + ":" + this.getPort + " 发现自己的分区元数据发生了变化，以下是目前的拓扑信息：")
            topic.getBackUps.foreach{
              backUp => {
                System.out.println("host:" + backUp.getHostPartition.getPartitionName + "======>")
                backUp.getCopyPartitions.foreach{
                  copyPartition => {
                    System.out.println(copyPartition.getPartitionName + ",")
                  }
                }

              }
            }
/*打印分区元数据变化end*/



            val backUps = topic.getBackUps
            backUps.foreach{
              backUp => {
                if(backUp.isHostAndCopy(this)){
                  val hostSynchronizePartition = backUp.getHostPartition
                  val copySynchronizePartitions = backUp.getRelatedCopyPartition(this)

                  val hostFilePartition = createFilePartitionIfNoExist(hostSynchronizePartition)
                  copySynchronizePartitions.foreach{
                    copySynchronizePartition => {
                      val copyFilePartition = createFilePartitionIfNoExist(copySynchronizePartition)
                      copyFilePartition.receiveData(topic.getName)
                    }
                  }
                  hostFilePartition.sendData(backUp.getCopyPartitions,topic.getName)

                }else if(backUp.isHost(this)){
                  val synchronizePartition = backUp.getHostPartition
                  val filePartition = createFilePartitionIfNoExist(synchronizePartition)
                  if(backUp.getCopyPartitions.nonEmpty){
                    filePartition.sendData(backUp.getCopyPartitions,topic.getName)
                  }
                }else if(backUp.isCopy(this)){
                  val synchronizePartitions = backUp.getRelatedCopyPartition(this)
                  synchronizePartitions.foreach{
                    synchronizePartition => {
                      val filePartition = createFilePartitionIfNoExist(synchronizePartition)
                      filePartition.receiveData(topic.getName)
                    }
                  }
                }
              }
            }
            synchronizedTopic.add(topic)
          }
        }
      }
    }
    sychronizePartition()
  }

  def isAlreadyStartSent(synchronizePartition: SynchronizePartition,topicName: String): Boolean ={
    val filePartition = getFilePartitionFromSynchronizePartition(synchronizePartition)
    filePartition != null && filePartition.isStartSent()
  }
  def isAlreadyStartRecive(synchronizePartition: SynchronizePartition,topickName: String): Boolean = {
    val filePartition = getFilePartitionFromSynchronizePartition(synchronizePartition)
    filePartition != null && filePartition.isStartRecive()
  }

  def getFilePartitionFromSynchronizePartition(synchronizePartition: SynchronizePartition): FilePartition = {
    var partition: FilePartition = null
    partitions.foreach{
      x => {
        if(synchronizePartition.getPartitionName == x.getName){
          partition = x
        }
      }
    }
    partition
  }

  def createFilePartitionIfNoExist(synchronizePartition: SynchronizePartition): FilePartition = {
    var filePartition = getFilePartitionFromSynchronizePartition(synchronizePartition)
    if (filePartition == null){
      filePartition = new FilePartitionFactory().createFilePartition(synchronizePartition.getPartitionName,this,excuter)
      partitions.add(filePartition)
    }
    filePartition
  }

  class backUpStartedFlag(private val hsot: SynchronizePartition,private val copys: mutable.HashSet[SynchronizePartition]){
    def getHost = host
    def getCopys = copys

  }
}


