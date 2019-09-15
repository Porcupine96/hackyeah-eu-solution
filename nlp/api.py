import grpc
import time
import logging
from concurrent import futures
from flask import Flask, send_from_directory

import generated.api_pb2 as proto
import generated.api_pb2_grpc as proto_grpc
from extractor import Info


class InformationExtractorServicer(proto_grpc.InformationExtractorServicer):
    def __init__(self, extractor_service):
        self.extractor_service = extractor_service

    def Extract(self, request, _context):
        results = self.extractor_service.extract(request.question, request.resultCount)

        try:
            return proto.ExtractResponse(
                results=[InformationExtractorServicer.__to_api(r) for r in results]
            )
        except Exception as e:
            logging.error(e)
            return proto.ExtractResponse(results=[])

    @staticmethod
    def __to_api(info: Info):
        return proto.ExtractResponse.Result(
            summary=info.summary,
            url=info.doc_link,
            page=info.page,
            content=info.content,
            score=info.score
        )


app = Flask(__name__, static_url_path='')


@app.route('/files/<path:path>')
def files(path):
    return send_from_directory('/data', path)


def serve(extractor_service):
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    proto_grpc.add_InformationExtractorServicer_to_server(
        InformationExtractorServicer(extractor_service), server
    )
    server.add_insecure_port("[::]:8080")
    server.start()

    logging.info("API started!")

    app.run(host="0.0.0.0")

    try:
        while True:
            time.sleep(24 * 60 * 60)
    except KeyboardInterrupt:
        server.stop(0)
