package com.example.spark.plan

object LogicalPlanLogDecoder {
  private val Prefix = CapturedLogicalPlanPayload.Marker + ":"

  def extractPayload(logLine: String): CapturedLogicalPlanPayload = {
    val idx = logLine.indexOf(Prefix)
    require(idx >= 0, s"Marker '${CapturedLogicalPlanPayload.Marker}' not found in log line")
    val encoded = logLine.substring(idx + Prefix.length).trim
    CapturedLogicalPlanPayload.decode(encoded)
  }

  def extractSql(logLine: String): Option[String] = {
    extractPayload(logLine).sqlText
  }
}
