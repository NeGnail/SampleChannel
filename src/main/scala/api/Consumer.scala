package api

import cluster.Topic

/**
  * Created by liwei on 2017/4/28 0028.
  */
class Consumer {
  private var topic:Topic = null
  def createTopic(topicName: String): Unit ={
    this.topic = new cluster.Consumer().createTopic(topicName)
  }
  def consume() = {
    topic.consume()
  }
}
