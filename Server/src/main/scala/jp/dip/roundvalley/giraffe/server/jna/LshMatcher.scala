package jp.dip.roundvalley.giraffe.server.jna

/*
 * LSHマッチングを行うクラス
 */

import com.sun.jna._
import jp.dip.roundvalley.scala.support._

object LshMatcher {
   val lib_path = "/Users/maruyayoshihisa/workspace/scala/workspace/jnaTest/jna/liblsh_matcher.so"
   val lshMatcher = NativeLibrary.getInstance(lib_path)
   
   ////lshのハッシュテーブルを作成
   def init(dir_path: String,db_size: java.lang.Integer){
       myLog.info("LSH","table creating....")
	   this.lshMatcher.getFunction("init").invoke(Array())
	   this.lshMatcher.getFunction("readFromMstImg").invoke(Array(dir_path,db_size))
	   myLog.info("LSH", "table created")
   }
}

class LshMatcher(thread_id: java.lang.Integer,ipadder: String){
  ////マッチングの実行
  ////int match(int query_keypoints_size,int rows,int cols,float* query_descriptors)
  //// TODO: 現在閾値を決めてないので、すべてを返してしまう。
  def exe_match(query_keypoints_size: java.lang.Integer,rows: java.lang.Integer, cols: java.lang.Integer,query_descriptors: Array[Float]): List[Int] = {
    var result_id: Pointer = null
    val time = myCountTime.getExecutionTime{
    	result_id = LshMatcher.lshMatcher.getFunction("match").invokePointer(Array(query_keypoints_size,rows,cols,query_descriptors))
    }
    myLog.exe_time(thread_id,ipadder,"Match",time.toString)
    val result_tupple = _makeTupple(result_id.getIntArray(0, 99).toList,0).sort((x,y) => x._2 > y._2)
    //result_tupple.foreach(x => print(x + ","))
    _getFront(5, result_tupple)
  }
  
  private def _getFront(num: Int,l: List[(Int,Int)]): List[Int] = {
    if(num == 0) Nil
    else l.head._1::_getFront(num - 1, l.tail)
  }
  
  private def _makeTupple(l: List[Int],i: Int): List[(Int,Int)] ={
    l match{
      case Nil => Nil
      case h::t => (i,h)::_makeTupple(t, i + 1)
    }
  } 
}