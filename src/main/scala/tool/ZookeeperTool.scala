package tool

import java.util.concurrent.CountDownLatch

import cluster.{Broker, BrokerException}
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.{CreateMode, WatchedEvent, Watcher, ZooKeeper}

/**
  * Created by Administrator on 2017/4/16 0016.
  */
object ZookeeperTool {
  private val zooKeeper = new ZooKeeper(FlexMQConstant.ZookeeperAddress, FlexMQConstant.BrokerSynchronizeTimeOut, new initWatcher())
  private val countDownLatch = new CountDownLatch(1)
  private class initWatcher extends Watcher {
    override def process(event: WatchedEvent): Unit = {
      countDownLatch.countDown()
      if(zooKeeper.getChildren("/",false,null).size() == 0){
        zooKeeper.create("/FlexMQ",null,Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT)
      }
      if(!zooKeeper.getChildren("/FlexMQ",false,null).contains("Broker")){
        zooKeeper.create("/FlexMQ/Broker",null,Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT)
      }
      if(!zooKeeper.getChildren("/FlexMQ",false,null).contains("Topic")){
        zooKeeper.create("/FlexMQ/Topic",null,Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT)
      }
    }
  }

  def getZookeeper = {
    countDownLatch.await()
    zooKeeper
  }

  def buildZookeeperPath(broker: Broker): String = {
    if (broker == null || !StringTool.isValid(broker.getHost) || broker.getPort == 0) {
      throw new BrokerException("Broker is illegal")
    }
    FlexMQConstant.ZookeeperBrokerRoot + broker.getHost + ":" + broker.getPort
  }

}
