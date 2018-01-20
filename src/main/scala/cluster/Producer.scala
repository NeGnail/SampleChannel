package cluster

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import akka.actor.ActorSystem
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.ZooDefs.Ids
import tool.{FlexMQConstant, Serialization, StringTool, ZookeeperTool}

import scala.collection.mutable

/**
  * Created by Administrator on 2017/4/17 0017.
  */
class Producer extends Client{


  def createTopic(topicName: String,partitionCount: Int,copyCount: Int): Topic = {
    val backUps = new PartitionSelector(ZookeeperTool.getZookeeper.getChildren(StringTool.deleteLastChar(FlexMQConstant.ZookeeperBrokerRoot),false)).selectBackUps(partitionCount,copyCount)
    val topic = new SynchronizeTopic(topicName,backUps)
    topic.setRoute(new MinRoute)

    synchronizeBroker(topic,partitionCount,copyCount)
    synchronizeTopic(topic)


    new Topic(topic,this)

  }

  def synchronizeBroker(synchronizeTopic: SynchronizeTopic,partitionCount: Int,copyCount: Int) = {
    val backUps = synchronizeTopic.getBackUps


    val alredySynchronizeBroker = new mutable.HashSet[String]()
    backUps.foreach{
      backUp => {

        val allPartitions = backUp.getAllPartition()
        allPartitions.foreach{
          partition => {
            val brokerAddress = partition.getBrokerAddress
            if(alredySynchronizeBroker.contains(brokerAddress)){

            }else{
              val lastSynchronizeInfoBytes = ZookeeperTool.getZookeeper.getData(StringTool.createZookeeperBrokerPath(brokerAddress),false,null)

              var lastSynchronizeInfo: SynchronizeInfo = null
              var newSynchronizeInfo: SynchronizeInfo = null
              if(lastSynchronizeInfoBytes == null){
                newSynchronizeInfo = new SynchronizeInfo()
                newSynchronizeInfo.addTopic(synchronizeTopic)
              }else{
                lastSynchronizeInfo = Serialization.deSerialize(lastSynchronizeInfoBytes).asInstanceOf[SynchronizeInfo]
                newSynchronizeInfo = lastSynchronizeInfo.addTopic(synchronizeTopic)

              }
              System.out.println("生产者同步原信息到broker：" + newSynchronizeInfo)
              ZookeeperTool.getZookeeper.setData(StringTool.createZookeeperBrokerPath(brokerAddress),Serialization.serialize(newSynchronizeInfo),-1)
              alredySynchronizeBroker.add(brokerAddress)
            }
          }
        }
      }
    }
  }
  def synchronizeTopic(synchronizeTopic: SynchronizeTopic): Unit ={
    ZookeeperTool.getZookeeper.create(StringTool.createZookeeperTopicPath(synchronizeTopic.getName),Serialization.serialize(synchronizeTopic),Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL)
  }
}
