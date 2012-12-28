package jp.ne.seeken.server.lsh.hash.pstable
import jp.ne.seeken.server.lsh.hash.math.random.NormalRandomNumber
import jp.dip.roundvalley.scala.support._

/**
 * ND_HashFunction を使ってハッシュ関数を生成したり、LSHの変換パラメータL,Kを取得するHandlerクラスです。
 * @author hattori_tsukasa
 * @param p1 GoodHash　probability
 * @param p2 BadHash　probability
 * @param n 対象データの総数(例:検索の場合なら全検索対象データ)
 * @param r　ハッシュ関数 (a*x+b)/r で用いられるr
 */


class h(r: Double,dimention: Int){
  private val b = r * java.lang.Math.random()
  private val a = List.fill(dimention)(NormalRandomNumber.getNormalRandomNumber).toArray
  ////h(x) = Integer( (ax +b) / r) 
  def apply(x: List[Int]): Int = {
    var sum = 0.0
    val s  = x.map(y => sum = sum + a(y))
    ( (sum + b) / r).toInt
  }
}

class NormalHashFunctionGenerator(p1 : Double, p2 : Double, n : Int, dimention : Int, r : Double) {
  //TODO : あってる？
  val rho = Math.log(1 / p1) / Math.log(1 / p2)
  ////一データに当たりに生成するハッシュベクトルの個数
  val L = Math.ceil(Math.pow(n, rho)).toInt
  ////ハッシュ関数ベクトルの次元数 (次元削減写像後の次元の数)
  val K = (Math.log(n) / Math.log(1 / p2)).toInt

  /**
   * バケット計算用のハッシュ関数群を返す
   */
  def HashFunctionsList : List[List[h]] = List.fill(L)(List.fill(K)(new h(r,dimention)))
}