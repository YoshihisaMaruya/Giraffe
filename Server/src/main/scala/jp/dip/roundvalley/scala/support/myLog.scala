package jp.dip.roundvalley.scala.support

import java.util.Date

/*
 * log書き出しを行うクラス
 * created by maruya : date : 2012/12/1
 * emerg	サーバが稼動できないほどの重大なエラー
 * alert	critよりも重大なエラー
 * crit	重大なエラー
 * error	エラー
 * warn	警告
 * notice	通知メッセージ
 * info	サーバ情報
 * debug	デバック用の情報
 */


object myLog {
  
    //YYYYMMDD TIME
    private def getDateTime(): String =  {
      "%tY/%<tm/%<td %<tR" format new Date
    }
    
    //ログ書き出し
    private def write_log(level: String,tag: String,message: String){
      println("[" + level + "]","[TAG]=>" + tag + ",[Message]=>" + message + ",[DATE]=>" + getDateTime)
    }
    
  	def emerg(tag: String,message: String){
	  write_log("EMERG",tag,message)
	}
  	
  	def alert(tag: String,message: String){
	   write_log("ALERT",tag,message)
	}
  	
  	def crit(tag: String,message: String){
	  write_log("CRIT",tag,message)
	}
  	
  	def error(tag: String,message: String){
	  write_log("ERROR",tag,message)
	}
  	
  	def warn(tag: String,message: String){
	  write_log("WARN",tag,message)
	}
  	
  	def notice(tag: String,message: String){
	  write_log("NOTICE",tag,message)
	}
  	  	
	def info(tag: String,message: String){
	 write_log("INFO",tag,message)
	}
	
	def debug(tag: String,message: String){
	  write_log("DEBUG",tag,message)
	}
	
	def exe_time(thread_id: Int,ipadder: String,exe_name: String,exe_time: String){
	  write_log("EXE_TIME",exe_name,"thread_id=" + thread_id + ",ipadder=" + ipadder + ",exe_time=" + exe_time + "[msec]")
	}
}