package jp.ne.seeken.server

/**
 * thread idを管理するクラス
 */
object ThreadIdManagement {

  val max_thread_id = 100 //最大スレッド数
  private var thread_id_management: Array[Boolean] = Array.fill(max_thread_id)(true) //

  private def _free_thread_id(thread_id: Int): Int = {
    if (thread_id < max_thread_id) {
      if (this.thread_id_management(thread_id)) {
        this.thread_id_management(thread_id) = false
        thread_id
      } else this._free_thread_id(thread_id + 1)
    } else -1
  }

  //id獲得
  def acquisition(): Int = {
    this._free_thread_id(0)
  }

  //id開放
  def releace(thead_id: Int) {
    this.thread_id_management(thead_id) = true
  }
}