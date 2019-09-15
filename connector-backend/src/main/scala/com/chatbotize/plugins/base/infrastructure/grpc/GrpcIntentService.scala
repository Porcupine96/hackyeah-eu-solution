package com.chatbotize.plugins.base.infrastructure.grpc

import io.codeheroes.nlp.api.InformationExtractorGrpc.InformationExtractor
import io.codeheroes.nlp.api.{ExtractRequest, ExtractResponse, InformationExtractorGrpc}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.Future

class GrpcIntentService(host: String, port: Int) extends InformationExtractor {
  private val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build
  private val stub    = InformationExtractorGrpc.stub(channel)

  override def extract(request: ExtractRequest): Future[ExtractResponse] = stub.extract(request)
}
