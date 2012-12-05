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

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import org.squeryl.KeyedEntity
import org.squeryl.Schema
import org.squeryl.{ SessionFactory, Session }

object jnaTest {
  val port = 5004
  val thread_num = 50
  val mst_image_dir_path = getClass.getClassLoader.getResource("mst_image").getFile
  val mst_image_num = 99
  val sqlite_db = getClass.getClassLoader.getResource("h2").getFile
   
  def main(args : Array[String]) = {
    println("h2 test")
   
    Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection("jdbc:h2:" + sqlite_db + "/test", "sa", "")
    val stmt = conn.createStatement
    stmt.execute("drop table sample")
    stmt.execute("create table sample (id identity, value varchar(255))")
    conn.close

    par(insert(1), insert(2))
    
    println("end")
    /*
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
    }*/
  }

  // 100件INSERTする関数
  def insert(id : Int) {
    val conn = DriverManager.getConnection("jdbc:h2:" + sqlite_db + "/test", "sa", "")
    val stmt = conn.createStatement
    for (i <- 0 to 100) {
      stmt.execute("insert into sample (value) values ( '%d-%d' )" format (id, i))
    }
    conn.close
  }
}