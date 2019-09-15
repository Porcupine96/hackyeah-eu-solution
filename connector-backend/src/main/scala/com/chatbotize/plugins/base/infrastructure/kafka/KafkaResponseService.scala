package com.chatbotize.plugins.base.infrastructure.kafka

import java.util.Properties

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.{Done, NotUsed}
import com.chatbotize.plugin.response.Response
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.{Future, Promise}

class KafkaResponseService(topic: String, bootstrapServers: String)(implicit system: ActorSystem) {

  private val settings = ProducerSettings(system, new StringSerializer, new ByteArraySerializer)
    .withBootstrapServers(bootstrapServers)

  private val properties = {
    val props = new Properties()
    props.put("bootstrap.servers", bootstrapServers)
    props.put("acks", "all")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
    props
  }

  private val producer = new KafkaProducer[String, Array[Byte]](properties)

  private def getKey(response: Response): String = s"${response.platformId}-${response.chatbotId}-${response.userId}"

  def sink(): Sink[Response, Future[Done]] =
    Flow[Response]
      .map(response => new ProducerRecord[String, Array[Byte]](topic, getKey(response), response.toByteArray))
      .toMat(Producer.plainSink[String, Array[Byte]](settings))(Keep.right[NotUsed, Future[Done]])

  def respond(response: Response): Future[Done] = {
    val record = new ProducerRecord[String, Array[Byte]](
      topic,
      getKey(response),
      response.toByteArray
    )

    val promise = Promise[Done]

    producer.send(
      record,
      new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit =
          if (exception != null) {
            promise.failure(exception)
          } else {
            promise.success(Done)
          }
      }
    )

    promise.future
  }
}
