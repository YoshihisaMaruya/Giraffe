package jp.ne.seeken.server.model

import net.liftweb.mapper._
import jp.ne.seeken.server.jna._
import java.io.File
import org.apache.commons.io.IOUtils
import jp.dip.roundvalley.scala.support.YoutubeDownloadLinkGenerator
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.OutputStreamWriter
import scala.io.Source
import javax.imageio.ImageIO
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import jp.dip.roundvalley.scala.support.myLog

/**
 * 特徴量とその対応を保持するデータベース(h2)
 */
protected class PermanentDB extends LongKeyedMapper[PermanentDB] with IdPK {
  def getSingleton = PermanentDB

  /**
   * imageの名前はid.png or id.jpg
   */
  object image extends MappedString(this, 255)
  
  /**
   * imageの高さ
   */
  object height extends MappedInt(this)
  
  /**
   * imageの横幅
   */
  object width extends MappedInt(this)

  /**
   * imageの名前をとりあえず覚えておく
   */
  object image_name extends MappedString(this, 255)

  /**
   * featureの名前はid.surf
   */
  object feature extends MappedString(this, 255)
  
  /**
   * 特徴点の配列
   */
  def featuresArray: Array[Float] =  {
    val sf = Source.fromFile(this.feature)
    val r = sf.getLines.toList(0).split(',').map(f => f.toFloat)
    sf.close()
    r
  }

  
  /**
   * 行
   */
  object col extends MappedInt(this)
  
  /**
   * 列
   */
  object row extends MappedInt(this)
  
  /**
   * キーポイントサイズ
   */
  object keypointsSize extends MappedInt(this)

  /**
   * Yotubeリンク
   */
  object youtube_link extends MappedString(this, 5000)

  /**
   * Yotubeリンクダウンロードへのリンク
   */
  object youtube_download_link extends MappedString(this, 5000)
  
  /**
   * grayを保存
   */
  object gray extends MappedString(this,500)
  
  /**
   * gray byte配列を所得
   */
  def grayArray: Array[Byte] = {
        
    val fi = new FileInputStream(gray)
    val result = new Array[Byte](width * height)
	fi.read(result,0,result.length)
    fi.close
    result
  }
  
}

protected object PermanentDB extends PermanentDB with LongKeyedMetaMapper[PermanentDB] {
  val image_dir = getClass.getClassLoader.getResource("image").getFile //imageを保存するパス
  val feature_dir = getClass.getClassLoader.getResource("feature").getFile //特徴量を保存するパス
  val gray_dir =  getClass.getClassLoader.getResource("gray").getFile //grayイメージを保存するパス
  
  def remove(id: Int) {
  }

  def remove(c: PermanentDB) {
    if (!c.image.toString.equals("")) {
      val image = c.image
      (new File(image)).delete
    }
    if (!c.feature.toString.equals("")) {
      val feature = c.feature
      (new File(feature)).delete
    }
    if (!c.gray.toString.equals("")) {
      val gray = c.gray
      (new File(gray)).delete
    }
    c.delete_!
  }

  /**
   * H2 DBを初期化
   */
  def removeAll = {
    this.findAll.foreach(remove)
  }

  /**
   *
   */
  @Override
  def create(surf: Surf, input_image: String, youtube_link: String) {
    myLog.info("PermanentDB.create",input_image)
    //idの取得
    val c = super.create
    c.save
    val id = c.id

    //Surfの実行,保存
    surf.fromFile(input_image)
    val feature = feature_dir + "/" + id + ".surf"
    val feature_file = new File(feature)
    val feature_fos = new FileOutputStream(feature_file, false)
    val feature_writer = new OutputStreamWriter(feature_fos, "UTF-8")

    val col = surf.col
    val row = surf.row
    c.keypointsSize(surf.keypoints_size).col(col).row(row).save
    
    val descriptors = surf.descriptors

    for (i <- 0 until row) {
      val k = col * i
      for (j <- 0 until col) {
                  feature_writer.write(descriptors(k + j) + ",")
      }
    }
    feature_writer.write("\n")
    c.feature(feature)
        
    feature_writer.close
    feature_fos.close

    //yotubeリンクの取得
    val youtube_download_link = YoutubeDownloadLinkGenerator(youtube_link)
    c.youtube_download_link(youtube_download_link)
    c.youtube_link(youtube_link)


    //画像の縦横を取得
    val fs = new FileInputStream(input_image)
    val image = ImageIO.read(fs)
    val width = image.getWidth
    val height = image.getHeight
    fs.close
    
    //imageファイルのコピー
    val is = new FileInputStream(input_image)
    val image_path = image_dir + "/" + id + ".jpg"
    val os = new FileOutputStream(image_path)
    try {
      IOUtils.copy(is, os)
    } catch {
      case e => throw new Exception(e.getMessage())
    } finally {
      os.close
      is.close
    }
 
    c.width(width).height(height)
    c.image(image_path).image_name(input_image)
    
       //grayデータの保存
    val gray = gray_dir +"/" + id + ".obj"
    val gray_fi = new FileOutputStream(gray)
	
    val gray_array = surf.gray(width,height)
    gray_fi.write(gray_array,0,gray_array.length)
    gray_fi.flush
    gray_fi.close
 
    c.gray(gray)

    c.save
  }

  /**
   * Mst imageからDBを作成
   */
  def createFromMstImage(surf: Surf, mst_image_dir_path: String) {
    //dummyのyoutube
    val dummy_youtube_link = "http://www.youtube.com/watch?v=iGfLqqjHh3U"
    val mst_images = (new File(mst_image_dir_path)).listFiles.toList
    mst_images.foreach(f => {
      create(surf, f.getCanonicalPath, dummy_youtube_link)
    })
  }

  /**
   * H2 DB
   */
  def init(refresh: Boolean = false) {

  }
}