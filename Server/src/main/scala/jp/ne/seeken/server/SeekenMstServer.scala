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
object SeekenMstServer{
  def apply(port: Int,max_thread: Int){
    (new SeekenMstServer(port,max_thread)).start
  }
}

/**
 *
 */
class SeekenMstServer(port: Int,max_thread: Int) extends Thread{

  override def run = {
    try {
      //create a server socket
      val serverSocket = new ServerSocket(port)
      Surf.init(max_thread)
      myLog.info("Serverstart", "port=" + port + ",ipadder=" + serverSocket.getInetAddress.toString)

      while (true) {
        val socket = serverSocket.accept()
        val id = ThreadIdManagement.acquisition
        val th = new ClinetManagerThread(id, socket)
        th.start()
        //accept a connection from a clinet  
      }
    } catch {
      case e => e.printStackTrace()
    }
  }
}