package tool

import java.io.File

/**
  * Created by Administrator on 2017/4/16 0016.
  */
object FlexMQConstant {


  /*自定义变量*/
  private val FileSeparator = File.separator


  private val zookeeperSeparator = "/"
  /*配置文件地址*/
  val FlexMQConfigPath = "FlexMQConfig"
  /*zookeeper*/
  val ZookeeperAddress = ConfigLoader.loadConfig(FlexMQConfigPath).getString("zookeeper.address")



  val ZookeeperFlexMQRoot = zookeeperSeparator + "FlexMQ" + zookeeperSeparator
  val ZookeeperBrokerRoot = ZookeeperFlexMQRoot + "Broker" + zookeeperSeparator
  val ZookeeperTopicRoot = ZookeeperFlexMQRoot + "Topic" + zookeeperSeparator
  /*akka*/
  val FilePartitionBackUpAkkaSystem = "FilePartitionBackUpAkkaSystem"
  val FilePartitionChannelAkkaSystem = "FilePartitionChannelAkkaSystem"
  val FilePartitionChannelAndBackUpReciveAkkaSystem = "FilePartitionChannelAndBackUpReciveAkkaSystem"
  val AkkaNetProtocol = ConfigLoader.loadConfig(FlexMQConfigPath).getString("akka.net.protocol")

  /*broker*/
  val BrokerRoot = ConfigLoader.loadConfig(FlexMQConfigPath).getString("broker.root")
  val BrokerSynchronizeTimeOut = 5000
  val PartitionCount = ConfigLoader.loadConfig(FlexMQConfigPath).getInt("partition.count")
  val BrokerThreadCount = ConfigLoader.loadConfig(FlexMQConfigPath).getInt("broker.thread.count")
  /*storage*/
  val StorageRoot = BrokerRoot + "storage" + FileSeparator
  /*partition*/
  val PartitionPre = "partition_"
  val FilePartitionBackUpCycleTime = ConfigLoader.loadConfig(FlexMQConfigPath).getInt("filePartition.backUp.cycleTime")
  /*region*/
  val FileStorageRegionMaxSize = 500*1024*1024
  val FileStorageRegionPre = "region_"
  /*message*/
  val MaxMessageSize = ConfigLoader.loadConfig(FlexMQConfigPath).getInt("max.message.size")

}
