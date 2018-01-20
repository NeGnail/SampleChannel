package cluster

import java.io.File
import java.util.concurrent.{ExecutorService, ScheduledExecutorService}

import storage.{BaseFileDefaultStorage, BaseFileMemoryMappingStorage, FilePartition}
import tool.FlexMQConstant


/**
  * Created by hp on 2017/3/25.
  */
class FilePartitionFactory {
  def createFilePartition(partitionName: String,broker: Broker,excuter: ExecutorService) : FilePartition = {
    val storage = new BaseFileDefaultStorage(FlexMQConstant.StorageRoot + partitionName + File.separator)
    new FilePartition(partitionName,storage,broker,excuter)
  }
}
