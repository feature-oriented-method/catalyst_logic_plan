# Spark Logical Plan Capture Extension (V2)

Расширение для Spark SQL, которое:

1. Перехватывает логический Catalyst-план через `spark.sql.extensions`
2. Формирует сериализуемый payload с:
   - marker: `SPARK_LOGICAL_PLAN_CAPTURE_V2`
   - `sqlText` (если доступен)
   - `logicalPlanJson`
   - `analyzedPlanText`
3. Сериализует payload Java `ObjectOutputStream`
4. Кодирует в Base64
5. Пишет строку в лог:

`SPARK_LOGICAL_PLAN_CAPTURE_V2:<base64>`

## Сборка

```bash
sbt clean test package
```

JAR после сборки:

`target/scala-2.13/spark-logical-plan-capture-v2_2.13-0.1.0.jar`

## Подключение в PySpark 3.5.2 (Python 3.11, Scala 2.13)

```python
from pyspark.sql import SparkSession

spark = (
    SparkSession.builder
    .appName("plan-capture-demo")
    .config(
        "spark.jars",
        "/path/to/spark-logical-plan-capture-v2_2.13-0.1.0.jar"
    )
    .config(
        "spark.sql.extensions",
        "com.acme.OtherSparkExtension"
    )  # будет дополнено нашим extension, а не затерто
    .getOrCreate()
)

spark.sql("select 1 as value").show()
```

После этого в логах появится строка с маркером `SPARK_LOGICAL_PLAN_CAPTURE_V2`.

## Десериализация и восстановление SQL

Для парсинга лога в JVM-части доступен helper:

- `com.example.spark.plan.LogicalPlanLogDecoder.extractPayload(logLine)`
- `com.example.spark.plan.LogicalPlanLogDecoder.extractSql(logLine)`

Пример вызова из PySpark через JVM:

```python
line = "SPARK_LOGICAL_PLAN_CAPTURE_V2:BASE64_HERE"
sql_opt = spark._jvm.com.example.spark.plan.LogicalPlanLogDecoder.extractSql(line)
print(sql_opt)  # scala.Option[String]
```

## Тесты

Добавлены тесты:

- `CapturedLogicalPlanPayloadSpec` — roundtrip encode/decode
- `LogicalPlanCaptureExtensionSpec` — интеграционный тест SparkSession + extension + проверка логов и восстановления SQL



