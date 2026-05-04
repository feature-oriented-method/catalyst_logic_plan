package com.example.spark.plan

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Base64

final case class CapturedLogicalPlanPayload(
    marker: String,
    sqlText: Option[String],
    logicalPlanJson: String,
    analyzedPlanText: String)
    extends Serializable

object CapturedLogicalPlanPayload {
  val Marker: String = "SPARK_LOGICAL_PLAN_CAPTURE_V2"

  def encode(payload: CapturedLogicalPlanPayload): String = {
    val bos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bos)
    try {
      oos.writeObject(payload)
      oos.flush()
      Base64.getEncoder.encodeToString(bos.toByteArray)
    } finally {
      oos.close()
      bos.close()
    }
  }

  def decode(base64: String): CapturedLogicalPlanPayload = {
    val raw = Base64.getDecoder.decode(base64)
    val bis = new ByteArrayInputStream(raw)
    val ois = new ObjectInputStream(bis)
    try {
      ois.readObject().asInstanceOf[CapturedLogicalPlanPayload]
    } finally {
      ois.close()
      bis.close()
    }
  }
}
