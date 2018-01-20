package cluster

import scala.collection.mutable

/**
  * Created by Administrator on 2017/4/28 0028.
  */
trait Route {
  def routePartition(synchronizeBackUps: mutable.HashSet[SynchronizeBackUp]): SynchronizeBackUp
}
