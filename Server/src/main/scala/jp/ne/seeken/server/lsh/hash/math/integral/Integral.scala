package jp.ne.seeken.server.lsh.hash.math.integral

/**
 * 積分の機能を表すインターフェースです。
 * @author hattori_tsukasa
 */
trait Integral {
	def setInterval(start: Double,end: Double,maxStrip: Double)
	def executeIntegral: Double
}