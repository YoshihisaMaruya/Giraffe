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
import java.io.FileOutputStream

class ClinetManagerThread(thread_id: Int, socket: Socket) extends Thread {

  private var permanent_ids_manager: Array[Int] = List.fill(5)(-1).toArray

  /**
   * permanent idをマネージメント、必要な物だけimage情報に追加する。
   * @return (widths,height,images)
   */
  private def _makeImageInfoOfResponse(ids: List[Int]): (Array[Int],Array[Int],Array[Array[Byte]]) = {
    /**
     * idsからimage infoを作成
     */
    def _make(ids: List[Int],widths: Array[Int],heights: Array[Int],images: Array[Array[Byte]],i: Int): (Array[Int],Array[Int],Array[Array[Byte]]) = {
      ids match {
        case Nil => (widths,heights,images)
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
             widths(i) = p.width
             heights(i) = p.height
             images(i) = p.grayArray
          }
          else {
        	  images(i) = null
          }
          _make(t, widths,heights,images,i + 1)
        }
      }
    }
    val result = _make(ids, new Array[Int](5),new Array[Int](5),new Array[Array[Byte]](5),0)
    permanent_ids_manager = ids.toArray
    result
  }


  /**
   * response用データを作成
   * TODO: 専用のクラスを作る
   */
  def _makeResponse(result_ids: List[Int]): ResponseSerializer = {
    val xml = MkTrackingData.get(result_ids)

    val id_maps = result_ids.toArray

    val image_info = _makeImageInfoOfResponse(result_ids)
    
    new ResponseSerializer(xml, id_maps, image_info._1,image_info._2,image_info._3)
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
        
        //サーバ側の処理時間の測定開始
        val start = System.currentTimeMillis()

        is_close = if (request == null) true //nullが来たら通信終了
        else {
          myLog.info("Accept", "thread_id=" + thread_id + ", ipadder=" + this.socket.getInetAddress)
          myLog.info("RequestSize", "widht=" + request.width + ",height=" + request.height + ",size=" + request.data.length + "[B]" + ",color_format = " + request.color_format)
          //request
          //クエリーにかかる時間を測定
          val query_time = System.currentTimeMillis()
          val result = seekenDB.query(request)
          
          //クエリーにかかった時間
          myLog.exe_time(thread_id, this.socket.getInetAddress.toString, "Query Time", (System.currentTimeMillis() - query_time).toString)
          myLog.info("Result", "thread_id =" + thread_id + ",result_ids=" + result.toString)

          //response
          //リスポンスデータを作成にかかった時間を測定
          val make_response_time = System.currentTimeMillis()
          val response = _makeResponse(result)
          
          
          //TODO : デバック用なので後で消去
          val req_os = new FileOutputStream("/Users/maruyayoshihisa/Desktop/hoge/request.obj")
          val req_oos = new ObjectOutputStream(req_os)
          req_oos.writeObject(request)
          req_oos.close
          req_os.close
          
          val res_os = new FileOutputStream("/Users/maruyayoshihisa/Desktop/hoge/response.obj")
          val res_oos = new ObjectOutputStream(res_os)
          res_oos.writeObject(response)
          res_oos.close
          res_os.close
          
          
          
          //リスポンスデータを作成にかかった時間
          myLog.exe_time(thread_id, this.socket.getInetAddress.toString, "Make Response Time", (System.currentTimeMillis() - make_response_time).toString)
          
          //サーバ側の処理時間
          myLog.exe_time(thread_id, this.socket.getInetAddress.toString, "Total Time", (System.currentTimeMillis() - start).toString)

          //送信
          out.writeObject(response)
          out.flush     
          //resetしないとクライアント側がOutOfMemoryになる？
          out.reset

          println()
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