package cluster

import storage.Message

import scala.collection.mutable

/**
  * Created by LiangEn.LiWei on 2017/3/20.
  */
trait Partition {

  def getBroker(): Broker

  def getName(): String

  def synchronize()
  def sendData(partitions: mutable.HashSet[Partition],topicName: String)

  def receiveData(topicName: String)


  def store(msg: Message): Int

  def force()

  def read(offset: Int): Message

  def read(): Message

  def overdue()


}