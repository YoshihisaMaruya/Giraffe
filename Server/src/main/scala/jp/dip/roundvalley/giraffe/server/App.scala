package jp.dip.roundvalley.giraffe.server

import scala.io.Source
import scala.xml.XML
import scala.xml.parsing.{ConstructingParser,XhtmlParser}
import scala.xml.NodeSeq

/*
 * app.xmlから固定データの読み込み
 */

object App {
	val app_sorce = Source.fromFile(getClass.getClassLoader.getResource("app.xml").getFile)
	val app_xml = XML.loadString( app_sorce.getLines.mkString )
	
	private def _getElem(node:NodeSeq,tag: List[String]): NodeSeq = {
	  tag match{
	    case Nil => node
	    case h::t =>{
	      val root = node \ {h}
	      this._getElem(root,t)
	    }
	   }
	}
	
	def get(tag: String):NodeSeq = {
	  val tags = (tag split '.').toList
	  val root = app_xml \\ "app"
	  this._getElem(root,tags)
	}
    
}