package jp.ne.seeken.server.lsh.hash.pstable
import jp.ne.seeken.server.lsh.hash.math.integral.impl.Gauusian

/**
 * GoodHash_Probability(p1)とBadHash_Probability(p2)を計算するクラスです。p1=p(1)およびp2=p(c)は
 * 『Locality-Sensitive Hashing Scheme Based on p-stable Distributions (Datar 2004)』
 * を元に作成されています。
 *
 * @author hattori_tsukasa
 * @edit YoshihsiaMaruya
 * @param c
 * @param r
 */

class NormalHashProbability(c : Double, r : Double) {
  val gaussian = new Gauusian(0, 1)
  val start = -10000.0d
  val coffi = 2.0d / Math.sqrt(2.0d * Math.Pi)

  /**
   * 安定分布(正規分布)のGoodHashProbability (=p1)を取得します。
   * @return p1
   */
  def getGoodHashProb : Double = this.calculate_P(r)

  /**
   * 安定分布(正規分布)のBadHashProbability (=p2)を取得します。
   * @return p2
   */
  def getBadHashProb : Double = this.calculate_P(r / c)

  /**
   * 安定分布(正規分布)の場合のハッシュ値の衝突確率p(c,r)を取得します。
   * @param x
   * @return
   */
  private def calculate_P(x : Double) : Double = {
    val end = -x;
    gaussian.setInterval(start, end, 0.01);
    val secondTerm = 2.0d * gaussian.executeIntegral
    val thirdTerm = coffi * (1.0d - Math.exp(-0.5d * x * x)) / x;
    1.0d - secondTerm - thirdTerm;
  }
}