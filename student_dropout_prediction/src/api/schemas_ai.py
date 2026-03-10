from pydantic import BaseModel, Field


class AskAiPayload(BaseModel):
    mode: str
    course_title: str
    question: str
    context_text: str | None = None


class SummaryPayload(BaseModel):
    topic: str
    source_text: str


class QuizPayload(BaseModel):
    topic: str
    source_text: str
    question_count: int = Field(default=2, ge=1, le=10)


class TeacherQuizPayload(BaseModel):
    course_title: str
    topic: str
    difficulty: str
    question_count: int = Field(default=5, ge=1, le=10)
    source_text: str | None = None