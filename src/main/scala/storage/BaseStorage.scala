package storage

/**
  * Created by Administrator on 2017/4/16 0016.
  */
trait BaseStorage {
  def readAndDelete(topicName: String, i: Int, i1: Int): Message

  def store(msg: Message)

  def read(topicName: String,regionSequence: Int, offset: Int): Message

  def delete(topicName: String,regionSequence: Int)

  def delete(topicName: String,regionSequence: Int, offset: Int)
}
