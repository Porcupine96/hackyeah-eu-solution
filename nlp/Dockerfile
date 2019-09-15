FROM ubuntu:16.04


# Install python
RUN apt-get update
RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:deadsnakes/ppa
RUN apt-get update
RUN apt-get install -y build-essential python3.6 python3.6-dev python3-pip python3.6-venv

# Update pip
RUN python3.6 -m pip install pip --upgrade
RUN python3.6 -m pip install wheel

# Installing ML dependencies
RUN python3.6 -m pip install numpy gensim sklearn nltk spacy contractions PyPDF2 flask

# Installing other stuff
RUN python3.6 -m pip install grpcio-tools

# Install spacy model
RUN python3.6 -m spacy download en_core_web_md

RUN mkdir data
RUN mkdir app

VOLUME data

ADD *.py /app/
ADD eu-protobufs /app/eu-protobufs

RUN mkdir app/generated
RUN python3.6 -m grpc_tools.protoc -I ./app/eu-protobufs/api --python_out=./app/generated/ --grpc_python_out=./app/generated/ ./app/eu-protobufs/api/api.proto
RUN ls ./app/generated | xargs -I {} sed -i 's/import \([a-zA-Z0-9_]*\)_pb2/import generated\.\1_pb2/g'  ./app/generated/{}

EXPOSE 5000

CMD python3.6 /app/main.py
