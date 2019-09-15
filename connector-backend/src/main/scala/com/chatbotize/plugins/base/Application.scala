package com.chatbotize.plugins.base

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.chatbotize.plugins.base.Application.ServicesConfig
import com.chatbotize.plugins.base.domain.{Defs, MessageProcessor}
import com.chatbotize.plugins.base.infrastructure.grpc.GrpcIntentService
import com.chatbotize.plugins.base.infrastructure.kafka.{KafkaRequestsService, KafkaResponseService}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor

object Application {
  final case class ServicesConfig(kafka: KafkaConfig, intent: IntentConfig)
  final case class KafkaConfig(host: String, port: Int) {
    def stringify: String = s"$host:$port"
  }
  final case class IntentConfig(host: String, port: Int)

}

class Application(config: Config, services: ServicesConfig) extends StrictLogging {

  private implicit val system: ActorSystem          = ActorSystem("ApplicationActorSystem", config)
  private implicit val mat: ActorMaterializer       = ActorMaterializer()
  private implicit val ec: ExecutionContextExecutor = system.dispatcher

  private val requestSource = new KafkaRequestsService(
    groupId = s"plugin-${Defs.PLUGIN_ID}",
    clientId = UUID.randomUUID().toString,
    bootstrapServers = services.kafka.stringify,
    topic = s"plugin-${Defs.PLUGIN_ID}-requests"
  )
  private val responseSink = new KafkaResponseService(
    topic = "plugin-responses",
    bootstrapServers = services.kafka.stringify
  )

  private val intentService = new GrpcIntentService(services.intent.host, services.intent.port)

  private val messageProcessor = new MessageProcessor(requestSource, responseSink, intentService)

  def start(): Unit =
    messageProcessor.initialize()
}
