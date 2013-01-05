import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import jp.ne.seeken.YoutubeDownload
import java.net.Socket
import java.io.DataOutputStream
import jp.ne.seeken.server.model.SeekenDB
import scala.tools.scalap.scalax.rules.Input
import java.io.OutputStream
import java.io.InputStream
import java.io.File
import java.io.FileOutputStream

/**
* YoutubeDownloadクラスのテスト
*/

@RunWith( classOf[JUnitRunner] )
class testYoutubeDownload  extends FunSuite{
	SeekenDB.connect //本来はここでやるべきではない
  
	val port = 8001
	val yd = new YoutubeDownload(port)
	yd.start
	
	Thread.sleep(5000)
	
	val socket = new Socket("localhost",port)

	var out: DataOutputStream = null
	var in: InputStream = null
	var result: OutputStream = null
	try{
	  out = new DataOutputStream(socket.getOutputStream())
	  in = socket.getInputStream()
	  result = new FileOutputStream(new File("/Users/maruya/Desktop/hoge/test.3gp"))
	  out.writeInt(1)
	  
	  val buf = new Array[Byte](1024)
	  def download(in: InputStream,out: OutputStream){
		 val len = in.read(buf)
		 if(len > 0){
		   out.write(buf,0,len)
		   download(in, out)
		 }
	  }
	  download(in, result)
	}
	catch{
	  case e => e.printStackTrace
	}
	finally {
	    out.close
	    in.close
		socket.close
	}

}