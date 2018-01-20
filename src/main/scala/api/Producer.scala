package api

import cluster.Topic
import storage.Message

/**
  * Created by liwei on 2017/4/28 0028.
  */
class Producer {
  private var topic:Topic = null
  def createTopic(topicName: String,partitionCount: Int,copyCount: Int): Unit ={
    this.topic = new cluster.Producer().createTopic(topicName,partitionCount,copyCount)
  }
  def produce(msg: Message) = {
    topic.produce(msg)
  }
}
