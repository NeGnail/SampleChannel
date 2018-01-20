package storage

import java.io.{File, FileInputStream, FileOutputStream, RandomAccessFile}
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel.MapMode
import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import tool.{FileTool, FlexMQConstant}

/**
  * Created by liwei
  */
class BaseFileMemoryMappingStorage(private val partitionFileRoot: String) extends BaseStorage{
  private val lock = new ReentrantReadWriteLock()
  private val RegionSuffix = ".txt"

  def getPartitionFileRoot = partitionFileRoot


  def createNewTopicIfNoExist(msg: Message) = {
    val file = new File(partitionFileRoot + msg.getTopicName)
    if(!file.exists()){
      file.mkdirs()
    }
  }

  override def store(msg: Message): Unit = {
    lock.writeLock().lock()
    createNewTopicIfNoExist(msg)

    var lastRegion = getLastRegion(msg.getTopicName)
    if(lastRegion == null){
      lastRegion = createNewRegion(msg.getTopicName)
    }
    var mappedByteBuffer: MappedByteBuffer = null

    if (lastRegion.length() + FlexMQConstant.MaxMessageSize < FlexMQConstant.FileStorageRegionMaxSize) {
      mappedByteBuffer = new RandomAccessFile(lastRegion,"rw").getChannel.map(MapMode.READ_WRITE, 0, FlexMQConstant.MaxMessageSize)
    } else {
      val newRegion = createNewRegion(msg.getTopicName)
      mappedByteBuffer = new RandomAccessFile(newRegion,"rw").getChannel.map(MapMode.READ_WRITE, 0, FlexMQConstant.MaxMessageSize)
    }

    mappedByteBuffer.put(msg.serialize())
    mappedByteBuffer.force()
    System.out.println("已经存入数据:" + msg)
    lock.writeLock().unlock()
  }

  override def read(topicName: String,regionSequence: Int, offset: Int): Message = {
    lock.readLock().lock()
    val region = getRegion(topicName,regionSequence.toString)
    if(region == null){
      throw new FileStorageNotExistException("this region of regionSequence is not exist")
    }
    if(offset + FlexMQConstant.MaxMessageSize > FlexMQConstant.FileStorageRegionMaxSize){
      throw new FileStorageExceedValidLengthException("offset exceed valid length")
    }
    if(offset + FlexMQConstant.MaxMessageSize > region.length()){
      throw new FileStorageRegionLengthNotEnoughException("region length is not enough to this offset")
    }

    val mappedByteBuffer = new FileInputStream(region).getChannel.map(MapMode.READ_ONLY, offset, FlexMQConstant.MaxMessageSize)
//    mappedByteBuffer.load()
//    mappedByteBuffer.flip()

    val messageArrayByte = new Array[Byte](FlexMQConstant.MaxMessageSize)
    mappedByteBuffer.get(messageArrayByte)
//
    lock.readLock().unlock()

    val msg = Message.deserialize(messageArrayByte)
    System.out.println("已经读取数据:" + msg)


    return msg
  }

  override def delete(topicName: String,regionSequence: Int): Unit = {
    val region = getRegion(topicName,regionSequence.toString)
    if(region != null){
      region.delete()
    }
  }

  override def delete(topicName: String,regionSequence: Int, offset: Int): Unit = {


  }


  def getLastRegion(topicName: String): File = {
    val regions = FileTool.getFolder(partitionFileRoot + topicName).listFiles()
    if(regions.isEmpty){
      return null
    }
    val lastRegion = regions.last
    if(lastRegion.exists()){
      lastRegion
    }else{
      null
    }
  }
  def getRegion(topicName: String,regionSequence: String): File = {
    val region = new File(partitionFileRoot + topicName + File.separator + FlexMQConstant.FileStorageRegionPre + regionSequence + RegionSuffix)
    if(region.exists()){
      region
    }else{
      null
    }
  }
  def createNewRegion(topicName: String): File = {
    var newRegion: File = null

    val lastRegion = getLastRegion(topicName)
    if(lastRegion != null){
      newRegion = FileTool.getFile(partitionFileRoot + topicName + File.separator + FlexMQConstant.FileStorageRegionPre + FileTool.getNextSequence(getLastRegion(topicName).getName)+ RegionSuffix)
    }else{
      newRegion = FileTool.getFile(partitionFileRoot + topicName + File.separator + FlexMQConstant.FileStorageRegionPre + 1 + RegionSuffix)
    }

    newRegion
  }

  override def readAndDelete(topicName: String, regionSequence: Int, offset: Int): Message = {
    null
  }

}
