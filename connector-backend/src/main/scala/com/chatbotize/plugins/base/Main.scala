package com.chatbotize.plugins.base

import com.chatbotize.plugins.base.Application._
import com.typesafe.config.ConfigFactory

object Main extends App {

  private val config = ConfigFactory.load("default.conf")
  private val services = ServicesConfig(
    KafkaConfig(config.getString("kafka.host"), config.getInt("kafka.port")),
    IntentConfig(config.getString("intent-service.host"), config.getInt("intent-service.port"))
  )
  private val application = new Application(config, services)

  application.start()
}
