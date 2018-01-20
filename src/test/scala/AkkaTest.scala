import java.util.concurrent.TimeUnit

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}
import network.AkkaServer
import org.junit.Test
import tool.{ConfigLoader, FlexMQConstant}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Created by Administrator on 2017/5/8 0008.
  */
class AkkaTest {
//  @Test
  def testAkkaRemote: Unit = {
    val actorSystem = ActorSystem("test1system",ConfigLoader.loadConfig("TestRemote").getConfig("akkaConfig"))
    actorSystem.actorOf(Props(new TestRemoteActor()),"sub1")
  }
  class TestRemoteActor extends Actor{
    override def receive: Receive = {
      case message: String => {
        System.out.println(message + "***********")
      }

    }
  }

  @Test
  def testAkkaLocal = {
    testAkkaRemote

    val actorSystem = ActorSystem("test1system",ConfigLoader.loadConfig("TestLocal").getConfig("akkaConfig"))
    val actorRef = Await.result(actorSystem.actorSelection("akka.tcp://test1system@127.0.0.1:6788/user/sub1").resolveOne(FiniteDuration.apply(5,TimeUnit.MINUTES)),Duration.Inf)
    actorRef ! "hi"

  }
  @Test
  def testLoadConfig = {
    System.out.print(ConfigLoader.loadConfig().getObject("akkaConfig").toConfig.getObject("akka").toConfig.getObject("remote").toConfig.getObject("netty.tcp").get("hostname").render())

  }
}
