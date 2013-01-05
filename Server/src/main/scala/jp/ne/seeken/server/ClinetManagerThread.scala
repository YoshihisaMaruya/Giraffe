package jp.ne.seeken.server

/**
 * クライアントの要求(画像検索要求)に結果を返すクラス
 */

//java
import java.io.File
import java.net.Socket
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import jp.ne.seeken.server.jna.Surf
import jp.ne.seeken.server.jna.LshMatcher
import jp.ne.seeken.server.model.Log
import jp.dip.roundvalley.scala.support._
import jp.ne.seeken.serializer._
import jp.ne.seeken.xml
import jp.ne.seeken.server.model.SeekenDB
import jp.ne.seeken.xml.MkTrackingData
import jp.ne.seeken.xml.MkTrackingData

class ClinetManagerThread(thread_id: Int, socket: Socket) extends Thread {

  private var permanent_ids_manager: Array[Int] = List.fill(5)(-1).toArray

  //imgを読み込む
  private def _loadImage(file_name: String): BufferedImage = {
    try {
      println(file_name)
      val file = new File(file_name)
      ImageIO.read(file)
    } catch {
      case e => e.printStackTrace(); null
    }
  }

  /**
   * rgbのバイト配列をint配列に直す
   * @param bRGB : RGB配列(バイト)
   * @return iRGB : RGB配列(int) * alphaは0
   */
  private def rgbByteArraytoIntArray(bRGB: Array[Byte]): Array[Int] = {
    val iRGB_length = bRGB.length / 3
    val iRGB = new Array[Int](iRGB_length)
    var count = 0
    for (i <- 0 until iRGB_length) {
      val r = bRGB(count)
      val g = bRGB(count + 1)
      val b = bRGB(count + 2)
      iRGB(i) = (0xff000000 | r << 16 | g << 8 | b)
      count = count + 3
    }
    iRGB
  }

  /**
   * permanent idをマネージメント、必要な物だけimageに追加する。
   */

  private def _makeImageOfResponse(ids: List[Int]) = {
    def _make(ids: List[Int], i: Int, images: Array[Array[Byte]]): Array[Array[Byte]] = {
      ids match {
        case Nil => images
        case id :: t => {
          var is_send = false
          for (p_id <- permanent_ids_manager) {
            //対象のidが送信されていない場合、新たに追加
            if (p_id == id) {
            	is_send = true
            }
          }
          if(!is_send){
             val p = SeekenDB.findById(id)
              images(i) = _makeRGBImage(_loadImage(p.image))
          }
          _make(t, i + 1, images)
        }
      }
    }
    val result = _make(ids, 0, new Array[Array[Byte]](5))
    permanent_ids_manager = ids.toArray
    result
  }

  private def _makeRGBImage(buffer: BufferedImage): Array[Byte] = {
    //color int(a,r,g,b) をbyte(a),byte(b),byte(c)に分割
    val a = (c: Int) => (c >> 24).toByte
    val r = (c: Int) => (c >> 16 & 0xff).toByte
    val g = (c: Int) => (c >> 8 & 0xff).toByte
    val b = (c: Int) => (c & 0xff).toByte

    //response
    val height = buffer.getHeight
    val widht = buffer.getWidth
    val rgbs = new Array[Byte](height * widht * 3) //byte型
    var count: Int = 0
    for (y <- 0 until height) {
      for (x <- 0 until widht) {
        //rgbs(count) = rgb(result_img.getRGB(x, y))
        val argb = buffer.getRGB(x, y)
        rgbs(count) = r(argb)
        rgbs(count + 1) = g(argb)
        rgbs(count + 2) = b(argb)
        count = count + 3
      }
    }
    rgbs;
  }

  /**
   * response用データを作成
   * TODO: 専用のクラスを作る
   */
  def _makeResponse(result_ids: List[Int]): ResponseSerializer = {
    val xml = MkTrackingData.get(result_ids)

    val id_maps = result_ids.toArray

    val images = _makeImageOfResponse(result_ids)
    
    val heighs = result_ids.map(id => {
      val s = SeekenDB.findById(id)
      s.height.toInt
    }).toArray
    
    val widths = result_ids.map(id => {
      val s = SeekenDB.findById(id)
      s.width.toInt
    }).toArray
    
    new ResponseSerializer(xml, id_maps, widths,heighs,images)
  }

  override def run() {
    myLog.info("NewClient", "thread_id=" + thread_id + ", ipadder=" + socket.getInetAddress())

    var is_close = false
    var out: ObjectOutputStream = null
    var in: ObjectInputStream = null
    var result_ids_buffer: Array[Int] = new Array[Int](5)

    //seeken db
    val seekenDB = new SeekenDB(thread_id, this.socket.getInetAddress.toString)

    try {
      out = new ObjectOutputStream(this.socket.getOutputStream());
      in = new ObjectInputStream(this.socket.getInputStream());

      while (!is_close) {
        //accept
        val request = in.readObject().asInstanceOf[RequestSerializer]
        val start = System.currentTimeMillis()

        is_close = if (request == null) true //nullが来たら通信終了
        else {
          myLog.info("Accept", "thread_id=" + thread_id + ", ipadder=" + this.socket.getInetAddress)
          myLog.info("RequestSize", "widht=" + request.width + ",height=" + request.height + ",size=" + request.data.length + "[B]" + ",color_format = " + request.color_format)
          //request
          val result = seekenDB.query(request.width, request.height, request.data, request.color_format)
          myLog.info("Result", "thread_id =" + thread_id + ",result_ids=" + result.toString)

          //response
          val response = _makeResponse(result)
          //myLog.info("ResponseSize","widht=" + response.ge + "height=" + response.getHeight + "size=" + response.getByteData.length + "[B]")
          out.writeObject(response)
          out.flush()

          //total log
          myLog.exe_time(thread_id, this.socket.getInetAddress.toString, "Total", (System.currentTimeMillis() - start).toString)
         // Log.tag("Response").exe_time((System.currentTimeMillis() - start).toString).ipadder(this.socket.getInetAddress.toString).save()
          false
        }
      }

    } catch {
      case e => {
        e.printStackTrace()
      }
    } finally {
      this.socket.close()
      out.close()
      in.close()
      ThreadIdManagement.releace(thread_id) //idの開放
    }
  }
}