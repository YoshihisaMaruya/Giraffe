package jp.ne.seeken.server.model

import jp.ne.seeken.server.jna.Surf
import jp.ne.seeken.server.jna.Surf
import jp.dip.roundvalley.scala.support._
import net.liftweb.common.Box
import net.liftweb.http.{ LiftRules }
import net.liftweb.mapper.{ DB, Schemifier, DefaultConnectionIdentifier, StandardDBVendor }
import jp.ne.seeken.server.jna.LshMatcher
import scala.io.Source
import scala.io.Source
import jp.ne.seeken.serializer.RequestSerializer

/**
 * PermanentDB,LSH DBを管理するクラス
 */
object SeekenDB {
  def convetFileToFeatures(file_path: String): Array[Float] = Source.fromFile(file_path).getLines.toList(0).split(',').map(f => f.toFloat)

  def connect() {
    //DB connection
    if (!DB.jndiJdbcConnAvailable_?) {
      val prop = new java.util.Properties()
      val config = prop.load(new java.io.FileInputStream(getClass.getClassLoader.getResource("conf.properties").getFile))

      val h2_db = getClass.getClassLoader.getResource("h2").getFile
      val h2_name = prop.get("h2.name").toString
      val h2_username = prop.get("h2.username").toString
      val h2_passwd = prop.get("h2.passwd").toString

      val vendor = new StandardDBVendor("org.h2.Driver", "jdbc:h2:" + h2_db + "/" + h2_name, Box(h2_username), Box(h2_passwd))
      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
      Schemifier.schemify(true, Schemifier.infoF _, Log, PermanentDB)
    }
  }
  /**
   * mst imageからLsh DBを作成。ただし、JUnitからしか呼ばれない
   *
   */
  def createFromMstImage {
    myCountTime.printExecutionTime({
      //Surfの初期化
      Surf.init(1)
      val surf = new Surf(0)
      PermanentDB.removeAll
      PermanentDB.createFromMstImage(surf, getClass.getClassLoader.getResource("mst_image").getFile)
    })
  }

  /**
   * PermanentDBからLSH DBを作成
   */
  def create(max_thread: Int) = {
    //Lsh初期化
    LshMatcher.init
    Surf.init(max_thread)
    myCountTime.printExecutionTime({
      LshDB.create(PermanentDB.findAll)
    })
  }

  /**
   *
   */
  def add {}

  //TODO: PermenentDBを直接返すと、編集可能になってしまう。SeekenDBはPermanentDBのラッパーなので、要変更
  /**
   *
   */
   def findById(id: Int): PermanentDB = PermanentDB.findByKey(id).get
  
   
   /**
    * def findAll(): 
    */
   def findAll: List[PermanentDB] = PermanentDB.findAll
 
  

  def remove {}

  /**
   * Youtubeのダウンロードリンクを取得
   */
  def getYoutubeDownloadLink(id: Int): String = {
    val p = findById(id)

    val youtube_download_link =
      try {
        Source.fromURL(p.youtube_download_link.toString)
        p.youtube_download_link.toString
      } catch {
        case e => {
          val link = YoutubeDownloadLinkGenerator(p.youtube_link.toString)
          p.youtube_download_link(link).save
          link
        }
      }
    youtube_download_link
  }
}

class SeekenDB(thread_id: Int, ipadder: String) {
  val lsh = new LshMatcher(thread_id, ipadder)
  val surf = new Surf(thread_id,ipadder)

  /**
   *
   */
  def query(query_keypoints_size: Int, rows: Int, cols: Int, query_descriptors: Array[Float]): List[Int] = lsh.exe_match(query_keypoints_size, rows, cols, query_descriptors)

  /**
   * 
   */
  def query(width: Int,height: Int,data: Array[Byte],color_format: String): List[Int] = {
    color_format match {
    	case "argb" => surf.fromRGB(width, height,data)
    	case "gray" => surf.fromGray(width, height, data)
    }
    query(surf.keypoints_size,surf.row,surf.col,surf.descriptors)
  }
  
  def query(rs: RequestSerializer): List[Int] = {
    this.query(rs.width,rs.height,rs.data,rs.color_format)
  }
  
  def query(id: Int): List[Int] = {
    val q = SeekenDB.findById(id)
    lsh.exe_match(q.keypointsSize.toInt, q.row.toInt, q.col.toInt, q.featuresArray)
  }
  
  def query(file_path: String): List[Int] = {
	  surf.fromFile(file_path)
	  query(surf.keypoints_size, surf.row, surf.col, surf.descriptors)
  }
}

