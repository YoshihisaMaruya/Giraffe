package jp.ne.seeken.server.jna

/**
 * LSHマッチングを行うクラス
 * calling liblsh_matcher.so
 */

import com.sun.jna._

//support
import jp.dip.roundvalley.scala.support._

//seeken
import jp.ne.seeken.server.model._

object LshMatcher {
   var current_size: Int = 0
   //LSH DBのマックスサイズ
   val max_size = 1000
   val lib_path = getClass.getClassLoader.getResource("jna/liblsh_matcher.so").getFile
   val lshMatcher = NativeLibrary.getInstance(lib_path)
   val id_map = new Array[Int](max_size)
 
   ////lshのハッシュテーブルを作成
   def init[T](dir_path: String,db_size: T)(implicit t:T => java.lang.Integer){
       myLog.info("LSH","table creating....")
	   this.lshMatcher.getFunction("init").invoke(Array())
	   this.lshMatcher.getFunction("readFromMstImg").invoke(Array(dir_path,db_size))
	   myLog.info("LSH", "table created")
	   current_size = db_size.asInstanceOf[Int]
   }
   
   /**
    * LSHの初期化
    */
   def init[T](d: T,n: T,L: T,k: T)(implicit t:T => java.lang.Integer){
      myLog.info("LSH","table creating....")
      this.lshMatcher.getFunction("init").invoke(Array(d,n,L,k))
      myLog.info("LSH","table creating....")
   }
   
   def init{
	   init(128,100000,32,64)
   }
   
   /**
    * LSHに追加
    */
   def add[T](id: Int,row: T, col: T,features: Array[Float])(implicit t:T => java.lang.Integer){
     this.lshMatcher.getFunction("add").invoke(Array(row,col,features))
     id_map(current_size) = id
     current_size = current_size + 1
   }
 
   /**
    * imageデータからLSHに追加
    */
    def add(image_path: String){

   }
}

class LshMatcher(thread_id: Int,ipadder: String){
  ////マッチングの実行
  ////int match(int query_keypoints_size,int rows,int cols,float* query_descriptors)
  //// TODO: 現在閾値を決めてないので、すべてを返してしまう。
  def exe_match[T](query_keypoints_size: T,rows: T, cols: T,query_descriptors: Array[Float])(implicit t:T => java.lang.Integer): List[Int] = {
    var result_id: Pointer = null
    val time = myCountTime.getExecutionTime{
    	result_id = LshMatcher.lshMatcher.getFunction("match").invokePointer(Array(query_keypoints_size,rows,cols,query_descriptors))
    }
    
    myLog.exe_time(thread_id,ipadder,"Match",time.toString)
    
    val result_tupple = _makeTupple(result_id.getIntArray(0, LshMatcher.current_size.asInstanceOf[java.lang.Integer]).toList,0).sort((x,y) => x._2 > y._2)
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
      case h::t => (LshMatcher.id_map(i),h)::_makeTupple(t, i + 1)
    }
  } 
}