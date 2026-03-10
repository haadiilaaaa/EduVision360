import React, { useEffect, useState } from "react";
import {
  getMyTeacherCourses,
  generateTeacherQuizDraft,
  saveTeacherQuiz,
  getTeacherQuizzesByCourse
} from "../services/academicService";
import { Brain, ListChecks, Save, BookOpen, FileText } from "lucide-react";

function TeacherQuizGenerator() {
  const [courses, setCourses] = useState([]);
  const [savedQuizzes, setSavedQuizzes] = useState([]);

  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [topic, setTopic] = useState("");
  const [difficulty, setDifficulty] = useState("MEDIUM");
  const [questionCount, setQuestionCount] = useState(5);
  const [sourceText, setSourceText] = useState("");

  const [draft, setDraft] = useState(null);
  const [saveTitle, setSaveTitle] = useState("");
  const [saveDescription, setSaveDescription] = useState("");

  const [loadingAction, setLoadingAction] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadCourses = async () => {
    try {
      const res = await getMyTeacherCourses();
      const data = res.data || [];
      setCourses(data);

      if (data.length > 0 && !selectedCourseId) {
        setSelectedCourseId(data[0].id);
      }
    } catch (err) {
      console.error(err);
      setError("Failed to load teacher courses.");
    }
  };

  const loadSavedQuizzes = async (courseId) => {
    if (!courseId) {
      setSavedQuizzes([]);
      return;
    }

    try {
      const res = await getTeacherQuizzesByCourse(courseId);
      setSavedQuizzes(res.data || []);
    } catch (err) {
      console.error(err);
      setSavedQuizzes([]);
    }
  };

  useEffect(() => {
    loadCourses();
    // eslint-disable-next-line
  }, []);

  useEffect(() => {
    if (selectedCourseId) {
      loadSavedQuizzes(selectedCourseId);
    }
    // eslint-disable-next-line
  }, [selectedCourseId]);

  const resetMessages = () => {
    setMessage("");
    setError("");
  };

  const handleGenerateDraft = async () => {
    resetMessages();

    if (!selectedCourseId || !topic.trim()) {
      setError("Course and topic are required.");
      return;
    }

    try {
      setLoadingAction("generate");

      const res = await generateTeacherQuizDraft({
        courseId: selectedCourseId,
        topic: topic.trim(),
        difficulty,
        questionCount: Number(questionCount),
        sourceText: sourceText.trim()
      });

      const generated = res.data;

      setDraft({
        aiInteractionId: generated.aiInteractionId,
        courseId: generated.courseId,
        courseCode: generated.courseCode,
        courseTitle: generated.courseTitle,
        title: generated.title || `${topic.trim()} Quiz`,
        topic: generated.topic || topic.trim(),
        difficulty: generated.difficulty || difficulty,
        questions: (generated.questions || []).map((q) => ({
          questionText: q.question || "",
          options: Array.isArray(q.options) ? q.options : ["", "", "", ""],
          correctAnswer: q.correctAnswer || "",
          explanation: q.explanation || ""
        }))
      });

      setSaveTitle(generated.title || `${topic.trim()} Quiz`);
      setSaveDescription("AI-assisted quiz draft reviewed by teacher");
      setMessage("Quiz draft generated successfully.");
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || err?.response?.data?.detail || "Failed to generate quiz draft.");
    } finally {
      setLoadingAction("");
    }
  };

  const handleQuestionChange = (index, field, value) => {
    setDraft((prev) => {
      if (!prev) return prev;

      const updatedQuestions = [...prev.questions];
      updatedQuestions[index] = {
        ...updatedQuestions[index],
        [field]: value
      };

      return {
        ...prev,
        questions: updatedQuestions
      };
    });
  };

  const handleOptionChange = (questionIndex, optionIndex, value) => {
    setDraft((prev) => {
      if (!prev) return prev;

      const updatedQuestions = [...prev.questions];
      const updatedOptions = [...updatedQuestions[questionIndex].options];
      updatedOptions[optionIndex] = value;

      updatedQuestions[questionIndex] = {
        ...updatedQuestions[questionIndex],
        options: updatedOptions
      };

      return {
        ...prev,
        questions: updatedQuestions
      };
    });
  };

  const handleRemoveQuestion = (index) => {
    setDraft((prev) => {
      if (!prev) return prev;

      const updatedQuestions = prev.questions.filter((_, i) => i !== index);

      return {
        ...prev,
        questions: updatedQuestions
      };
    });
  };

  const validateDraftBeforeSave = () => {
    if (!draft) return "No quiz draft to save.";
    if (!saveTitle.trim()) return "Quiz title is required.";
    if (!draft.questions || draft.questions.length === 0) return "At least one question is required.";

    for (let i = 0; i < draft.questions.length; i++) {
      const q = draft.questions[i];

      if (!q.questionText?.trim()) {
        return `Question ${i + 1} text is required.`;
      }

      const cleanedOptions = (q.options || []).map((o) => o.trim()).filter(Boolean);
      if (cleanedOptions.length < 2) {
        return `Question ${i + 1} must have at least 2 options.`;
      }

      if (!q.correctAnswer?.trim()) {
        return `Question ${i + 1} must have a correct answer.`;
      }

      if (!cleanedOptions.includes(q.correctAnswer.trim())) {
        return `Question ${i + 1} correct answer must match one of the options exactly.`;
      }
    }

    return null;
  };

  const handleSaveQuiz = async () => {
    resetMessages();

    const validationError = validateDraftBeforeSave();
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setLoadingAction("save");

      await saveTeacherQuiz({
        courseId: draft.courseId,
        aiInteractionId: draft.aiInteractionId,
        title: saveTitle.trim(),
        description: saveDescription.trim(),
        topic: draft.topic,
        difficulty: draft.difficulty,
        questions: draft.questions.map((q) => ({
          questionText: q.questionText.trim(),
          options: q.options.map((o) => o.trim()).filter(Boolean),
          correctAnswer: q.correctAnswer.trim(),
          explanation: q.explanation?.trim() || ""
        }))
      });

      setMessage("Quiz saved successfully.");
      await loadSavedQuizzes(draft.courseId);

      setDraft(null);
      setSaveTitle("");
      setSaveDescription("");
      setTopic("");
      setSourceText("");
      setQuestionCount(5);
      setDifficulty("MEDIUM");
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || err?.response?.data?.detail || "Failed to save quiz.");
    } finally {
      setLoadingAction("");
    }
  };

  return (
    <div className="teacher-quiz-page">
      <style>{`
        .teacher-quiz-page {
          color: var(--text-main, #e5e7eb);
          animation: fadeIn 0.3s ease;
        }

        .quiz-grid {
          display: grid;
          grid-template-columns: 1.2fr 0.8fr;
          gap: 20px;
        }

        .panel {
          background: var(--bg-card, rgba(255,255,255,0.04));
          border: 1px solid var(--border-soft, rgba(255,255,255,0.10));
          border-radius: 22px;
          padding: 22px;
          box-shadow: var(--shadow-lg, 0 18px 45px rgba(0,0,0,0.25));
        }

        .panel h3 {
          margin-top: 0;
          margin-bottom: 16px;
          color: var(--accent, #60a5fa);
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .form-grid {
          display: grid;
          gap: 12px;
        }

        .input,
        .select,
        .textarea {
          width: 100%;
          padding: 12px 14px;
          border-radius: 14px;
          border: 1px solid var(--border-color, rgba(255,255,255,0.12));
          background: var(--bg-card-2, rgba(255,255,255,0.03));
          color: var(--text-main, #e5e7eb);
          box-sizing: border-box;
          outline: none;
        }

        .input:focus,
        .select:focus,
        .textarea:focus {
          border-color: rgba(96, 165, 250, 0.4);
          box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.08);
        }

        .textarea {
          min-height: 120px;
          resize: vertical;
        }

        .btn-row {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .btn {
          border: none;
          border-radius: 14px;
          padding: 12px 16px;
          font-weight: 700;
          cursor: pointer;
          display: inline-flex;
          align-items: center;
          gap: 8px;
          transition: 0.25s ease;
        }

        .btn:hover {
          transform: translateY(-1px);
        }

        .btn:disabled {
          opacity: 0.65;
          cursor: not-allowed;
          transform: none;
        }

        .primary {
          background: var(--accent, #60a5fa);
          color: #111827;
        }

        .secondary {
          background: rgba(96, 165, 250, 0.12);
          color: var(--accent, #60a5fa);
          border: 1px solid rgba(96, 165, 250, 0.25);
        }

        .danger {
          background: rgba(239, 68, 68, 0.12);
          color: #fca5a5;
          border: 1px solid rgba(239, 68, 68, 0.25);
        }

        .message-box {
          margin-top: 14px;
          padding: 12px 14px;
          border-radius: 14px;
          font-weight: 600;
        }

        .success {
          background: rgba(74, 222, 128, 0.12);
          color: #86efac;
          border: 1px solid rgba(74, 222, 128, 0.25);
        }

        .error {
          background: rgba(248, 113, 113, 0.10);
          color: #fca5a5;
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .info {
          background: rgba(96, 165, 250, 0.10);
          color: #93c5fd;
          border: 1px solid rgba(96, 165, 250, 0.25);
        }

        .card-list {
          display: flex;
          flex-direction: column;
          gap: 14px;
          margin-top: 16px;
        }

        .card {
          border: 1px solid var(--border-color, rgba(255,255,255,0.12));
          border-radius: 16px;
          padding: 14px;
          background: var(--bg-card-2, rgba(255,255,255,0.03));
        }

        .question-card {
          margin-top: 14px;
          padding: 16px;
          border-radius: 16px;
          border: 1px solid var(--border-color, rgba(255,255,255,0.12));
          background: var(--bg-card-2, rgba(255,255,255,0.03));
        }

        .option-grid {
          display: grid;
          gap: 10px;
          margin-top: 10px;
        }

        .muted {
          color: var(--text-muted, #9ca3af);
          font-size: 14px;
          line-height: 1.6;
        }

        .section-title {
          margin-top: 18px;
          margin-bottom: 10px;
          font-weight: 700;
          color: var(--text-main, #e5e7eb);
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }

        @media (max-width: 950px) {
          .quiz-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>

      <div className="quiz-grid">
        <div className="panel">
          <h3>
            <Brain size={20} />
            Teacher AI Quiz Generation
          </h3>

          <div className="message-box info">
            This feature generates a quiz draft using AI, but the teacher reviews and edits the content before saving.
          </div>

          <div className="form-grid">
            <select
              className="select"
              value={selectedCourseId}
              onChange={(e) => setSelectedCourseId(e.target.value)}
            >
              <option value="">Select course</option>
              {courses.map((course) => (
                <option key={course.id} value={course.id}>
                  {course.courseCode} - {course.title}
                </option>
              ))}
            </select>

            <input
              className="input"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="Quiz topic"
            />

            <select
              className="select"
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
            >
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>

            <select
              className="select"
              value={questionCount}
              onChange={(e) => setQuestionCount(Number(e.target.value))}
            >
              {[1,2,3,4,5,6,7,8,9,10].map((n) => (
                <option key={n} value={n}>
                  {n} Question{n > 1 ? "s" : ""}
                </option>
              ))}
            </select>

            <textarea
              className="textarea"
              value={sourceText}
              onChange={(e) => setSourceText(e.target.value)}
              placeholder="Optional grounding text or lecture notes..."
            />

            <div className="btn-row">
              <button
                className="btn primary"
                onClick={handleGenerateDraft}
                disabled={!!loadingAction}
              >
                <ListChecks size={16} />
                {loadingAction === "generate" ? "Generating..." : "Generate Quiz Draft"}
              </button>
            </div>
          </div>

          {message && <div className="message-box success">{message}</div>}
          {error && <div className="message-box error">{error}</div>}

          {draft && (
            <>
              <div className="section-title">Review and Edit Draft</div>

              <input
                className="input"
                value={saveTitle}
                onChange={(e) => setSaveTitle(e.target.value)}
                placeholder="Quiz title"
              />

              <textarea
                className="textarea"
                value={saveDescription}
                onChange={(e) => setSaveDescription(e.target.value)}
                placeholder="Quiz description"
                style={{ marginTop: "12px" }}
              />

              {draft.questions.map((q, index) => (
                <div className="question-card" key={index}>
                  <div className="section-title">Question {index + 1}</div>

                  <textarea
                    className="textarea"
                    value={q.questionText}
                    onChange={(e) => handleQuestionChange(index, "questionText", e.target.value)}
                    placeholder="Question text"
                  />

                  <div className="option-grid">
                    {q.options.map((opt, optIndex) => (
                      <input
                        key={optIndex}
                        className="input"
                        value={opt}
                        onChange={(e) => handleOptionChange(index, optIndex, e.target.value)}
                        placeholder={`Option ${optIndex + 1}`}
                      />
                    ))}
                  </div>

                  <input
                    className="input"
                    value={q.correctAnswer}
                    onChange={(e) => handleQuestionChange(index, "correctAnswer", e.target.value)}
                    placeholder="Correct answer (must match one option exactly)"
                    style={{ marginTop: "12px" }}
                  />

                  <textarea
                    className="textarea"
                    value={q.explanation}
                    onChange={(e) => handleQuestionChange(index, "explanation", e.target.value)}
                    placeholder="Explanation"
                    style={{ marginTop: "12px" }}
                  />

                  <div className="btn-row" style={{ marginTop: "12px" }}>
                    <button
                      className="btn danger"
                      onClick={() => handleRemoveQuestion(index)}
                    >
                      Remove Question
                    </button>
                  </div>
                </div>
              ))}

              <div className="btn-row" style={{ marginTop: "16px" }}>
                <button
                  className="btn secondary"
                  onClick={handleSaveQuiz}
                  disabled={!!loadingAction}
                >
                  <Save size={16} />
                  {loadingAction === "save" ? "Saving..." : "Save Quiz"}
                </button>
              </div>
            </>
          )}
        </div>

        <div className="panel">
          <h3>
            <BookOpen size={20} />
            Saved Quizzes
          </h3>

          {!selectedCourseId ? (
            <div className="muted">Select a course to view saved quizzes.</div>
          ) : savedQuizzes.length === 0 ? (
            <div className="muted">No saved quizzes for this course yet.</div>
          ) : (
            <div className="card-list">
              {savedQuizzes.map((quiz) => (
                <div className="card" key={quiz.id}>
                  <strong>{quiz.title}</strong>
                  <div className="muted">
                    {quiz.courseCode} - {quiz.courseTitle}
                  </div>
                  <div className="muted">
                    Topic: {quiz.topic} | Difficulty: {quiz.difficulty}
                  </div>
                  <div className="muted">
                    Questions: {quiz.questionCount} | Status: {quiz.status}
                  </div>
                  <div className="muted">
                    AI-assisted: {quiz.generatedByAi ? "Yes" : "No"}
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="section-title">
            <FileText size={16} style={{ verticalAlign: "middle", marginRight: "8px" }} />
            Dissertation note
          </div>
          <div className="muted">
            This page demonstrates AI-assisted quiz authoring with teacher review before persistence, not full autonomous assessment delivery.
          </div>
        </div>
      </div>
    </div>
  );
}

export default TeacherQuizGenerator;