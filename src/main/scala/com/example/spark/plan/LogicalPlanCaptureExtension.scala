package com.example.spark.plan

import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.SparkSessionExtensions
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan

class LogicalPlanCaptureExtension extends (SparkSessionExtensions => Unit) with Serializable {
  override def apply(extensions: SparkSessionExtensions): Unit = {
    extensions.injectPostHocResolutionRule { _ =>
      CaptureLogicalPlanRule
    }
  }
}

object CaptureLogicalPlanRule extends Rule[LogicalPlan] {
  private val logger = LogManager.getLogger("com.example.spark.plan.CaptureLogicalPlanRule")

  override def apply(plan: LogicalPlan): LogicalPlan = {
    val payload = CapturedLogicalPlanPayload(
      marker = CapturedLogicalPlanPayload.Marker,
      sqlText = plan.origin.sqlText,
      logicalPlanJson = plan.toJSON,
      analyzedPlanText = plan.treeString
    )

    val encoded = CapturedLogicalPlanPayload.encode(payload)
    logger.info(s"${CapturedLogicalPlanPayload.Marker}:$encoded")
    plan
  }
}
