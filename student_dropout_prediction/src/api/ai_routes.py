import os
import json
import re
import traceback
from typing import Any, Dict

import requests
from requests.exceptions import ReadTimeout, RequestException
from dotenv import load_dotenv
from fastapi import APIRouter, HTTPException

from .schemas_ai import AskAiPayload, SummaryPayload, QuizPayload, TeacherQuizPayload

load_dotenv()

router = APIRouter(prefix="/ai", tags=["AI Tutor"])

OLLAMA_BASE_URL = (os.getenv("OLLAMA_BASE_URL") or "http://localhost:11434").rstrip("/")
OLLAMA_MODEL = (os.getenv("OLLAMA_MODEL") or "llama3.2:1b").strip()
OLLAMA_TIMEOUT_SECONDS = int((os.getenv("OLLAMA_TIMEOUT_SECONDS") or "300").strip())


def call_ollama_chat(messages: list, temperature: float = 0.2, num_predict: int = 300) -> str:
    url = f"{OLLAMA_BASE_URL}/api/chat"

    body: Dict[str, Any] = {
        "model": OLLAMA_MODEL,
        "messages": messages,
        "stream": False,
        "options": {
            "temperature": temperature,
            "num_predict": num_predict
        },
        "keep_alive": "15m"
    }

    try:
        response = requests.post(url, json=body, timeout=OLLAMA_TIMEOUT_SECONDS)
    except ReadTimeout:
        raise HTTPException(
            status_code=500,
            detail=f"Ollama response timed out after {OLLAMA_TIMEOUT_SECONDS} seconds."
        )
    except RequestException as ex:
        raise HTTPException(
            status_code=500,
            detail=f"Could not connect to Ollama at {OLLAMA_BASE_URL}. Error: {str(ex)}"
        )

    if response.status_code != 200:
        raise HTTPException(
            status_code=500,
            detail=f"Ollama error: {response.status_code} - {response.text}"
        )

    try:
        data = response.json()
    except Exception:
        raise HTTPException(
            status_code=500,
            detail=f"Invalid JSON received from Ollama: {response.text}"
        )

    content = data.get("message", {}).get("content", "")
    if not content:
        raise HTTPException(
            status_code=500,
            detail=f"Empty response from Ollama: {data}"
        )

    return content.strip()


def extract_json_from_text(text: str) -> Dict[str, Any]:
    cleaned = text.strip()

    # Remove markdown fences if present
    cleaned = re.sub(r"^```json\s*", "", cleaned, flags=re.IGNORECASE)
    cleaned = re.sub(r"^```\s*", "", cleaned)
    cleaned = re.sub(r"\s*```$", "", cleaned)
    cleaned = cleaned.strip()

    # 1) Try direct JSON parse
    try:
        return json.loads(cleaned)
    except json.JSONDecodeError:
        pass

    # 2) Try extracting the outermost JSON object if full braces exist
    start = cleaned.find("{")
    end = cleaned.rfind("}")
    candidate = None

    if start != -1 and end != -1 and end > start:
        candidate = cleaned[start:end + 1]
        try:
            return json.loads(candidate)
        except json.JSONDecodeError:
            pass

    # 3) Try recovering truncated JSON by balancing brackets/braces
    if start != -1:
        candidate = cleaned[start:]

        open_curly = candidate.count("{")
        close_curly = candidate.count("}")
        open_square = candidate.count("[")
        close_square = candidate.count("]")

        if close_square < open_square:
            candidate += "]" * (open_square - close_square)

        if close_curly < open_curly:
            candidate += "}" * (open_curly - close_curly)

        try:
            return json.loads(candidate)
        except json.JSONDecodeError:
            pass

    raise HTTPException(
        status_code=500,
        detail=f"Model did not return valid JSON. Raw output: {text}"
    )


def normalize_quiz_output(parsed: Dict[str, Any], expected_count: int) -> Dict[str, Any]:
    questions = parsed.get("questions")

    if not isinstance(questions, list):
        raise HTTPException(status_code=500, detail="Quiz JSON must contain a 'questions' array.")

    cleaned_questions = []

    for q in questions[:expected_count]:
        if not isinstance(q, dict):
            continue

        question = str(q.get("question", "")).strip()
        options = q.get("options", [])
        correct_answer = str(q.get("correctAnswer", "")).strip()
        explanation = str(q.get("explanation", "")).strip()

        if not question:
            continue

        if not isinstance(options, list):
            continue

        options = [str(opt).strip() for opt in options if str(opt).strip()]

        if len(options) < 4:
            continue

        options = options[:4]

        if correct_answer not in options:
            correct_answer = options[0]

        if not explanation:
            explanation = "This is the correct answer based on the given topic."

        cleaned_questions.append({
            "question": question,
            "options": options,
            "correctAnswer": correct_answer,
            "explanation": explanation
        })

    if not cleaned_questions:
        raise HTTPException(status_code=500, detail="No valid quiz questions were generated.")

    return {"questions": cleaned_questions}


def normalize_teacher_quiz_output(parsed: Dict[str, Any], expected_count: int, topic: str) -> Dict[str, Any]:
    normalized = normalize_quiz_output(parsed, expected_count)

    title = str(parsed.get("title", "")).strip()
    if not title:
        clean_topic = (topic or "").strip() or "Generated"
        title = f"{clean_topic} Quiz"

    return {
        "title": title,
        "questions": normalized["questions"]
    }


@router.post("/ask")
def ask_ai(payload: AskAiPayload):
    try:
        mode = (payload.mode or "").upper().strip()

        question_text = getattr(payload, "question", None)
        if question_text is None or not str(question_text).strip():
            raise HTTPException(status_code=400, detail="Question is required")

        system_prompt = (
            "You are EduVision360's academic AI assistant for university students. "
            "Answer clearly, accurately, and simply. "
            "Use student-friendly language. "
            "If course material context is provided, use it as the main source. "
            "If the question goes beyond the provided context, say that clearly and then give a helpful general explanation. "
            "Do not invent course-specific facts that were not provided."
        )

        if mode == "CHATBOT":
            system_prompt += (
                " Act like a helpful academic chatbot. "
                "Keep the tone natural, supportive, and concise."
            )
        else:
            system_prompt += (
                " Act like an AI tutor. "
                "Explain concepts step by step with short examples where useful."
            )

        context_block = ""
        if payload.context_text and payload.context_text.strip():
            context_block = f"""
Course material context:
{payload.context_text.strip()}
""".strip()

        user_prompt = f"""
Course: {payload.course_title}

{context_block}

Student question:
{question_text}
""".strip()

        result = call_ollama_chat(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.2,
            num_predict=350
        )

        return {"responseText": result}

    except HTTPException:
        raise
    except Exception as ex:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(ex))


@router.post("/summary")
def generate_summary(payload: SummaryPayload):
    try:
        system_prompt = (
            "You create clear, student-friendly academic summaries. "
            "Keep them concise, accurate, and easy to understand."
        )

        user_prompt = f"""
Topic: {payload.topic}

Source text:
{payload.source_text}

Instructions:
- Write a concise student-friendly summary
- Focus only on the most important points
- Use short paragraphs
- Keep the answer under 200 words
""".strip()

        result = call_ollama_chat(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.2,
            num_predict=250
        )

        return {"responseText": result}

    except HTTPException:
        raise
    except Exception as ex:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(ex))


@router.post("/quiz")
def generate_quiz(payload: QuizPayload):
    try:
        question_count = int(payload.question_count or 2)

        if question_count < 1:
            question_count = 1
        if question_count > 3:
            question_count = 3

        system_prompt = (
            "You generate academic multiple-choice quizzes in JSON only. "
            "Do not use markdown. Do not explain outside JSON."
        )

        user_prompt = f"""
Generate exactly {question_count} multiple-choice questions.

Topic:
{payload.topic}

Source text:
{payload.source_text}

Return ONLY valid JSON in this exact format:
{{
  "questions": [
    {{
      "question": "string",
      "options": ["option1", "option2", "option3", "option4"],
      "correctAnswer": "one of the options exactly",
      "explanation": "short explanation"
    }}
  ]
}}

Rules:
- Exactly {question_count} questions
- Exactly 4 options per question
- correctAnswer must exactly match one option
- Keep explanations short
""".strip()

        result = call_ollama_chat(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.0,
            num_predict=500
        )

        parsed = extract_json_from_text(result)
        normalized = normalize_quiz_output(parsed, question_count)

        return normalized

    except HTTPException:
        raise
    except Exception as ex:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(ex))


@router.post("/teacher-quiz")
def generate_teacher_quiz(payload: TeacherQuizPayload):
    try:
        question_count = int(payload.question_count or 5)

        if question_count < 1:
            question_count = 1
        if question_count > 10:
            question_count = 10

        clean_topic = (payload.topic or "").strip()
        clean_difficulty = (payload.difficulty or "").strip().lower()
        clean_course_title = (payload.course_title or "").strip()
        clean_source_text = (payload.source_text or "").strip()

        if not clean_source_text:
            clean_source_text = (
                f"This quiz should cover the topic '{clean_topic}' "
                f"for the course '{clean_course_title}'."
            )

        system_prompt = (
            "You are an academic quiz generation assistant for university teachers. "
            "Return strict JSON only. "
            "Do not use markdown. "
            "Do not add any text before or after the JSON. "
            "The response must be a single valid JSON object. "
            "Return exactly the requested number of questions. "
            "Each question must have exactly 4 answer options. "
            "The correctAnswer must exactly match one of the options."
        )

        user_prompt = f"""
Generate exactly {question_count} multiple-choice questions for a teacher-authored quiz draft.

Course title:
{clean_course_title}

Topic:
{clean_topic}

Difficulty:
{clean_difficulty}

Source text / grounding context:
{clean_source_text}

Return ONLY valid JSON in this exact structure:
{{
  "title": "{clean_topic} Quiz",
  "questions": [
    {{
      "question": "string",
      "options": ["option1", "option2", "option3", "option4"],
      "correctAnswer": "one of the options exactly",
      "explanation": "short explanation"
    }}
  ]
}}

Rules:
- Exactly {question_count} questions
- Exactly 4 options per question
- difficulty should influence wording and complexity
- Questions must stay relevant to the topic
- correctAnswer must exactly match one option
- Keep explanations short and clear
- Output JSON only
""".strip()

        result = call_ollama_chat(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.1,
            num_predict=700
        )

        parsed = extract_json_from_text(result)
        normalized = normalize_teacher_quiz_output(parsed, question_count, clean_topic)

        return normalized

    except HTTPException:
        raise
    except Exception as ex:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(ex))