#!/usr/bin/env python

import csv
import ntpath
import os
from typing import Dict, List

from PyPDF2 import PdfFileReader
from nltk.tokenize import word_tokenize

from doc import Document


def read_tsv_map(path: str) -> Dict[str, int]:
    results = {}
    with open(path, 'r', encoding='utf-8') as f:
        tsv_reader = csv.reader(f, delimiter="\t")
        for row in tsv_reader:
            results[row[0]] = int(row[1])

    return results


def read_frequencies(path: str) -> Dict[str, int]:
    word_frequencies = read_tsv_map(path)

    for c in ".,;'!?-_":
        word_frequencies[c] = 1000000

    return word_frequencies


def read_pdf(path: str) -> List[Document]:
    page_docs = []
    with open(path, 'rb') as f:
        reader = PdfFileReader(f)

        for i, page in enumerate(reader.pages):
            doc = Document(
                name=ntpath.basename(path),
                page=i + 1,
                content=page.extractText()
            )
            page_docs.append(doc)

    return page_docs


def read_documents(data_dir: str) -> List[Document]:
    docs = []
    for filename in os.listdir(data_dir):
        if filename.endswith(".pdf"):
            path = os.path.join(data_dir, filename)
            page_docs = read_pdf(path)

            for doc in page_docs:
                docs.append(doc)
    return docs


def fix_tokens(tokens: List[str], nlp) -> List[str]:
    vocab = nlp.vocab
    result_tokens = []
    i = 0
    while i < len(tokens) - 1:
        left = vocab.strings[tokens[i]]
        right = vocab.strings[tokens[i + 1]]
        if (tokens[i].isalpha() and tokens[i + 1]) and (left not in vocab or right not in vocab):
            merged_raw = '{}{}'.format(tokens[i], tokens[i + 0])
            merged = vocab.strings[merged_raw]

            if merged in vocab:
                result_tokens.append(merged_raw)
                i += 2
            else:
                result_tokens.append(tokens[i])
                i += 1
        else:
            result_tokens.append(tokens[i])
            i += 1

    return result_tokens


def format_answer(text: str) -> str:
    text = text.replace("\n", " ")
    text = text.replace("  ", " ")
    for c in ".,;:'!?-_":
        text = text.replace(" " + c, c)
    text = text.replace("- ", "-")

    tokens = word_tokenize(text)

    if len(tokens) > 0:
        if tokens[0].isdigit():
            text = text[len(tokens[0]):].strip()

    return text
