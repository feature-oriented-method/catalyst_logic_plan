package com.example.spark.plan

import org.scalatest.funsuite.AnyFunSuite

class CapturedLogicalPlanPayloadSpec extends AnyFunSuite {

  test("encode/decode roundtrip keeps payload fields") {
    val source = CapturedLogicalPlanPayload(
      marker = CapturedLogicalPlanPayload.Marker,
      sqlText = Some("select 1 as value"),
      logicalPlanJson = """[{"class":"Project"}]""",
      analyzedPlanText = "Project [1 AS value#1]"
    )

    val encoded = CapturedLogicalPlanPayload.encode(source)
    val decoded = CapturedLogicalPlanPayload.decode(encoded)

    assert(decoded == source)
  }
}
