package cluster

import scala.collection.mutable

/**
  * Created by Administrator on 2017/4/16 0016.
  */
class SynchronizeInfo extends Serializable{
  private val topics = new mutable.HashSet[SynchronizeTopic]()

  def getTopics = topics
  def addTopic(synchronizeTopic: SynchronizeTopic): SynchronizeInfo ={
    topics.add(synchronizeTopic)
    this
  }
  def removeTopic(synchronizeTopic: SynchronizeTopic): Unit = {
    topics.remove(synchronizeTopic)
  }


  override def toString = s"SynchronizeInfo($topics, $getTopics)"
}
