package jp.ne.seeken.server.lsh.hash.math.integral.impl
import jp.ne.seeken.server.lsh.hash.math.integral.Integral
import jp.ne.seeken.server.lsh.hash.math.IntegralIntervalException

/**
 * ガウス関数の区間積分を数値計算的に実行するクラスです
 * 積分法はシンプソン法で実装されています。
 *
 * @author hattori_tsukasa
 * @param average : 平均
 * @param variance : 分散
 */
class Gauusian(average : Double, variance : Double) extends Integral {

  //ガウシアンの係数。
  val cofficent = 1 / Math.sqrt(2.0 * Math.Pi) * variance

  private var start : Double = 0
  private var end : Double = 0
  private var strip : Double = 0

  //積分区間を2nとした場合のn(=pointNum)
  private var pointNum : Double = 0

  @Override
  def setInterval(start : Double, end : Double, maxStrip : Double) {
    if (start >= end) throw new IntegralIntervalException();
    this.start = start
    this.end = end

      // 積分領域をmaxstrip以下まで二分割し続ける
      def splitStrip(length : Double, pointNum : Int) : (Double, Double) = {
        if (!(maxStrip <= length)) (length, Math.pow(2, pointNum - 1))
        else splitStrip(length * 0.5, pointNum + 1)
      }

    val split = splitStrip(end - start, 0)

    this.strip = split._1
    this.pointNum = split._2
  }

  /**
   * シンプソン法による積分を実行します。
   */
  @Override
  def executeIntegral : Double = {
    val coffi = this.strip / 3.0d

      def GaussFanction(x : Double) : Double = {
        val y = x - this.average
        this.cofficent * Math.exp((-0.5 * y * y) / this.variance)
      }

      def integral(result : Double, i : Int) : Double = {
        if (!(i <= pointNum - 1)) result + coffi * 4.0d * GaussFanction(start + (2.0d * i - 1.0d) * strip)
        else integral(result + coffi * (2.0d * GaussFanction(start + 2.0d * i * strip) + 4.0d * GaussFanction(start + (2.0d * i - 1.0d) * strip)),
          i + 1)
      }

    //@TODO: 並列化
    val result = integral(coffi * (GaussFanction(this.start) + GaussFanction(this.end)), 0)
    result
  }

}