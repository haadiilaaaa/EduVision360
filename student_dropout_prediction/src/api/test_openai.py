import os
import json
import traceback

from dotenv import load_dotenv
from fastapi import APIRouter, HTTPException
from openai import OpenAI

from .schemas_ai import AskAiPayload, SummaryPayload, QuizPayload

load_dotenv()

router = APIRouter(prefix="/ai", tags=["AI Tutor"])

OPENAI_API_KEY = (os.getenv("OPENAI_API_KEY") or "").strip()
OPENAI_MODEL = (os.getenv("OPENAI_MODEL") or "gpt-5-mini").strip()

if not OPENAI_API_KEY:
    raise RuntimeError("OPENAI_API_KEY is not set")

client = OpenAI(
    api_key=OPENAI_API_KEY,
    timeout=60.0
)


@router.post("/ask")
def ask_ai(payload: AskAiPayload):
    try:
        system_prompt = (
            "You are an academic support assistant for university students. "
            "Answer clearly, accurately, and simply. "
            "If the question is ambiguous, give the best educational answer based on the course title. "
        )

        if payload.mode == "CHATBOT":
            system_prompt += "Act like a helpful academic chatbot."
        else:
            system_prompt += "Act like an AI tutor and explain concepts step by step with examples."

        prompt = f"""
Course: {payload.course_title}
Student question: {payload.question}
"""

        response = client.responses.create(
            model=OPENAI_MODEL,
            input=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": prompt}
            ]
        )

        return {
            "responseText": response.output_text
        }

    except Exception as ex:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(ex))


@router.post("/summary")
def generate_summary(payload: SummaryPayload):
    try:
        prompt = f"""
Create a clear, student-friendly summary.

Topic: {payload.topic}

Source text:
{payload.source_text}
"""

        response = client.responses.create(
            model=OPENAI_MODEL,
            input=[
                {
                    "role": "system",
                    "content": "You generate concise but helpful academic summaries for students."
                },
                {"role": "user", "content": prompt}
            ]
        )

        return {
            "responseText": response.output_text
        }

    except Exception as ex:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(ex))


@router.post("/quiz")
def generate_quiz(payload: QuizPayload):
    try:
        prompt = f"""
Generate {payload.question_count} multiple-choice questions based on the topic and source text.

Topic: {payload.topic}

Source text:
{payload.source_text}

Return valid JSON only in this exact structure:
{{
  "questions": [
    {{
      "question": "string",
      "options": ["A", "B", "C", "D"],
      "correctAnswer": "string",
      "explanation": "string"
    }}
  ]
}}
"""

        response = client.responses.create(
            model=OPENAI_MODEL,
            input=[
                {
                    "role": "system",
                    "content": "You generate academic quizzes in strict JSON format."
                },
                {"role": "user", "content": prompt}
            ]
        )

        output_text = response.output_text.strip()
        parsed = json.loads(output_text)

        return parsed

    except Exception as ex:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(ex))