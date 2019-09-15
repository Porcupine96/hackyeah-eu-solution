#!/usr/bin/env python

import ntpath
import re

MAX_ANSW_SENTS = 5

def has_link(line: str) -> bool:
    return "https://" in line


def preprocess(text):
    result_lines = []
    lines = text.split('\n')

    i = 0
    while i < len(lines):
        if re.match('^\d+\s+.*', lines[i]) and (has_link(lines[i]) or (i+1 < len(lines) and has_link(lines[i+1]))):
                i += 1
        else:
            result_lines.append(lines[i])
            i += 1

    return '\n'.join(result_lines)


def is_valid_question(sentence: str) -> bool:
    token_count = len(sentence.split(' '))
    return sentence.endswith('?') and sentence[0].isupper() and token_count > 3


def is_valid_answer_part(sentence: str) -> bool:
    tokens = sentence.split(' ')
    return len(tokens) > 1 and not tokens[0].strip().isdigit() and not sentence.startswith('http') and not sentence.startswith('/')


def is_question(sentence: str) -> bool:
    return sentence.endswith('?')


def is_apposition(sentence: str) -> str:
    return sentence.strip().startswith('(')


def cleanup(sentence: str) -> str:
    if len(sentence) > 1 and sentence[1] in '.)':
        sentence = sentence[2:].strip()

    sentence = sentence.replace('\n', ' ')
    sentence = sentence.replace('  ', ' ')
    return sentence.strip()


def generate_qa_pairs(path):
    text = read_file(path)
    text = nlp(preprocess(text))

    i = 0
    sentences = list(text.sents)
    qa = {}
    while i < len(sentences) - 1:
        s = str(sentences[i]).strip()
        if is_valid_question(s) :
            question = cleanup(s)

            answer = ''
            answer_length = 0
            i += 1
            s = str(sentences[i]).strip()

            while not is_question(s) and i < len(sentences) - 1 and answer_length < MAX_ANSW_SENTS:
                if (is_apposition(s) and answer_length == 0) or not is_valid_answer_part(s):
                    i += 1
                    s = str(sentences[i]).strip()
                else:
                    answer += ' {}'.format(cleanup(s))
                    i += 1
                    s = str(sentences[i]).strip()
                    answer_length += 1

            answer = cleanup(answer)

            if len(answer) > 0:
                if answer_length == MAX_ANSW_SENTS:
                    answer += "..."
                    filename = ntpath.basename(path)
                    link = "https://hack-eu.dev.codeheroes.tech/files/{}".format(filename)
                    answer += "\n\nYou can find out more at:\n{}".format(link)

                qa[question] = answer

        else:
            i += 1

    return qa
