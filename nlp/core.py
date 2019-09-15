import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from spacy.attrs import IS_STOP
from nltk.tokenize import word_tokenize

from doc import Document
from typing import List
from utils import fix_tokens


def noun_phrases(text: str, nlp):
    doc = nlp(text)
    phrases = []

    for spans in doc.noun_chunks:
        for token in spans:
            if not token.check_flag(IS_STOP):
                phrases.append(str(token))
    return phrases


def best_matches(text: str,
                 documents: List[Document],
                 phrase_rank,
                 nlp,
                 n=3):
    key_phrases = noun_phrases(text, nlp)

    document_scores = {}
    for doc in documents:
        score = 0
        for phrase in key_phrases:
            rank = phrase_rank[doc]
            score += rank.get(phrase, 0)
        document_scores[doc] = score

    top = sorted(document_scores.items(), key=lambda x: x[1], reverse=True)
    return top[:n]


def filter_unwanted(tokens: List[str]) -> List[str]:
    def cond(t):
        for c in "&\/_":
            if t.startswith(c):
                return False
        return True

    result_tokens = [t for t in tokens if cond(t)]
    return result_tokens


def normalized_tokens(content: str, nlp) -> List[str]:
    tokens = word_tokenize(content)
    tokens = filter_unwanted(tokens)
    tokens = fix_tokens(tokens, nlp)
    return tokens


def ranked_phrases(documents, nlp):
    tokenizer = lambda t: normalized_tokens(t, nlp)
    tfidf = TfidfVectorizer(tokenizer=tokenizer, stop_words='english')
    contents = [d.content for d in documents]

    X = tfidf.fit_transform(contents)

    results = {}
    for doc, doc_tfidf in zip(documents, X):
        doc_tfidf = list(np.asarray(doc_tfidf.todense())[0])
        ranked = dict(zip(tfidf.get_feature_names(), doc_tfidf))
        results[doc] = ranked
    return results
