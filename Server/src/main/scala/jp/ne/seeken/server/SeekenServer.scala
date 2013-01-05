package jp.ne.seeken.server

/**
 * mst server
 */

//scala
import scala.sys.process._
import scala.io.Source
import scala.util.Random
import jp.dip.roundvalley.scala.support._
import jp.ne.seeken.server.model._
import jp.ne.seeken.server.jna._
import java.net.ServerSocket
import jp.ne.seeken.YoutubeDownload


/**
 *
 */
object SeekenServer {
  val prop = new java.util.Properties()
  

  def main(args: Array[String]) = {
    prop.load(new java.io.FileInputStream(getClass.getClassLoader.getResource("conf.properties").getFile))
    val port = prop.get("seeken.mst.port").toString.toInt
    val max_thread = prop.get("seeken.mst.max_thread").toString.toInt

    //SeekenDB
    myLog.info("SeekenServer", "Seeken DB creating...")
    SeekenDB.connect
    SeekenDB.create(max_thread)
    myLog.info("SeekenServer", "Seeken DB created...")

    //Youtube Downloader
    myLog.info("YoutubeDownload", "Youtube Download creating...")
    YoutubeDownload(port + 1)
    myLog.info("YoutubeDownload", "Youtube Download creating...")

    
   //Seeken Mst Server
    SeekenMstServer(port,max_thread)
  }
}