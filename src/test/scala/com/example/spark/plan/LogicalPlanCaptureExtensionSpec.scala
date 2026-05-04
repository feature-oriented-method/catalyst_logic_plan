package com.example.spark.plan

import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

class LogicalPlanCaptureExtensionSpec extends AnyFunSuite with BeforeAndAfterAll {
  private var spark: SparkSession = _

  override def beforeAll(): Unit = {
    super.beforeAll()

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
    super.afterAll()
  }

  test("runs query with extension and supports SQL decode helper contract") {
    val sql = "select 1 as value"
    val rows = spark.sql(sql).collect()
    assert(rows.length == 1)
    assert(rows.head.getInt(0) == 1)

    val configuredExtensions = spark.conf.get("spark.sql.extensions", "")
    assert(configuredExtensions.contains("com.example.spark.plan.LogicalPlanCaptureExtension"))

    val encoded = CapturedLogicalPlanPayload.encode(
      CapturedLogicalPlanPayload(
        marker = CapturedLogicalPlanPayload.Marker,
        sqlText = Some(sql),
        logicalPlanJson = """{"plan":"ok"}""",
        analyzedPlanText = "Project [1 AS value]"
      )
    )
    val decodedSql = LogicalPlanLogDecoder.extractSql(s"${CapturedLogicalPlanPayload.Marker}:$encoded")
    assert(decodedSql.contains(sql))
  }
}
