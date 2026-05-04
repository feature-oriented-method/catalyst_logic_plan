# spark-logical-plan-capture-v2

Repository: https://github.com/feature-oriented-method/catalyst_logic_plan

PyPI-пакет для PySpark 3.5.x, который подключает Spark SQL extension:

- перехватывает логические планы Catalyst
- сериализует payload в Java binary form
- кодирует payload в Base64
- пишет маркер в лог: `SPARK_LOGICAL_PLAN_CAPTURE_V2:<base64>`

## Installation

```bash
pip install spark-logical-plan-capture-v2
```

## Usage

```python
from pyspark.sql import SparkSession
from spark_logical_plan_capture import configure_spark_builder

builder = SparkSession.builder.appName("capture-demo").master("local[*]")
spark = configure_spark_builder(builder).getOrCreate()

spark.sql("select 1 as value").show()
```

## Decode SQL from log line

```python
from spark_logical_plan_capture import decode_captured_sql_from_logline

log_line = "SPARK_LOGICAL_PLAN_CAPTURE_V2:<base64>"
sql = decode_captured_sql_from_logline(spark, log_line)
print(sql)
```

## Build and publish

1. Соберите JVM jar:

```bash
sbt clean test package
```

2. Скопируйте jar в Python package data:

```bash
python scripts/prepare_python_package.py
```

3. Соберите wheel/sdist:

```bash
python -m build
```

4. Опубликуйте:

```bash
python -m twine upload dist/*
```
