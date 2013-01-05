package jp.ne.seeken.server.model

//自作のLSHを使いたい!
import jp.ne.seeken.server.jna.LshMatcher

import scala.io.Source

/**
 * LSH DB
 */
protected object LshDB {
	/**
	 * 永続データからLSH DBを作成
	 */
	def create(permanents: List[PermanentDB]){
	  permanents.foreach(p => LshMatcher.add(p.id.toInt, p.row.toInt,p.col.toInt,p.featuresArray))
	}
}