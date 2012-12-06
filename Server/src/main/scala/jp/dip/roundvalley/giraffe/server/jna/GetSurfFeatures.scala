package jp.dip.roundvalley.giraffe.server.jna

/*
 *SURF特徴量検出を行うクラス 
 */

import com.sun.jna._
import jp.dip.roundvalley.scala.support._
import jp.dip.roundvalley.giraffe.server.model._

object GetSurfFeatures {
   val lib_path = getClass.getClassLoader.getResource("jna/libget_surf_features.so").getFile
   val getSurfFeaturs = NativeLibrary.getInstance(lib_path)
   
   def init(thread_count: java.lang.Integer){
	   val init = this.getSurfFeaturs.getFunction("init")
	   init.invoke(Array(thread_count))
   }
}

class GetSurfFeatures(thread_id: java.lang.Integer,ipadder: String) {
    /*
     *     
     */
    def fromFile(file_path: String){
		val exeSurf = GetSurfFeatures.getSurfFeaturs.getFunction("exeSurf")
		exeSurf.invokePointer(Array(thread_id,file_path))
    }
    
    /*
     * 
     */
    def fromYUV(width: java.lang.Integer,height: java.lang.Integer,data: Array[Byte]){
      val exeSurf = GetSurfFeatures.getSurfFeaturs.getFunction("exeSurfFromYuv")
      val mtime = myCountTime.getExecutionTime{
    	  exeSurf.invoke(Array(thread_id,width,height,data))
      }
      myLog.exe_time(thread_id,ipadder,"SURF",mtime.toString)
      Log.create.tag("SURF").ipadder(ipadder).exe_time(mtime.toString).save
    }
	
    /*
     * 
     */
	def rows(): java.lang.Integer = {
	     GetSurfFeatures.getSurfFeaturs.getFunction("getRows").invokeInt(Array(thread_id))
	}
	
	/*
	 * 
	 */
	def cols(): java.lang.Integer = {
			GetSurfFeatures.getSurfFeaturs.getFunction("getCols").invokeInt(Array(thread_id))
	}
	
	/*
	 * 
	 */
	def keypoints_size(): java.lang.Integer = {
	  GetSurfFeatures.getSurfFeaturs.getFunction("getKeypointsSize").invokeInt(Array(thread_id))
	}
	
	/*
	 * 
	 */
	def descriptors(): Array[Float] = {
	  val descriptors = GetSurfFeatures.getSurfFeaturs.getFunction("getDescriptors").invokePointer(Array(thread_id))
	  descriptors.getFloatArray(0, cols * rows)
	}
}