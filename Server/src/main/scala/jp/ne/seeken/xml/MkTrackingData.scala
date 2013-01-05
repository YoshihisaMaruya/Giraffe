package jp.ne.seeken.xml

import scala.io.Source
import scala.xml.XML
import scala.xml.parsing.{ ConstructingParser, XhtmlParser }
import scala.xml.transform.{ RewriteRule, RuleTransformer }
import scala.xml.{ Node, NodeSeq, Utility }
import scala.xml.Attribute
import jp.ne.seeken.server.model.SeekenDB

/**
 * TODO: 汚すぎ。直さないと。。。
 */
object MkTrackingData {
  /*
  val i = Source.fromFile("TrackingData.xml")
  val xml = XML.loadString(i.mkString)
  i.close
  */

  def get(ids: List[Int]): String = {
    def _makeSensorCoses(ids: List[Int]): List[scala.xml.Elem] = {
      ids match {
        case Nil => Nil
        case id :: t => {
          val p = SeekenDB.findById(id)
          val xml =
            <SensorCOS>
              <SensorCosID>{ id }</SensorCosID>
              <Parameters>
                <referenceImage widthMM={ p.width.toString } heightMM={ p.height.toString }>{ id + ".jpg" }</referenceImage>
              </Parameters>
            </SensorCOS>;
          xml :: _makeSensorCoses(t)
        }
      }
    }

    def _makeCoses(ids: List[Int]): List[scala.xml.Elem] = {
      ids match {
        case Nil => Nil
        case id :: t => {
          val p = SeekenDB.findById(id)
          val xml =
            <COS>
              <COSName>MarkerlessCOS</COSName>
              <Name>{ id }</Name>
              <Fuser type="SmoothingFuser">
                <Parameters>
                  <AlphaRotation>0.6</AlphaRotation>
                  <AlphaTranslation>0.7</AlphaTranslation>
                  <KeepPoseForNumberOfFrames>5</KeepPoseForNumberOfFrames>
                </Parameters>
              </Fuser>
              <SensorSource trigger="1">
                <SensorID>FeatureTracking1</SensorID>
                <SensorCosID>{ id }</SensorCosID>
                <HandEyeCalibration>
                  <TranslationOffset>
                    <x>0</x>
                    <y>0</y>
                    <z>0</z>
                  </TranslationOffset>
                  <RotationOffset>
                    <x>0</x>
                    <y>0</y>
                    <z>0</z>
                    <w>1</w>
                  </RotationOffset>
                </HandEyeCalibration>
                <COSOffset>
                  <TranslationOffset>
                    <x>0</x>
                    <y>0</y>
                    <z>0</z>
                  </TranslationOffset>
                  <RotationOffset>
                    <x>0</x>
                    <y>0</y>
                    <z>0</z>
                    <w>1</w>
                  </RotationOffset>
                </COSOffset>
              </SensorSource>
            </COS>;
          xml :: _makeCoses(t)
        }
      }
    }

    val xml =
      <TrackingData>
        <Sensors>
          <Sensor type="FeatureBasedSensorSource" subtype="fast">
            <SensorID>FeatureTracking1</SensorID>
            <Parameters>
              <FeatureBasedParameters>
              </FeatureBasedParameters>
            </Parameters>
            {
              _makeSensorCoses(ids)
            }
          </Sensor>
        </Sensors>
        <Connections>
          {
            _makeCoses(ids)
          }
        </Connections>
      </TrackingData>;
      val w = new java.io.StringWriter
      XML.write(w, xml, "UTF-8", true, null)
      val result = w.toString
      w.close
      result
  }
}