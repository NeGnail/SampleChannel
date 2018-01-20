package cluster

import java.util.concurrent.atomic.AtomicInteger

/**
  * Created by Administrator on 2017/4/29 0029.
  */
class MessageContext(private val regionSequence: AtomicInteger, private val offset: AtomicInteger) {
  def this(){
    this(new AtomicInteger(0),new AtomicInteger(0))
  }

  def getRegionSequence = regionSequence
  def getOffset = offset

}
