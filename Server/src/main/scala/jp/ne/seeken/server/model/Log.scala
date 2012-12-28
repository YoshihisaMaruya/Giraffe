package jp.ne.seeken.server.model

import net.liftweb.mapper._

class Log extends LongKeyedMapper[Log] with IdPK {
  def getSingleton = Log
  object tag extends MappedString(this, 255)
  object exe_time extends MappedString(this,255)
  object ipadder extends MappedString(this,255)
}

object Log extends Log with LongKeyedMetaMapper[Log] {

}