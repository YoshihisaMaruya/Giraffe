//ORM
import org.squeryl.KeyedEntity
import org.squeryl.Schema

//sql



class ORMTest {

}

object DB extends Schema{
  val message = table[Message]
}



class Message(val user:String,val test:String,val id: Long = 0L) extends KeyedEntity[Long]