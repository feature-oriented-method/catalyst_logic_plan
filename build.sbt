ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.14"

lazy val sparkVersion = "3.5.2"

lazy val root = (project in file("."))
  .settings(
    name := "spark-logical-plan-capture-v2",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
      "org.apache.logging.log4j" % "log4j-api" % "2.23.1",
      "org.apache.logging.log4j" % "log4j-core" % "2.23.1" % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    Test / fork := true,
    Test / javaOptions ++= Seq(
      "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens=java.base/java.nio=ALL-UNNAMED"
    )
  )
