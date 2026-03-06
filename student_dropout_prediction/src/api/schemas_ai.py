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
    question_count: int = Field(ge=1, le=10)