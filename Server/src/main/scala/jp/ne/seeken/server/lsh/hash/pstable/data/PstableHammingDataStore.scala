package jp.ne.seeken.server.lsh.hash.pstable.data
import jp.ne.seeken.server.lsh.hash.pstable.PstableHandlerWrapper
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

import jp.dip.roundvalley.scala.support._

class PstableHammingDataStore(mst_data: List[List[Int]], phw : PstableHandlerWrapper) {
	
	//private var pstableHashMap = new HashMap[String,List[Int]]
	val pstableHashMap = this.makeBackets
	
	/**
	 * バケットsを作成
	 */
	private def makeBackets: HashMap[String,ListBuffer[Int]] = { 
	  var phm = new HashMap[String,ListBuffer[Int]]
	  var i = 0
	  
	  this.mst_data.foreach({
	    m =>{
	    	val pstableHashes = phw.getPstableHashes(m)
	    	pstableHashes.foreach(pstableHash => {
	    	  val factorList = phm.get(pstableHash)
	    	  if(factorList == None){
	    	     val fl = new ListBuffer[Int]
	    		 phm.put(pstableHash,fl += i)
	    	  }
	    	  else{
	    	     factorList.get += i 
	    	  }
	    	})
	    }
	    i = i + 1
	  })
	  phm
	}
	
	
		/**
	 * pstableの検索を実行します。
	 * @param pstableHashes
	 * @return lshの結果。クエリに対する近傍データの候補群
	 */
	def search(pstableHashes:List[String]):List[Int] = {
	  var result = new ListBuffer[Int]
	  
	  pstableHashes.map(h => {
	      val r = pstableHashMap.get(h)
	      r match {
	      	case None => null 
	      	case Some(x) => {
	      	  result = result ++ x
	      	}
	      }
	  })
	  
	  result.toList.sort((x,y) => x < y)
	}
}