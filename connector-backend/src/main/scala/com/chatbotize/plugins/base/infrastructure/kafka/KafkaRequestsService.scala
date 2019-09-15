package com.chatbotize.plugins.base.infrastructure.kafka

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import com.chatbotize.plugin.request.Request
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

class KafkaRequestsService(groupId: String, clientId: String, bootstrapServers: String, topic: String)(
    implicit system: ActorSystem) {

  private val settings = ConsumerSettings(system, new StringDeserializer, new ByteArrayDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withClientId(clientId)
    .withGroupId(groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")

  def source(): Source[Request, NotUsed] =
    Consumer
      .plainSource(settings, Subscriptions.topics(topic))
      .map(record =>
        try {
          Some(Request.parseFrom(record.value()))
        } catch {
          case ex: Throwable =>
            system.log.error(ex, s"Error while parsing value of record $record. Omitting...")
            None
      })
      .mapConcat(_.toList)
      .mapMaterializedValue(_ => NotUsed)

}
