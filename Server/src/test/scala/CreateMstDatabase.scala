//Unit Test
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import jp.ne.seeken.server.model._


/**
* PstableDataHammingStoreTest.javaをscala向けに書き換え(thanks)
*/

@RunWith( classOf[JUnitRunner] )
class CreateMstDatabase  extends FunSuite{
	SeekenDB.connect
	//SeekenDB.createFromMstImage
	SeekenDB.create(1)
	val seekenDB = new SeekenDB(0,"0.0.0.0")
	val result = seekenDB.query("/Users/maruya/Desktop/hoge/DSC_0309.JPG")
	result.foreach(println)
}