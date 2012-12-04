import jp.dip.roundvalley
import scala.sys.process._
import com.sun.jna._
import scala.io.Source
import scala.concurrent.ops._
import scala.util.Random
import com.sun.org.apache.xalan.internal.xsltc.compiler.ForEach
import jp.dip.roundvalley.giraffe.server.jna.GetSurfFeatures
import java.net._
import jp.dip.roundvalley.giraffe.server._
import jp.dip.roundvalley.scala.support._
import jp.dip.roundvalley.giraffe.server.jna.LshMatcher

object jnaTest {
  val port = 5004
  val thread_num = 50
  val mst_image_dir_path = getClass.getClassLoader.getResource("mst_image").getFile
  val mst_image_num = 99
  
  def main(args : Array[String]) = {
    println("jna test")
    
    try{
      //create a server socket
      val serverSocket = new ServerSocket(port)
      GetSurfFeatures.init(thread_num)
      LshMatcher.init(mst_image_dir_path, mst_image_num)
      myLog.info("Serverstart","port=" + port + ",ipadder=" + serverSocket.getInetAddress.toString)
      
      while(true){
    	  val socket = serverSocket.accept()
    	  val id = ThreadIdManagement.acquisition
    	  val th = new ClinetManagerThread(id,socket)
    	  th.start()
      //accept a connection from a clinet  
      } 
    }
    catch{
      case e => e.printStackTrace() 
    }
    
   }
  
}