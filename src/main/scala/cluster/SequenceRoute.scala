package cluster

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable

/**
  * Created by Administrator on 2017/4/28 0028.
  */
class SequenceRoute extends Route with Serializable{
  private val sequence = new AtomicInteger(0)
  override def routePartition(synchronizeBackUps: mutable.HashSet[SynchronizeBackUp]): SynchronizeBackUp = {
    synchronizeBackUps.toList.apply(sequence.getAndIncrement())
  }
}
