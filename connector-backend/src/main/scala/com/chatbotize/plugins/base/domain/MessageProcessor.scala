package com.chatbotize.plugins.base.domain

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import com.chatbotize.plugin.request.Request
import com.chatbotize.plugin.request.Request.Action
import com.chatbotize.plugin.response._
import com.chatbotize.plugins.base.infrastructure.kafka.{KafkaRequestsService, KafkaResponseService}
import com.chatbotize.protocol.request.Message.Payload
import com.chatbotize.protocol.request.{Message => RequestMessage}
import com.chatbotize.protocol.response
import com.chatbotize.protocol.response.Message.{Payload => RPayload}
import com.chatbotize.protocol.response.{MessageTag, Message => ResponseMessage}
import com.typesafe.scalalogging.StrictLogging
import io.codeheroes.nlp.api.ExtractRequest
import io.codeheroes.nlp.api.InformationExtractorGrpc.InformationExtractor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class MessageProcessor(
                        requestService: KafkaRequestsService,
                        responseService: KafkaResponseService,
                        intentService: InformationExtractor)(implicit system: ActorSystem, mat: ActorMaterializer, ec: ExecutionContext)
  extends StrictLogging {

  private def delayedRestart() =
    system.scheduler.scheduleOnce(5 seconds, () => start())

  private def finishResponse(request: Request) =
    Response(
      request.pluginId,
      request.instanceId,
      request.platformId,
      request.chatbotId,
      request.userId,
      None,
      Response.Action.LoudFinish(ActionLoudFinish()))


  private def unexpectedResponse(request: Request, message: RequestMessage) =
    Response(
      request.pluginId,
      request.instanceId,
      request.platformId,
      request.chatbotId,
      request.userId,
      None,
      Response.Action.Unexpected(ActionUnexpected(message)))

  private def messageResponse(request: Request, messages: Seq[ResponseMessage]) =
    Response(
      request.pluginId,
      request.instanceId,
      request.platformId,
      request.chatbotId,
      request.userId,
      None,
      Response.Action.Response(ActionResponse(messages))
    )

  private def start(): Unit = {
    requestService
      .source()
      .mapAsyncUnordered(32)(request =>
        request.action match {
          case Action.Empty =>
            Future.successful(List(finishResponse(request)))
          case Action.Initialize(_) =>
            Future.successful(
              List(messageResponse(
                request,
                List(ResponseMessage(MessageTag.DEFAULT, RPayload.Text(response.Text("I am connnected to huge database of data :) Ask me anything:")))))))

          case Action.Message(value) =>
            value.message.payload match {

              case Payload.Text(text) =>
                intentService.extract(ExtractRequest(text.value, 3)).map(results => {
                  logger.info(s"For ${text.value} get $results")

                  if(results.results.isEmpty){
                    List(unexpectedResponse(request, value.message))
                  }else {
                    val result = results.results.maxBy(_.score)

                    List(messageResponse(request, List(ResponseMessage(
                      MessageTag.DEFAULT, RPayload.Text(response.Text(result.summary))),
                      ResponseMessage(MessageTag.DEFAULT, RPayload.UrlButton(response.UrlButton(s"You can find more at page ${result.page} at attached document.", "Open", result.url))))),
                      finishResponse(request)
                    )
                  }
                })

              case _ =>
                Future.successful(List(unexpectedResponse(request, value.message)))
            }

          case Action.Abort(_) =>
            Future.successful(List.empty)
        })
      .mapConcat(identity)
      .toMat(responseService.sink())(Keep.right)
      .run()
      .onComplete {
        case Success(_) =>
          logger.warn(s"MessageProcessor stopped. Restarting...")
          delayedRestart()

        case Failure(ex) =>
          logger.error(s"MessageProcessor failed. Restarting...", ex)
          delayedRestart()
      }
  }

  def initialize(): Unit = start()
}
