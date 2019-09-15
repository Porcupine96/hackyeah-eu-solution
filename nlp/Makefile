version = 0.8

protoc_generate:
		rm -rf generated
		mkdir -p generated
		python -m grpc_tools.protoc -I ./eu-protobufs/api --python_out=./generated/ --grpc_python_out=./generated/ ./eu-protobufs/api/api.proto
		ls generated | xargs -I {} sed -i 's/import \([a-zA-Z0-9_]*\)_pb2/import generated\.\1_pb2/g'  generated/{}

build:
		docker build . -t docker.codeheroes.io/eu-nlp:$(version)

publish: build
		docker push docker.codeheroes.io/eu-nlp:$(version)

run:
		docker run --rm -p 8080:8080 docker.codeheroes.io/eu-nlp:$(version)

run_sh:
		docker run --rm -it  --entrypoint /bin/sh  docker.codeheroes.io/eu-nlp:$(version)
