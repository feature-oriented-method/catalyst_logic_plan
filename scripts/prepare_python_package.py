from __future__ import annotations

import shutil
from pathlib import Path


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    scala_target = root / "target" / "scala-2.13"
    package_jars = root / "python" / "src" / "spark_logical_plan_capture" / "jars"
    pattern = "spark-logical-plan-capture-v2_2.13-*.jar"

    candidates = sorted(scala_target.glob(pattern))
    if not candidates:
        raise FileNotFoundError(
            f"No compiled jar found in '{scala_target}' with pattern '{pattern}'. "
            "Run `sbt clean test package` first."
        )

    package_jars.mkdir(parents=True, exist_ok=True)
    for old_jar in package_jars.glob(pattern):
        old_jar.unlink()

    src = candidates[-1]
    dst = package_jars / src.name
    shutil.copy2(src, dst)
    print(f"Copied: {src} -> {dst}")


if __name__ == "__main__":
    main()
