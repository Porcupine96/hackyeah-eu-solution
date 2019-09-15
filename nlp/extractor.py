import logging
from typing import List

from core import best_matches, normalized_tokens
from doc import Document
from utils import format_answer

MIN_SCORE = 0.1


class Info:
    def __init__(self, summary: str, name: str, page: int, content: str, score: float):
        self.summary = summary
        self.doc_link = "https://hack-eu.dev.codeheroes.tech/files/{}".format(name)
        self.page = page
        self.content = content
        self.score = score


class ExtractorService:
    def __init__(self,
                 docs: List[Document],
                 phrase_rank,
                 nlp):
        self.docs = docs
        self.phrase_rank = phrase_rank
        self.nlp = nlp

    def extract(self, text: str, res_count: int):
        docs, rank, nlp = self.docs, self.phrase_rank, self.nlp
        best = best_matches(text, docs, rank, nlp, n=res_count)

        results = []
        for doc, score in best:
            content = ' '.join(normalized_tokens(doc.content, nlp))
            content = format_answer(content)

            try:
                # disable the summarize since it works worse than
                # not using it at all
                #  summary = summarize(content)
                summary = content

                # if len(summary) == 0:
                #     summary = content

                info = Info(summary, doc.name, doc.page, content, score)

                if score >= MIN_SCORE:
                    results.append(info)
            except Exception as e:
                print("ERROR")
                logging.error(e)

        return results
