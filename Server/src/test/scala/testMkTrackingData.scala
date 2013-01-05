import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import jp.ne.seeken.xml.MkTrackingData
import jp.ne.seeken.server.model.SeekenDB

/**
* YoutubeDownloadクラスのテスト
*/

@RunWith( classOf[JUnitRunner] )
class testMkTrackingData extends FunSuite{
	SeekenDB.connect
	println(MkTrackingData.get(List(1,2,3,4,5)))
}