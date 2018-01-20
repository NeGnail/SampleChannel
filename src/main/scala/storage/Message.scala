package storage

import tool.{FlexMQConstant, Serialization}

import scala.collection.mutable.ArrayBuffer
/**
  * Created by Administrator on 2017/4/16 0016.
  */
class Message(private val data: Any) extends Serializable{

  private var topicName: String = null
  def setTopicName(topicName: String) = {
    this.topicName = topicName
  }

  def getTopicName = topicName
  def getData = data

  //  def size() : Int = {
  //    null
  //  }
  def serialize() : Array[Byte] = {
    val resultBytes = new ArrayBuffer[Byte]()

    val origineMessageByte = Serialization.serialize(this)
    if(origineMessageByte.size > FlexMQConstant.MaxMessageSize){
      throw new MessageException("the size of message super max size")
    }else {
      val fill = FlexMQConstant.MaxMessageSize - origineMessageByte.size
      resultBytes ++= origineMessageByte ++= fillByte(fill)
    }

    resultBytes.toArray
  }

  private def fillByte(count: Int) : Array[Byte] = {
    new Array[Byte](count)
  }


  //  override def toString = s"Message($fillArray, $topicName, $data, $getTopicName, $getData, $serialize)"
}
object Message{
  private val fillFlag = "#"
  val maxSize = FlexMQConstant.MaxMessageSize

  def messageCountToByteSize(count: Int) : Int = {
    count * maxSize
  }
  def deserialize(bytes: Array[Byte]): Message = {
    Serialization.deSerialize(bytes).asInstanceOf[Message]



    //    System.out.println("反序列临时数据开始")
    //
    //    val fillByteArray = bytes
    //    val originByteArray = new ArrayBuffer[Byte]()
    //
    //    var index = 0
    //    System.out.println("反序列临时数据开始11111")
    //
    //    while(index <= fillByteArray.length-1){
    //      System.out.println("反序列临时数据开始22222")
    //
    //      val tempByteArrayfillByteArray = new ArrayBuffer[Byte]()
    //      tempByteArrayfillByteArray += fillByteArray.apply(index) += fillByteArray.apply(index+1)
    //      System.out.println("反序列临时数据开始33333")
    //      System.out.println("反序列临时数据的条件打印：" + Serialization.deSerialize(tempByteArrayfillByteArray.toArray).isInstanceOf[String])
    //      if(Serialization.deSerialize(tempByteArrayfillByteArray.toArray).isInstanceOf[String] && Serialization.deSerialize(tempByteArrayfillByteArray.toArray).asInstanceOf[String].equals("##")){
    //          System.out.print("反序列临时数据："+Serialization.deSerialize(tempByteArrayfillByteArray.toArray))
    //        return Serialization.deSerialize(originByteArray.toArray).asInstanceOf[Message]
    //      }
    //
    //      originByteArray ++= tempByteArrayfillByteArray
    //      index += 2
    //    }
    //  null
    //    return Serialization.deSerialize(originByteArray.toArray).asInstanceOf[Message]
  }

}
