package cluster

import scala.collection.mutable

/**
  * Created by liwei
  */
class SynchronizeBackUp(private val hostPartition: SynchronizePartition, private val copyPartitions: mutable.HashSet[SynchronizePartition]) extends Serializable{



  def getHostPartition = hostPartition
  def getCopyPartitions = copyPartitions

  def addPartition(sychronizePartition: SynchronizePartition) = {
    copyPartitions.add(sychronizePartition)
  }
  def removePartition(sychronizePartition: SynchronizePartition) = {
    copyPartitions.remove(sychronizePartition)
  }

  def isHost(broker: Broker): Boolean = {
    hostPartition.getBrokerAddress.equals(broker.getHost + ":" + broker.getPort)
  }
  def isCopy(broker: Broker): Boolean = {
    copyPartitions.foreach{
      copyPartition => {
        if(copyPartition.getBrokerAddress.equals(broker.getHost + ":" + broker.getPort)){
          return true
        }
      }
    }
    false
  }
  def isHostAndCopy(broker: Broker): Boolean = {
    isHost(broker) && isCopy(broker)
  }
  def getRelatedCopyPartition(broker: Broker): mutable.HashSet[SynchronizePartition] = {
    val synchronizePartitions = new mutable.HashSet[SynchronizePartition]()

    copyPartitions.foreach{
      copyPartition => {
        if(copyPartition.getBrokerAddress == broker.getHost + ":" + broker.getPort){
          synchronizePartitions.add(copyPartition)
        }
      }
    }

    synchronizePartitions
  }

  def getAllPartition(): mutable.HashSet[SynchronizePartition] = {
    val synchronizePartitions = new mutable.HashSet[SynchronizePartition]()

    synchronizePartitions.add(hostPartition)
    synchronizePartitions ++ copyPartitions


  }

  override def toString = s"SynchronizeBackUp($hostPartition, $copyPartitions, $getHostPartition, $getCopyPartitions, $getAllPartition)"
}
