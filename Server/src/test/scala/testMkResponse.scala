import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import jp.ne.seeken.server.ClinetManagerThread
import jp.ne.seeken.server.model._
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.DataBufferByte
import java.awt.image.ComponentSampleModel
import java.awt.image.DataBuffer
import java.awt.image.Raster


/**
* PstableDataHammingStoreTest.javaをscala向けに書き換え(thanks)
*/

@RunWith( classOf[JUnitRunner] )
class testMkResponse extends FunSuite{
  SeekenDB.connect
  val c = new ClinetManagerThread(1,null)
   val r = c._makeResponse(List(1,2,3,4,10))
   val s  = SeekenDB.findById(10)
   println(s.image_name)
   val bRGB = r.getImage(4)
   val height = r.getHeight(4)
   val width = r.getWidth(4)
   
   /*val write = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR)

   val iRGB_length = bRGB.length / 3
    val iRGB = new Array[Int](iRGB_length)
    var count = 0
    for (i <- 0 until iRGB_length) {
      val r = bRGB(count)
      val g = bRGB(count + 1)
      val b = bRGB(count + 2)
      iRGB(i) = (0xff000000 | r << 16 | g << 8 | b)
      count = count + 3
    }
  
    
  for(i <- 0 until width){
    for(j <- 0 until height){
    for(j <- 0 until height){
      write.setRGB(i, j, bRGB())
    }
  }
  write.setRGB(0, 0, width, height, iRGB,0,width);*/
    val image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
  	val buffer = new DataBufferByte(bRGB,bRGB.length)

  	//The most difficult part of awt api for me to learn
  	val sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 3, width*3, Array(0,1,2));
  	val raster = Raster.createRaster(sampleModel, buffer, null);
  	image.setData(raster);
  	val f2 = new File("/Users/maruya/Desktop/hoge/ret.jpg");
  	ImageIO.write(image, "jpg", f2);
  
  
  /**
   * rgbのバイト配列をint配列に直す
   * @param bRGB : RGB配列(バイト)
   * @return iRGB : RGB配列(int) * alphaは0
   */
  private def rgbByteArraytoIntArray(bRGB: Array[Byte]): Array[Int] = {
    val iRGB_length = bRGB.length / 3
    val iRGB = new Array[Int](iRGB_length)
    var count = 0
    for (i <- 0 until iRGB_length) {
      val r = bRGB(count)
      val g = bRGB(count + 1)
      val b = bRGB(count + 2)
      iRGB(i) = (0xff000000 | r << 16 | g << 8 | b)
      count = count + 3
    }
    iRGB
  }
   

}