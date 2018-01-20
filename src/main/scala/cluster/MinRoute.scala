package cluster

import scala.collection.mutable

/**
  * Created by Administrator on 2017/5/28 0028.
  */
class MinRoute extends Route with Serializable{
  override def routePartition(synchronizeBackUps: mutable.HashSet[SynchronizeBackUp]): SynchronizeBackUp = {
    var minSynchronizeBackUp = synchronizeBackUps.toList.apply(0)
    synchronizeBackUps.foreach{
      synchronizeBackUp => {
        if(synchronizeBackUp.getHostPartition.getPartitionName.compareTo(minSynchronizeBackUp.getHostPartition.getPartitionName)<0){
          minSynchronizeBackUp = synchronizeBackUp
        }
      }
    }
    minSynchronizeBackUp
  }
}
