package cluster

/**
  * Created by Administrator on 2017/4/16 0016.
  */
class SynchronizePartition(private val brokerAddress: String, private val partitionName: String) extends Serializable{
  def getBrokerAddress = brokerAddress

  def getPartitionName = partitionName

  def getBrokerHost(): String = {
    brokerAddress.split(":").apply(0)
  }

  def getBrokerPort(): String = {
    brokerAddress.split(":").apply(1)
  }


  def canEqual(other: Any): Boolean = other.isInstanceOf[SynchronizePartition]

  override def equals(other: Any): Boolean = other match {
    case that: SynchronizePartition =>
      (that canEqual this) &&
        brokerAddress == that.brokerAddress &&
        partitionName == that.partitionName
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(brokerAddress, partitionName)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }


  override def toString = s"SynchronizePartition($brokerAddress, $partitionName, $getBrokerAddress, $getPartitionName, $getBrokerHost, $getBrokerPort, $hashCode)"
}
