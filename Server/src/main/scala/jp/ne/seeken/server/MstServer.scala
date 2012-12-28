package jp.ne.seeken.server


/**
 * mst server
 */

//scala
import scala.sys.process._
import scala.io.Source
import scala.util.Random

//lifr orm
import net.liftweb.common.Box
import net.liftweb.http.{ LiftRules }
import net.liftweb.mapper.{ DB, Schemifier, DefaultConnectionIdentifier, StandardDBVendor }

//seeken
import jp.dip.roundvalley.scala.support._
import jp.ne.seeken.server.model._
import jp.ne.seeken.server.jna._

//java
import java.net.ServerSocket


class MstServer {
  val mst_image_dir_path = getClass.getClassLoader.getResource("mst_image").getFile
  val mst_image_num = 99
  val port = App.get("server.port").text.toInt
  val max_thread = App.get("server.max_thread").text.toInt

  def main(args : Array[String]) = {
    println("h2 test")

    //DB connection
    if (!DB.jndiJdbcConnAvailable_?) {
      val db_info = App.get("db")
      val h2_db = getClass.getClassLoader.getResource("h2").getFile
      myLog.info("DB", "name=" + h2_db + "/" + (db_info \ "name").text + ",username=" + (db_info \ "username").text + ",passwd=" + (db_info \ "passwd").text)
      val vendor = new StandardDBVendor("org.h2.Driver", "jdbc:h2:" + h2_db + "/" + (db_info \ "name").text, Box((db_info \ "username").text), Box((db_info \ "passwd").text))
      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
      Schemifier.schemify(true, Schemifier.infoF _, Log)
    }

    try {
      //create a server socket
      val serverSocket = new ServerSocket(port)
      GetSurfFeatures.init(max_thread)
      LshMatcher.init(mst_image_dir_path, mst_image_num)
      myLog.info("Serverstart", "port=" + App.get("server.port") + ",ipadder=" + serverSocket.getInetAddress.toString)

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