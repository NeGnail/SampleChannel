package tool

/**
  * Created by LiangEn.LiWei on 2017/3/7.
  */
object StringTool {
  def deleteQuotation(string: String) = {
    string.substring(1,string.length-1)
  }


  def isValid(string: String): Boolean = {
    string != null && !string.isEmpty
  }
  def deleteLastChar(string: String): String = {
    string.substring(0,string.length-1)
  }
  def getPartitionNameFromSquence(sequence: Int): String = {
    FlexMQConstant.PartitionPre + sequence
  }
  def createZookeeperBrokerPath(brokerAddress: String) = {
    FlexMQConstant.ZookeeperBrokerRoot + brokerAddress
  }
  def createZookeeperTopicPath(topicName: String) = {
    FlexMQConstant.ZookeeperTopicRoot + topicName
  }

}
