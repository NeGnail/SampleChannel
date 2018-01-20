package api

import cluster.Broker

/**
  * Created by liwei on 2017/4/28 0028.
  */
object BrokerStarter {
  def main(args: Array[String]): Unit = {
    val broker = new Broker()
    broker.sychronize()
  }
}
