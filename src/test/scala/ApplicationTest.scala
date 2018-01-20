import java.io.{File, FileOutputStream, FileWriter}

import api.BrokerStarter
import cluster.{Broker, Consumer, Producer, SynchronizeInfo}
import com.typesafe.config.ConfigFactory
import org.junit.Test
import storage.Message
import tool.{Serialization, ZookeeperTool}

/**
  * Created by Administrator on 2017/4/30 0030.
  */
class ApplicationTest {

  @Test
  def testMessage = {
    val message = new Message("test","this is a test msg !")
    val messageBytes = message.serialize()

    System.out.println(messageBytes.length)
    System.out.println(Message.deserialize(messageBytes).getData)


  }
//  @Test
  def testBroker = {
    val broker = new Broker()
    broker.sychronize()
    System.out.println("**************testBroker Before**************")
//    System.out.print(ConfigFactory.load("FlexMQConfig").getString("zookeeper.broker"))
    System.out.println(ZookeeperTool.getZookeeper.getChildren("/FlexMQ/Broker",false))
    System.out.println("***************testBroker After***************")

  }
  @Test
  def testOther = {
    System.out.println(ZookeeperTool.getZookeeper.getChildren("/",false))
    System.out.println(ZookeeperTool.getZookeeper.getChildren("/FlexMQ",false))

  }
  @Test
  def testProduce = {
    testBroker
    Thread.sleep(5000)
    val producer = new Producer()
    val topic = producer.createTopic("test",3,2)
    System.out.println(Serialization.deSerialize(ZookeeperTool.getZookeeper.getData("/FlexMQ/Broker/127.0.0.1:6797",false,null)))
    System.out.println(Serialization.deSerialize(ZookeeperTool.getZookeeper.getData("/FlexMQ/Topic/test",false,null)))

    Thread.sleep(5000)
    topic.produce(new Message("test","hi,it is a message"))
    System.out.println("************testProduce Before*************")

//    Thread.sleep(5000)
//    val consumer = new Consumer()
//    val topicConsumer = consumer.createTopic("test")
//    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + topicConsumer.consume())


    Thread.sleep(60*60*1000)

    //    val info = Serialization.deSerialize(ZookeeperTool.getZookeeper.getData("/FlexMQ/Broker/127.0.0.1:6588",false,null))
//    System.out.println(info)
//    val file = new File("G:\\work\\tempWork\\workprint")
//
//    val subFile = new File(file,"test")
//    if(!subFile.exists()){
//      subFile.mkdirs()
//    }
//    val out = new FileWriter(subFile)
//    out.write(info.asInstanceOf[SynchronizeInfo].getTopics.toList.apply(0).toString)
//    System.out.println("************testProduce After*************")
  }
  @Test
  def testBrokerMetadataRegister = {

    System.out.println("**************测试运行开始**************")
//    val broker = new Broker("127.0.0.1",6788)
//    broker.sychronize()
    System.out.println("**************测试运行结束**************")

    System.out.println("**************测试验证结束**************")
    System.out.println("从配置中心拉取到broker元数据：" + ZookeeperTool.getZookeeper.getChildren("/FlexMQ/Broker",false))
    System.out.println("**************测试验证结束**************")

    Thread.sleep(60*60*1000)
  }
  @Test
  def testParitionSelector = {
    System.out.println("**************准备测试环境开始**************")
    testBroker
    System.out.println("**************准备测试环境结束**************")

    Thread.sleep(5000)
    System.out.println("**************测试运行开始**************")
    val producer = new Producer()
    val topic = producer.createTopic("test",3,2)
    System.out.println("**************测试运行结束**************")

    System.out.println("**************测试验证开始**************")
    System.out.println("已经完成分区算法，从配置中心同步到当前Broker对分区信息的视图："+Serialization.deSerialize(ZookeeperTool.getZookeeper.getData("/FlexMQ/Broker/127.0.0.1:6788",false,null)))
    System.out.println("已经完成分区算法，从配置中心同步到topic对分区信息的视图："+Serialization.deSerialize(ZookeeperTool.getZookeeper.getData("/FlexMQ/Topic/test",false,null)))
    System.out.println("**************测试验证结束**************")

    Thread.sleep(5000)
  }

  @Test
  def testAll = {
    BrokerStarter.main(null)
    Thread.sleep(5000)

    val producer = new Producer()
    val topic = producer.createTopic("tea",3,2)
    topic.produce(new Message("green tea"))
    topic.produce(new Message("read tea"))
    topic.produce(new Message("blue tea"))
    Thread.sleep(5000)



    Thread.sleep(60*60*1000)
  }
}
