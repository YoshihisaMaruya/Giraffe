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
import jp.ne.seeken.server.jna.GetSurfFeatures
import jp.ne.seeken.server.jna.LshMatcher
import jp.ne.seeken.server.model.Log

//support
import jp.dip.roundvalley.RequestSerializer
import jp.dip.roundvalley.ResponseSerializer
import jp.dip.roundvalley.scala.support._



class ClinetManagerThread(thread_id : Int, socket : Socket) extends Thread {

  //imgを読み込む
  private def _loadImage(file_name : String) : BufferedImage = {
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
  private def rgbByteArraytoIntArray(bRGB:Array[Byte]): Array[Int] = {
    val iRGB_length = bRGB.length / 3
    val iRGB = new Array[Int](iRGB_length)
    var count = 0
    for(i <- 0 until iRGB_length){
      val r = bRGB(count)
      val g = bRGB(count + 1)
      val b = bRGB(count + 2)
      iRGB(i) = (0xff000000 | r << 16 | g << 8 | b)
      count = count + 3
    }
    iRGB
  }
  
  /*
   * response用データを作成 
   */
  private def _makeResponse(result_id : Int) : ResponseSerializer = {
    val result_img = _loadImage("/Users/maruyayoshihisa/workspace/scala/workspace/jnaTest/mst_image/" + result_id + ".jpg")
    
    //color int(a,r,g,b) をbyte(a),byte(b),byte(c)に分割
    val r = (c:Int) => (c >> 16 & 0xff).toByte
    val g = (c:Int) => (c >> 8 & 0xff).toByte
    val b = (c:Int) => (c & 0xff).toByte
    
    //response
    if (result_img != null) {
      val height = result_img.getHeight
      val widht = result_img.getWidth
      val rgbs = new Array[Byte](height * widht * 3) //byte型
      val a : java.lang.Integer = 0
      var count : Int = 0
      for (y <- 0 until height) {
        for (x <- 0 until widht) {
          //rgbs(count) = rgb(result_img.getRGB(x, y))
          rgbs(count) = r(result_img.getRGB(x, y))
          rgbs(count + 1) = g(result_img.getRGB(x, y))
          rgbs(count + 2) = b(result_img.getRGB(x, y))
          count = count + 3
        }
      }
      new ResponseSerializer(result_id.toString, height, widht, rgbs)
    } else null
  }

  override def run() {
    myLog.info("NewClient","thread_id=" + thread_id + ", ipadder=" + socket.getInetAddress())

    var is_close = false
    var out : ObjectOutputStream = null
    var in : ObjectInputStream = null
    var result_ids_buffer : Array[Int] = new Array[Int](5)

    //native
    val getSurfFeatures = new GetSurfFeatures(thread_id,this.socket.getInetAddress.toString)
    val lshMatcher = new LshMatcher(thread_id,this.socket.getInetAddress.toString)

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
          myLog.info("RequestSize","widht=" + request.getWidth + "height=" + request.getHeight + "size=" + request.getByteData.length + "[B]")
          //surf
          getSurfFeatures.fromYUV(request.getWidth, request.getHeight, request.getByteData)

          //match
          val result_ids = lshMatcher.exe_match(getSurfFeatures.keypoints_size, getSurfFeatures.rows, getSurfFeatures.cols, getSurfFeatures.descriptors)
          myLog.info("Result", "thread_id =" + thread_id + ",result_ids=" + result_ids.toString)
          
          //response
          val response = _makeResponse(result_ids.head)
          myLog.info("ResponseSize","widht=" + response.getWidth + "height=" + response.getHeight + "size=" + response.getByteData.length + "[B]")
          out.writeObject(response)
          out.flush()
          
          //total log
          myLog.exe_time(thread_id,this.socket.getInetAddress.toString,"Total",(System.currentTimeMillis() - start).toString)
          Log.tag("Response").exe_time((System.currentTimeMillis() - start).toString).ipadder(this.socket.getInetAddress.toString).save()
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