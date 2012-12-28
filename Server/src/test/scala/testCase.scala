//Unit Test
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import scala.io.Source
import java.util.Properties
import java.io.InputStream
import jp.ne.seeken.server.lsh.hash.pstable.NormalHashProbability
import jp.ne.seeken.server.lsh.hash.pstable.NormalHashFunctionGenerator
import jp.ne.seeken.server.lsh.hash.pstable.NormalHashFunctionGenerator
import jp.ne.seeken.server.lsh.hash.pstable.PstableHandlerWrapper
import jp.ne.seeken.server.lsh.hash.pstable.data.PstableHammingDataStore


/**
* PstableDataHammingStoreTest.javaをscala向けに書き換え(thanks)
*/

@RunWith( classOf[JUnitRunner] )
class testCase extends FunSuite {
  //ベクトルマスターデータの読み込み
  val remove_par = (s:String) => s.substring(0, s.length()-1).substring(1)
  val drop_head = (s:String) => if(s.charAt(0) == ' ') s.drop(1).toInt else s.toInt
  val mst_data:List[List[Int]] = Source.fromFile(getClass.getClassLoader.getResource("resource/lsh.txt").getFile).getLines().toList.map(remove_par).map(s => s.split(",").map(drop_head).toList)

  //LSHパラメータの取得
  val prop = new java.util.Properties
  prop.load(getClass.getResourceAsStream("resource/lsh.properties"))
  val c = prop.getProperty("c").toDouble
  val r = prop.getProperty("r").toDouble
  println("c = " + c)
  println("r = " + r)
  
  val pp = new NormalHashProbability(c,r)
  val p1 = pp.getGoodHashProb
  val p2 = pp.getBadHashProb
  
  println("p1 = " + p1)
  println("p2 = " + p2)
  
  val n = prop.getProperty("n").toInt
  val dimension = prop.getProperty("dimension").toInt
  
  val ph = new NormalHashFunctionGenerator(p1,p2,n,dimension,r)
  
  val K = ph.K
  val L = ph.L
  println("n = " + n)
  println("d = " + dimension)
  println("K = " + K)
  println("L = " + L)
  
  //// 検索データの変換
  val start = System.currentTimeMillis()
  val phw = new PstableHandlerWrapper(ph.HashFunctionsList)
  //// バケットを生成
  val pstableDataHammingStore = new PstableHammingDataStore(mst_data,phw)
  println((start - System.currentTimeMillis()) + "[ms]")
  println("hash table size = " + pstableDataHammingStore.pstableHashMap.size)
  println("theory hash table size = " + (L * K * n))
  println("crash rate = " + (pstableDataHammingStore.pstableHashMap.size / (L * K * n)) * 100 + "%")
  
  		/**
		 * 検索の実行
		 */
		 
   val query = List(10, 59, 71, 84, 198, 207, 218, 228, 244, 311, 320, 329, 344, 354, 384, 418, 467)
   val pstableHashes =  phw.getPstableHashes(query)
   val result = pstableDataHammingStore.search(pstableHashes);
  result.foreach(r => print(r + ","))
}
