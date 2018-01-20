package cluster

import tool.{FlexMQConstant, Serialization, StringTool, ZookeeperTool}

/**
  * Created by Administrator on 2017/4/27 0027.
  */
class Consumer extends Client{
  def createTopic(topicName: String): Topic = {
    val synchronizeTopic = Serialization.deSerialize(ZookeeperTool.getZookeeper.getData(StringTool.createZookeeperTopicPath(topicName),false,null)).asInstanceOf[SynchronizeTopic]
    new Topic(synchronizeTopic,this)
  }
}
