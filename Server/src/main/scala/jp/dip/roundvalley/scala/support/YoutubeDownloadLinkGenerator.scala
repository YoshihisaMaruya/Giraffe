package jp.dip.roundvalley.scala.support

import scala.io.Source
import scala.util.parsing.json.JSON
/*import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;*/

/**
 * Yotubeへのダウンロードリンクを作成
 */
object YoutubeDownloadLinkGenerator {
  def apply(youtube_url: String): String = {
    val link = (new YoutubeDownloadLinkGenerator(youtube_url)).get
    link
  }
}

class YoutubeDownloadLinkGenerator(youtube_url: String) {
  //ATTENTION: 運用中のサイトに置く
  val site_url = "http://localhost/youtube/"
  val get_url = site_url + "?url=" + youtube_url
        
  private def read: String = {
    val json = Source.fromURL(get_url).getLines.toList
    json match {
    	case Nil => {Thread.sleep(100); read}
    	case h::t => h
    }
  }

  def get(): String = {
    val json =  JSON.parseFull(read)
    val info = json.get.asInstanceOf[List[Map[String, String]]].filter(info => info.get("type").get.equals("Medium Quality - 176x144"))
    val link = info match {
      case Nil => null
      case h :: t => h.get("url").get
    }
    link
  }
}