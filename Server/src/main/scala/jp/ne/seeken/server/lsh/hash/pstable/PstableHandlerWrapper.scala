package jp.ne.seeken.server.lsh.hash.pstable
import java.security.MessageDigest
import jp.dip.roundvalley.scala.support._
import scala.collection.mutable.ListBuffer

object PstableHandlerWrapper {
  
  private def getHaxDigest(digest: Array[Byte]): String = {
    val buf = new StringBuffer("")
    digest.foreach(d => {
      val n = d & 0xFF
      if (n < 16) buf.append("0")
      buf.append(Integer.toString(n,16))
    })
    buf.toString.substring(0,24)
  }
  /**
   *
   * @param hashVector : HashFunctionVectorGeneratorで生成されるLSHハッシュ値
   * @param hfvNum : HashFunctionVectorGeneratorの識別子
   * @return
   */
  def hashKeyTransForm(hashVector : List[Int], hfvNum : Int) : String = {
    val messageDigest =
      try {
        MessageDigest.getInstance("MD5")
      } catch {
        case _ => null
      }
      val a = "a"
      val sb = new StringBuilder
      sb.append(hfvNum + a)
      hashVector.foreach(hash => sb.append(hash + a))
      val binary = sb.toString.getBytes
      val hash = messageDigest.digest(binary)
      PstableHandlerWrapper.getHaxDigest(hash)
  }
}

class PstableHandlerWrapper(HashFunctionList : List[List[h]]) {
  /**
   * マスターデータから、ハッシュテーブルを作成
   */
  def getPstableHashes(x : List[Int]) : List[String] = {
     def _getPstableHashes(hashFunction : List[h]) : String = {
            val hashVector = hashFunction.map(hash => hash(x))
            PstableHandlerWrapper.hashKeyTransForm(hashVector, 0)
     }
     
      var lb = new ListBuffer[String]
      HashFunctionList.par.foreach(x => { lb += _getPstableHashes(x)})
      lb.toList
  }
}