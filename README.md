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

## Подключение в PySpark 3.5.2

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
        "com.example.spark.plan.LogicalPlanCaptureExtension"
    )
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

## Публикация в PyPI

В репозиторий добавлена Python-обертка (`pyproject.toml`, пакет `spark_logical_plan_capture`), которая:

- автоматически подключает JAR в `spark.jars`
- выставляет `spark.sql.extensions`
- предоставляет helper для извлечения SQL из лог-линии через JVM-декодер

Последовательность:

```bash
sbt clean test package
python scripts/prepare_python_package.py
python -m build
python -m twine upload dist/*
```

### GitHub CI/CD

Добавлен workflow: `.github/workflows/ci-publish-pypi.yml`

- `pull_request` и `workflow_dispatch`: делает полную проверку:
  - `sbt clean test package`
  - подготовка Python package data через `scripts/prepare_python_package.py`
  - `python -m build`
  - `twine check dist/*`
- `push` тега `v*`: после успешной сборки публикует в PyPI

Для публикации рекомендуется Trusted Publishing:

1. В PyPI (`Project settings -> Publishing`) добавить GitHub repository как trusted publisher.
2. В GitHub создать environment `pypi` (как в workflow) и при необходимости настроить protection rules.

После этого публикация делается пушем тега:

```bash
git tag v0.1.0
git push origin v0.1.0
```
