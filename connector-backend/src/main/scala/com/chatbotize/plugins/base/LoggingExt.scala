package com.chatbotize.plugins.base

import com.trueaccord.scalapb.GeneratedMessage
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

trait LoggingExt {

  implicit class FutureExt[T](future: Future[T]) extends StrictLogging {
    def logFailure(request: GeneratedMessage)(implicit ec: ExecutionContext): Future[T] = {
      future.transform(identity, ex => {
        logger.error(s"Error while executing request $request", ex)
        ex
      })
    }
  }

}
