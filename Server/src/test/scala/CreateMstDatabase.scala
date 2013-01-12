//Unit Test
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import jp.ne.seeken.server.model._
import jp.ne.seeken.server.SeekenServer
import java.io.FileInputStream
import java.io.ObjectInputStream
import jp.ne.seeken.serializer.RequestSerializer


/**
* mst imageをもとに、ダミーのデータベースを作成
*/

@RunWith( classOf[JUnitRunner] )
class CreateMstDatabase  extends FunSuite{
	SeekenServer.init(true)
	
	//テストコネクション
	val seekenDB = new SeekenDB(0,"0.0.0.0")
	
	
	//適当にクエリを投げてみる
	
	val c = SeekenDB.findById(1)
	val result = seekenDB.query(c.width,c.height,c.grayArray,"gray")
	result.foreach(println)
	
	val fi = new FileInputStream("/Users/maruyayoshihisa/Desktop/hoge/request.obj")
	val oi = new ObjectInputStream(fi)
	val request = oi.readObject().asInstanceOf[RequestSerializer]
	
	val r2 = seekenDB.query(request)
	
	r2.foreach(println)

}