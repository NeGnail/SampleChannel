import java.io.{File, FileInputStream, FileOutputStream, RandomAccessFile}
import java.nio.channels.FileChannel.MapMode
import java.util.concurrent.locks.{ReentrantLock, ReentrantReadWriteLock}

import cluster.SynchronizePartition
import org.junit.Test
import storage.Message
import tool.{FlexMQConstant, Serialization}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Administrator on 2017/5/8 0008.
  */
class FileTest {

  @Test
  def testChannel: Unit = {
//    val mappedByteBuffer = new FileOutputStream("H:\\FlexMQTempWork\\test\\partition_43\\test\\region_1.txt").getChannel.map(MapMode.READ_WRITE, 0, 0)
    System.out.println("主程序开始执行")
//    val lock = new ReentrantLock()

    new Thread(new Runnable {
      override def run() = {
//        while (true){
//          System.out.println("wo shi 11111")
//        }
      }
//        lock.lock()
        System.out.println("开始写入")

        val fileChannel = new RandomAccessFile("H:\\FlexMQTempWork\\test\\region_1.txt","rw").getChannel
        val mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE,0,256)
        mappedByteBuffer.put("this is a test".getBytes())
        mappedByteBuffer.force()
        fileChannel.close()
        System.out.println("写入完成")
//        lock.unlock()
//      }
    }).start()

//      val file = new File("H:\\\\FlexMQTempWork\\\\storage\\\\partition_43\\\\test\\\\region_1.txt","rw")
//    System.out.print(file.getAbsolutePath)
//    System.out.println("")
//    System.out.print(file.getPath)
    new Thread(new Runnable {
      override def run() = {
//        while (true){
//          System.out.println("wo shi 22222")
//        }
        while(true) {
          //          lock.lock()
          System.out.println("读取ing")
          val mappedByteBufferRead = new FileInputStream("H:\\FlexMQTempWork\\test\\region_1.txt").getChannel.map(MapMode.READ_ONLY, 0, FlexMQConstant.MaxMessageSize)
//          mappedByteBufferRead.load()
//          mappedByteBufferRead.
//          mappedByteBufferRead.flip()

//          val messageArrayByte = new Array[Byte](FlexMQConstant.MaxMessageSize)
          System.out.println(mappedByteBufferRead.getChar(0))
          System.out.println(mappedByteBufferRead.getChar(1))
          System.out.println(mappedByteBufferRead.getChar(2))
          //          lock.unlock()
        }
      }
    }).start()
    System.out.println("主程序执行完毕")


    Thread.sleep(1000000)
  }

  @Test
  def testEquels: Unit = {
    val a = new SynchronizePartition("11","aaa")
    val b = new SynchronizePartition("11","aaa")
    System.out.println(a.hashCode())
    System.out.println(b.hashCode())
    System.out.println(a.toString)
    System.out.println(b.toString)
    System.out.println(a.equals(b))

  }

  @Test
  def testFileLength: Unit = {
    System.out.println(new File("H:\\FlexMQTempWork\\test\\region_1.txt").length())
  }

  @Test
  def testDeserialize: Unit = {
    var bytes = Serialization.serialize("##")
    var byteBuffer = new ArrayBuffer[Byte](1000)
    byteBuffer ++= bytes ++= "hi".getBytes()
    byteBuffer.trimEnd(2)
    System.out.print(Serialization.deSerialize(byteBuffer.toArray))

  }

  @Test
  def testBytes: Unit = {
//    System.out.println("#".getBytes().length)
//    System.out.println("##".getBytes().length)
//    System.out.println("###".getBytes().length)
//    System.out.println("####".getBytes().length)
//      val message = new Message("test","hi")
//      System.out.println("************"+Serialization.serialize(message).length)

//    message.test1()
//    System.out.println(Serialization.serialize(message).length)

  }

  @Test
  def testSub: Unit = {
    val string = "12345678"
    System.out.println(string.lastIndexOf("6"))
    System.out.println(string.dropRight(3))

  }


}
