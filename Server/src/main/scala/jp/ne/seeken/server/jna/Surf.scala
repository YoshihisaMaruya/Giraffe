package jp.ne.seeken.server.jna

/**
 *  SURF特徴量検出を行うクラス 
 * calling ligget_surf_features
 */

//jna
import com.sun.jna._
import jp.dip.roundvalley.scala.support._
import jp.ne.seeken.server.model._

object Surf {
   val lib_path = getClass.getClassLoader.getResource("jna/libsurf.so").getFile
   val getSurfFeaturs = NativeLibrary.getInstance(lib_path)
   
   /**
    * Surf初期化
    */
   def init[T](max_thread: T)(implicit t:T => java.lang.Integer) = {
	   val init = this.getSurfFeaturs.getFunction("init")
	   init.invoke(Array(max_thread))
   }
}

class Surf(thread_id: java.lang.Integer,ipadder: String = null) {
    /*
     * 
     */
    def fromFile(file_path: String){
		val exeSurf = Surf.getSurfFeaturs.getFunction("exeSurfFromFile")
		exeSurf.invokePointer(Array(thread_id,file_path))
    }
    
    /**
     * YUVデータ形式からSURFを実行
     */
    def fromYUV(width: java.lang.Integer,height: java.lang.Integer,data: Array[Byte]){
      val exeSurf = Surf.getSurfFeaturs.getFunction("exeSurfFromYuv")
      val mtime = myCountTime.getExecutionTime{
    	  exeSurf.invoke(Array(thread_id,width,height,data))
      }
      myLog.exe_time(thread_id,ipadder,"SURF",mtime.toString)
      Log.create.tag("SURF").ipadder(ipadder).exe_time(mtime.toString).save
    }
    
    /**
     * RGBデータ形式からSURFを実行
     */
    def fromRGB(width: java.lang.Integer,height: java.lang.Integer,data: Array[Byte]){
       val exeSurf = Surf.getSurfFeaturs.getFunction("exeSurfFromRgb")
      val mtime = myCountTime.getExecutionTime{
    	  exeSurf.invoke(Array(thread_id,width,height,data))
      }
      myLog.exe_time(thread_id,ipadder,"SURF",mtime.toString)
      Log.create.tag("SURF").ipadder(ipadder).exe_time(mtime.toString).save
    }
	
    /**
     * rowsを取得
     */
	def row: java.lang.Integer = {
	     Surf.getSurfFeaturs.getFunction("getRow").invokeInt(Array(thread_id))
	}
	
	/**
	 * colsを取得
	 */
	def col: java.lang.Integer = {
			Surf.getSurfFeaturs.getFunction("getCol").invokeInt(Array(thread_id))
	}
	
	/**
	 * keypointのサイズを取得
	 */
	def keypoints_size: java.lang.Integer = {
	  Surf.getSurfFeaturs.getFunction("getKeypointsSize").invokeInt(Array(thread_id))
	}
	
	/**
	 * descripotrsを取得
	 */
	def descriptors: Array[Float] = {
	  val descriptors = Surf.getSurfFeaturs.getFunction("getDescriptors").invokePointer(Array(thread_id))
	  descriptors.getFloatArray(0, col * row)
	}
}