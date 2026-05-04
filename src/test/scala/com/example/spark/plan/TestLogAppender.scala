package com.example.spark.plan

import java.util.concurrent.CopyOnWriteArrayList

import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender

object TestLogAppender {
  private val events = new CopyOnWriteArrayList[String]()

  def clear(): Unit = events.clear()

  def appendMessage(message: String): Unit = {
    events.add(message)
  }

  def messages: List[String] = {
    import scala.jdk.CollectionConverters._
    events.asScala.toList
  }

  def create(name: String): Appender = {
    new AbstractAppender(name, null, null, false, Array.empty) {
      override def append(event: LogEvent): Unit = {
        appendMessage(event.getMessage.getFormattedMessage)
      }
    }
  }
}
