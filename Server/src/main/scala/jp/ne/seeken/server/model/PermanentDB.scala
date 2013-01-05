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
   * 
   */
  def featuresArray: Array[Float] = Source.fromFile(this.feature).getLines.toList(0).split(',').map(f => f.toFloat)
}

protected object PermanentDB extends PermanentDB with LongKeyedMetaMapper[PermanentDB] {
  val image_dir = getClass.getClassLoader.getResource("image").getFile //imageを保存するパス
  val feature_dir = getClass.getClassLoader.getResource("feature").getFile //特徴量を保存するパス
  
  def remove(id: Int) {
  }

  def remove(c: PermanentDB) {
    if (!c.image.equals("")) {
      val image = c.image
      (new File(image)).delete
    }
    if (!c.feature.equals("")) {
      val feature = c.feature
      (new File(feature)).delete
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
    //idの取得
    val c = super.create
    c.save
    val id = c.id

    //Surfの実行,保存
    surf.fromFile(input_image)
    val feature = feature_dir + "/" + id + ".surf"
    val f = new File(feature)
    val fileOutPutStream = new FileOutputStream(f, false)
    val writer = new OutputStreamWriter(fileOutPutStream, "UTF-8")

    val col = surf.col
    val row = surf.row
    c.keypointsSize(surf.keypoints_size).col(col).row(row).save
    
    val descriptors = surf.descriptors

    for (i <- 0 until row) {
      val k = col * i
      for (j <- 0 until col) {
                  writer.write(descriptors(k + j) + ",")
      }
    }
    writer.write("\n")
    c.feature(feature)

    writer.close
    fileOutPutStream.close
    
    //画像の縦横を取得
    val image = ImageIO.read(f)
    val width = image.getWidth
    val height = image.getHeight
    c.width(width).height(height)

    //yotubeリンクの取得
    val youtube_download_link = YoutubeDownloadLinkGenerator(youtube_link)
    c.youtube_download_link(youtube_download_link)
    c.youtube_link(youtube_link)

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
    }
    c.image(image_path).image_name(input_image)

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