package network

/**
  * Created by LiangEn.LiWei on 2017/3/6.
  */
class AkkaConnectionArgument(private val protocol: String, private val system: String) {
  def getProtocol = protocol
  def getSystem = system

}
