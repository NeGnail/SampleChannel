package cluster

import java.util.Random
import java.util.List

import tool.{FlexMQConstant, StringTool, ZookeeperTool}

import scala.collection.mutable

/**
  * Created by Administrator on 2017/4/17 0017.
  */
class PartitionSelector(private val brokers: List[String]) {

  def selectPartition(partitionCount: Int): mutable.HashSet[SynchronizePartition] = {
    val partitions = new mutable.HashSet[SynchronizePartition]()

    val random = new Random()

    val brokerAddressNoRepeat = new mutable.HashSet[String]()
    if(partitionCount < brokers.size){
      for(i <- 1 to partitionCount){

        var brokerAddress: String = null
        brokerAddress = brokers.get(random.nextInt(brokers.size))
        while(brokerAddressNoRepeat.contains(brokerAddress)){
          brokerAddress = brokers.get(random.nextInt(brokers.size))
        }
        brokerAddressNoRepeat.add(brokerAddress)

        val partitionName = StringTool.getPartitionNameFromSquence(random.nextInt(FlexMQConstant.PartitionCount))

        partitions.add(new SynchronizePartition(brokerAddress,partitionName))
      }
    }else if(partitionCount == brokers.size){
      for(i <- 1 to partitionCount){
        val brokerAddress = brokers.get(i-1)
        val partitionName = StringTool.getPartitionNameFromSquence(random.nextInt(FlexMQConstant.PartitionCount))

        partitions.add(new SynchronizePartition(brokerAddress,partitionName))
      }
    }else{
      for(i <- 1 to partitionCount){
        val brokerAddress = brokers.get(random.nextInt(brokers.size))
        var partitionName = StringTool.getPartitionNameFromSquence(random.nextInt(FlexMQConstant.PartitionCount))
        var synchronizePartition = new SynchronizePartition(brokerAddress,partitionName)

        while(partitions.contains(synchronizePartition)){
          partitionName = StringTool.getPartitionNameFromSquence(random.nextInt(FlexMQConstant.PartitionCount))
          synchronizePartition = new SynchronizePartition(brokerAddress,partitionName)
        }
        partitions.add(synchronizePartition)
      }
    }

    partitions
  }


  def selectBackUps(hostCount: Int, backUpCount: Int): mutable.HashSet[SynchronizeBackUp] = {
    if(hostCount * backUpCount > ZookeeperTool.getZookeeper.getChildren(StringTool.deleteLastChar(FlexMQConstant.ZookeeperBrokerRoot),false,null).size() * FlexMQConstant.PartitionCount){
      throw new PartitionSelectorException("the partitions count of need super partition count of current")
    }
    val backUps = new mutable.HashSet[SynchronizeBackUp]()

    val partitions = selectPartition(hostCount * (backUpCount+1)).toList
    for(i <- 1 to hostCount){
      val hostPartition = partitions.apply((i-1) * (1 + backUpCount))
      val copyPartitions = new mutable.HashSet[SynchronizePartition]()
      for(j <- 1 to backUpCount){
        copyPartitions.add(partitions.apply((i-1) * (1 + backUpCount) + j))
      }
      backUps.add(new SynchronizeBackUp(hostPartition,copyPartitions))
    }

    backUps
  }

  def selectPartition(): Unit ={

  }
}
