package cluster

import scala.collection.mutable

/**
  * Created by Administrator on 2017/4/16 0016.
  */
class SynchronizeTopic(private val name: String,private val backUps: mutable.HashSet[SynchronizeBackUp]) extends Serializable{

  private var route: Route = null

  def getBackUps = backUps
  def getName = name

  def setRoute(route: Route): Unit ={
    this.route = route
  }
  def getRoute = route

  def addBackUp(sychronizeBackUp: SynchronizeBackUp): Unit ={
    backUps.add(sychronizeBackUp)
  }
  def removeBackUp(sychronizeBackUp: SynchronizeBackUp): Unit ={
    backUps.remove(sychronizeBackUp)
  }

  override def toString = s"SynchronizeTopic($route, $name, $backUps, $getBackUps, $getName, $getRoute)"
}
