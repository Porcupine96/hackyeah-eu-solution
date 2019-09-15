import nltk
import spacy
import logging

from logging import INFO
from utils import read_documents
from core import ranked_phrases
from api import serve
from extractor import ExtractorService


def main():
    logging.basicConfig(
        level=INFO,
        format="%(asctime)s.%(msecs)03d %(levelname)s %(module)s - %(funcName)s: %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    nltk.download('punkt')

    data_path = "data"

    logging.info("Loading data...")

    nlp = spacy.load("en_core_web_md")
    docs = read_documents(data_path)
    phrase_rank = ranked_phrases(docs, nlp)

    service = ExtractorService(docs, phrase_rank, nlp)

    logging.info("DONE.")

    serve(service)


if __name__ == '__main__':
    main()
