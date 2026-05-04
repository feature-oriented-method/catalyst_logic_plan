from __future__ import annotations

from pathlib import Path
from typing import Optional

from pyspark.sql import SparkSession

LOG_MARKER = "SPARK_LOGICAL_PLAN_CAPTURE_V2"
EXTENSION_CLASS = "com.example.spark.plan.LogicalPlanCaptureExtension"
DECODER_CLASS = "com.example.spark.plan.LogicalPlanLogDecoder"
_JAR_GLOB = "spark-logical-plan-capture-v2_2.13-*.jar"


def jar_path() -> str:
    jars_dir = Path(__file__).resolve().parent / "jars"
    matches = sorted(jars_dir.glob(_JAR_GLOB))
    if not matches:
        raise FileNotFoundError(
            "Bundled Spark extension JAR was not found in package data. "
            "Expected file matching pattern "
            f"'{_JAR_GLOB}' in '{jars_dir}'."
        )
    return str(matches[-1])


def configure_spark_builder(builder: SparkSession.Builder) -> SparkSession.Builder:
    current_jars = builder._options.get("spark.jars", "")
    current_extensions = builder._options.get("spark.sql.extensions", "")
    ext_jar = jar_path()
    merged_jars = ",".join([v for v in [current_jars, ext_jar] if v])
    extension_values = [v.strip() for v in current_extensions.split(",") if v.strip()]
    if EXTENSION_CLASS not in extension_values:
        extension_values.append(EXTENSION_CLASS)
    merged_extensions = ",".join(extension_values)
    builder = builder.config("spark.jars", merged_jars)
    builder = builder.config("spark.sql.extensions", merged_extensions)
    return builder


def decode_captured_sql_from_logline(
    spark: SparkSession, log_line: str
) -> Optional[str]:
    jvm = spark._jvm
    decoder = getattr(getattr(jvm.com.example.spark.plan, "LogicalPlanLogDecoder$"), "MODULE$")
    scala_option = decoder.extractSql(log_line)
    return scala_option.get() if scala_option.isDefined() else None
