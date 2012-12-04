package jp.dip.roundvalley.scala.support


/*
 * 時間測定用のオブジェクト
 * created by maruya : date : 2012/12/1
 */
object myCountTime {
  
  def getExecutionTime(proc: => Unit): Double = {
    val start = System.currentTimeMillis()
    proc
    System.currentTimeMillis() - start
  }

  def printExecutionTime(proc: => Unit) {
    val start = System.currentTimeMillis()
    proc
    println((System.currentTimeMillis() - start) + "msec")
  }
}