package jp.ne.seeken

import java.net.ServerSocket
import java.net.Socket
import java.io.DataInputStream
import java.io.DataOutputStream
import jp.dip.roundvalley.scala.support._
import jp.ne.seeken.server.model.SeekenDB
import java.io.InputStream
import java.net.URL
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.File

object YoutubeDownload {
  def apply(port: Int){
	  (new YoutubeDownload(port)).start
  }
}
class YoutubeDownload(port: Int) extends Thread {
  override def run = {
    try {
      myLog.debug("YoutubeDownload", "start")
      //create a server socket
      val serverSocket = new ServerSocket(port)

      while (true) {
        val socket = serverSocket.accept()
        val th = new YoutubeDownloadThread(socket)
        th.start()
        //accept a connection from a clinet  
      }
    } catch {
      case e => e.printStackTrace()
    }
  }
}

/**
 *
 */
private class YoutubeDownloadThread(socket: Socket) extends Thread {
  val buf = new Array[Byte](1024)

  /**
   * yotubeから動画のダウンロード、かつクライアントへアウトプット
   */
  private def donwloadAndOutput(in: InputStream,out: OutputStream) {
    val len = in.read(buf)
    if (len > 0) {
      out.write(buf,0,len) //クライアントへの送信
      donwloadAndOutput(in,out)
    }
  }

  override def run() {
    var in: DataInputStream = null
    var out: OutputStream = null
    var youtube_in: InputStream = null
    try {
      myLog.debug("YoutubeDownloadThread", "new conenct ip = " + socket.getInetAddress)

      in = new DataInputStream(socket.getInputStream)
      out = socket.getOutputStream

      //idからlinkを取得
      val id = in.readInt()
      val link = SeekenDB.getYoutubeDownloadLink(id)
      
      val url = new URL(link)
      youtube_in = url.openStream()
      donwloadAndOutput(youtube_in,out)
      
      out.flush()

      myLog.debug("YoutubeDownloadThread", "end ip = " + socket.getInetAddress)
    } catch {
      case e => e.printStackTrace
    } finally {
      in.close
      out.close
      youtube_in.close
      socket.close
    }
  }
}