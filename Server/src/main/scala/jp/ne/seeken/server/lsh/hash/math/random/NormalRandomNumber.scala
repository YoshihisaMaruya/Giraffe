package jp.ne.seeken.server.lsh.hash.math.random

/**
 * Box-Muller transformによる正規乱数を発生させるクラスです。
 * @author hattori_tsukasa
 *
 */
object NormalRandomNumber {
  private var isOddNum: Boolean = true
  private var nrn1: Double = 0
  private var nrn2: Double = 0

  	/**
	 * Box-Muller transformによる正規乱数を取得します。
	 * @return　正規乱数
	 */
  def getNormalRandomNumber: Double = {
    if(isOddNum){
			val alpha = java.lang.Math.random();
			val beta  = java.lang.Math.random();
	
			nrn1 = Math.sqrt( -2.0d*Math.log(alpha) ) * Math.sin( 2.0d * Math.Pi * beta );
			nrn2 = Math.sqrt( -2.0d*Math.log(alpha) ) * Math.cos( 2.0d * Math.Pi * beta );
			isOddNum = false;
			return nrn1;
		}
		else{
			isOddNum = true;
			return nrn2;
		}
  }
}