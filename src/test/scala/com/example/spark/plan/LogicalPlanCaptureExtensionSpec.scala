package com.example.spark.plan

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.config.Configurator
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

class LogicalPlanCaptureExtensionSpec extends AnyFunSuite with BeforeAndAfterAll {
  private var spark: SparkSession = _
  private val logger = LogManager.getLogger("com.example.spark.plan.CaptureLogicalPlanRule").asInstanceOf[Logger]
  private val appender = TestLogAppender.create("capture-test-appender")

  override def beforeAll(): Unit = {
    super.beforeAll()
    Configurator.setLevel("com.example.spark.plan.CaptureLogicalPlanRule", Level.INFO)
    appender.start()
    logger.addAppender(appender)

    spark = SparkSession
      .builder()
      .appName("logical-plan-capture-extension-spec")
      .master("local[1]")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.shuffle.partitions", "1")
      .config("spark.sql.extensions", "com.example.spark.plan.LogicalPlanCaptureExtension")
      .getOrCreate()
  }

  override def afterAll(): Unit = {
    if (spark != null) {
      spark.stop()
    }
    logger.removeAppender(appender)
    appender.stop()
    super.afterAll()
  }

  test("logs base64 serialized plan with v2 marker and restores sql") {
    TestLogAppender.clear()
    val sql = "select 1 as value"

    spark.sql(sql).collect()

    val marker = CapturedLogicalPlanPayload.Marker + ":"
    val encodedRecord = TestLogAppender.messages.find(_.contains(marker))
    assert(encodedRecord.nonEmpty, "Expected captured logical plan marker in log output")

    val encoded = encodedRecord.get.substring(encodedRecord.get.indexOf(marker) + marker.length).trim
    val payload = CapturedLogicalPlanPayload.decode(encoded)

    assert(payload.marker == CapturedLogicalPlanPayload.Marker)
    assert(payload.sqlText.contains(sql))
    assert(payload.logicalPlanJson.nonEmpty)
    assert(payload.analyzedPlanText.contains("Project"))
  }
}
