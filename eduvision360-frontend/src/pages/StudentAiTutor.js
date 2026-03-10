import React, { useEffect, useRef, useState } from "react";
import {
  askAiChatbot,
  askAiTutor,
  generateAiSummary,
  generateAiQuiz,
  getMyAiHistory,
  getMyEnrollments,
  getStudentMaterialsByCourse
} from "../services/academicService";
import {
  Brain,
  MessageSquare,
  FileText,
  ListChecks,
  BookOpen,
  Volume2,
  Square,
  Mic,
  MicOff
} from "lucide-react";

function StudentAiTutor() {
  const [courses, setCourses] = useState([]);
  const [history, setHistory] = useState([]);
  const [materials, setMaterials] = useState([]);

  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [selectedMaterialId, setSelectedMaterialId] = useState("");

  const [question, setQuestion] = useState("");
  const [contextText, setContextText] = useState("");
  const [topic, setTopic] = useState("");
  const [sourceText, setSourceText] = useState("");
  const [questionCount, setQuestionCount] = useState(2);

  const [answer, setAnswer] = useState("");
  const [quiz, setQuiz] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loadingAction, setLoadingAction] = useState("");
  const [materialsLoading, setMaterialsLoading] = useState(false);

  const [ttsSupported, setTtsSupported] = useState(false);
  const [speakingKey, setSpeakingKey] = useState("");
  const [selectedVoice, setSelectedVoice] = useState(null);

  const [sttSupported, setSttSupported] = useState(false);
  const [listening, setListening] = useState(false);

  const utteranceRef = useRef(null);
  const recognitionRef = useRef(null);
  const speechBaseQuestionRef = useRef("");

  const loadData = async () => {
    try {
      const [coursesRes, historyRes] = await Promise.all([
        getMyEnrollments(),
        getMyAiHistory()
      ]);

      const courseList = coursesRes.data || [];
      setCourses(courseList);
      setHistory(historyRes.data || []);

      if (courseList.length > 0 && !selectedCourseId) {
        setSelectedCourseId(courseList[0].courseId);
      }
    } catch (err) {
      console.error("Failed to load AI tutor data", err);
      setError("Failed to load AI tutor data");
    }
  };

  const loadMaterialsByCourse = async (courseId) => {
    if (!courseId) {
      setMaterials([]);
      setSelectedMaterialId("");
      return;
    }

    try {
      setMaterialsLoading(true);

      const res = await getStudentMaterialsByCourse(courseId);
      const materialList = res.data || [];
      setMaterials(materialList);

      if (materialList.length > 0) {
        setSelectedMaterialId(materialList[0].id);
        applyMaterialToAiFields(materialList[0]);
      } else {
        setSelectedMaterialId("");
        setTopic("");
        setContextText("");
        setSourceText("");
      }
    } catch (err) {
      console.error("Failed to load course materials", err);
      setMaterials([]);
      setSelectedMaterialId("");
      setError("Failed to load course materials");
    } finally {
      setMaterialsLoading(false);
    }
  };

  const buildMaterialText = (material) => {
    if (!material) return "";

    const title = material.title ? `Title: ${material.title}` : "";
    const description = material.description ? `Description: ${material.description}` : "";

    if (material.type === "NOTE") {
      const content = material.content ? `Content:\n${material.content}` : "";
      return [title, description, content].filter(Boolean).join("\n\n");
    }

    return [
      title,
      description,
      `Material type: ${material.type}`,
      `Resource link: ${material.content || "N/A"}`,
      "Note: Full document text is not available yet for this material type."
    ]
      .filter(Boolean)
      .join("\n\n");
  };

  const applyMaterialToAiFields = (material) => {
    if (!material) return;

    const builtText = buildMaterialText(material);

    setTopic(material.title || "");
    setContextText(builtText);
    setSourceText(builtText);

    if (material.type !== "NOTE") {
      setMessage(
        "Selected material is a link/PDF link. AI is using title/description/link info only, not full document text."
      );
    }
  };

  const stopSpeaking = () => {
    if (typeof window !== "undefined" && "speechSynthesis" in window) {
      const synth = window.speechSynthesis;
      if (synth.speaking || synth.pending) {
        synth.cancel();
      }
    }
    utteranceRef.current = null;
    setSpeakingKey("");
  };

  const splitTextIntoChunks = (text, maxLength = 180) => {
    const cleaned = text?.replace(/\s+/g, " ").trim() || "";
    if (!cleaned) return [];

    const sentences = cleaned.match(/[^.!?]+[.!?]*/g) || [cleaned];
    const chunks = [];
    let current = "";

    for (const sentence of sentences) {
      const next = `${current} ${sentence}`.trim();

      if (next.length <= maxLength) {
        current = next;
      } else {
        if (current) {
          chunks.push(current);
        }

        if (sentence.length <= maxLength) {
          current = sentence.trim();
        } else {
          for (let i = 0; i < sentence.length; i += maxLength) {
            chunks.push(sentence.slice(i, i + maxLength).trim());
          }
          current = "";
        }
      }
    }

    if (current) {
      chunks.push(current);
    }

    return chunks.filter(Boolean);
  };

  const speakText = (text, key) => {
    if (!ttsSupported || !text?.trim()) return;

    if (speakingKey === key) {
      stopSpeaking();
      return;
    }

    const synth = window.speechSynthesis;
    stopSpeaking();
    setError("");

    const chunks = splitTextIntoChunks(text);
    if (chunks.length === 0) return;

    let chunkIndex = 0;

    const speakNextChunk = () => {
      if (chunkIndex >= chunks.length) {
        utteranceRef.current = null;
        setSpeakingKey("");
        return;
      }

      const utterance = new SpeechSynthesisUtterance(chunks[chunkIndex]);

      if (selectedVoice) {
        utterance.voice = selectedVoice;
        utterance.lang = selectedVoice.lang || "en-US";
      } else {
        utterance.lang = "en-US";
      }

      utterance.rate = 0.95;
      utterance.pitch = 1;
      utterance.volume = 1;

      utterance.onend = () => {
        chunkIndex += 1;
        speakNextChunk();
      };

      utterance.onerror = (event) => {
        const errorType = event?.error || "";

        if (
          errorType === "interrupted" ||
          errorType === "canceled" ||
          errorType === "cancelled"
        ) {
          utteranceRef.current = null;
          setSpeakingKey("");
          return;
        }

        utteranceRef.current = null;
        setSpeakingKey("");
        setError(`Text-to-speech failed${errorType ? `: ${errorType}` : "."}`);
      };

      utteranceRef.current = utterance;
      synth.speak(utterance);
    };

    setSpeakingKey(key);

    setTimeout(() => {
      speakNextChunk();
    }, 120);
  };

  const stopListening = () => {
    try {
      if (recognitionRef.current) {
        recognitionRef.current.stop();
      }
    } catch (err) {
      console.error("Failed to stop speech recognition", err);
    } finally {
      setListening(false);
    }
  };

  const startListening = () => {
    if (!sttSupported || !recognitionRef.current) {
      setError("Speech-to-text is not supported on this browser.");
      return;
    }

    try {
      setError("");
      speechBaseQuestionRef.current = question.trim()
        ? `${question.trim()} `
        : "";
      recognitionRef.current.start();
    } catch (err) {
      console.error("Failed to start speech recognition", err);
      setError("Unable to start voice input. Please try again.");
    }
  };

  const toggleListening = () => {
    if (listening) {
      stopListening();
    } else {
      startListening();
    }
  };

  useEffect(() => {
    if (typeof window !== "undefined" && "speechSynthesis" in window) {
      const synth = window.speechSynthesis;

      const loadVoices = () => {
        const voices = synth.getVoices();
        if (voices.length > 0) {
          setTtsSupported(true);

          const preferredVoice =
            voices.find((voice) => voice.lang?.toLowerCase().startsWith("en")) ||
            voices[0];

          setSelectedVoice(preferredVoice || null);
        } else {
          setTtsSupported(false);
        }
      };

      loadVoices();
      synth.onvoiceschanged = loadVoices;
    }

    if (typeof window !== "undefined") {
      const SpeechRecognition =
        window.SpeechRecognition || window.webkitSpeechRecognition;

      if (SpeechRecognition) {
        const recognition = new SpeechRecognition();
        recognition.lang = "en-US";
        recognition.continuous = false;
        recognition.interimResults = true;
        recognition.maxAlternatives = 1;

        recognition.onstart = () => {
          setListening(true);
        };

        recognition.onresult = (event) => {
          const transcript = Array.from(event.results)
            .map((result) => result[0]?.transcript || "")
            .join(" ")
            .trim();

          setQuestion(`${speechBaseQuestionRef.current}${transcript}`.trim());
        };

        recognition.onerror = (event) => {
          const errorType = event?.error || "";

          if (
            errorType === "aborted" ||
            errorType === "no-speech"
          ) {
            setListening(false);
            return;
          }

          setListening(false);
          setError(`Speech-to-text failed${errorType ? `: ${errorType}` : "."}`);
        };

        recognition.onend = () => {
          setListening(false);
        };

        recognitionRef.current = recognition;
        setSttSupported(true);
      } else {
        setSttSupported(false);
      }
    }

    loadData();

    return () => {
      if (typeof window !== "undefined" && "speechSynthesis" in window) {
        window.speechSynthesis.cancel();
        window.speechSynthesis.onvoiceschanged = null;
      }

      try {
        if (recognitionRef.current) {
          recognitionRef.current.stop();
        }
      } catch (err) {
        console.error("Speech recognition cleanup failed", err);
      }

      utteranceRef.current = null;
      recognitionRef.current = null;
      setSpeakingKey("");
      setListening(false);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (selectedCourseId) {
      loadMaterialsByCourse(selectedCourseId);
    } else {
      setMaterials([]);
      setSelectedMaterialId("");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedCourseId]);

  const handleMaterialChange = (materialId) => {
    setSelectedMaterialId(materialId);

    const material = materials.find((m) => m.id === materialId);
    if (material) {
      applyMaterialToAiFields(material);
    }
  };

  const resetOutputs = () => {
    stopSpeaking();
    stopListening();
    setAnswer("");
    setQuiz([]);
    setMessage("");
    setError("");
  };

  const handleAskTutor = async () => {
    resetOutputs();

    if (!selectedCourseId || !question.trim()) {
      setError("Course and question are required");
      return;
    }

    try {
      setLoadingAction("tutor");

      const res = await askAiTutor({
        courseId: selectedCourseId,
        question: question.trim(),
        contextText: contextText.trim()
      });

      setAnswer(res.data.responseText || "");
      setMessage("AI tutor response generated successfully");
      await loadData();
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "Failed to get AI tutor response");
    } finally {
      setLoadingAction("");
    }
  };

  const handleAskChatbot = async () => {
    resetOutputs();

    if (!selectedCourseId || !question.trim()) {
      setError("Course and question are required");
      return;
    }

    try {
      setLoadingAction("chatbot");

      const res = await askAiChatbot({
        courseId: selectedCourseId,
        question: question.trim(),
        contextText: contextText.trim()
      });

      setAnswer(res.data.responseText || "");
      setMessage("Chatbot response generated successfully");
      await loadData();
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "Failed to get chatbot response");
    } finally {
      setLoadingAction("");
    }
  };

  const handleGenerateSummary = async () => {
    resetOutputs();

    if (!selectedCourseId || !topic.trim() || !sourceText.trim()) {
      setError("Course, topic, and source text are required");
      return;
    }

    try {
      setLoadingAction("summary");

      const res = await generateAiSummary({
        courseId: selectedCourseId,
        topic: topic.trim(),
        sourceText: sourceText.trim()
      });

      setAnswer(res.data.responseText || "");
      setMessage("Summary generated successfully");
      await loadData();
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "Failed to generate summary");
    } finally {
      setLoadingAction("");
    }
  };

  const handleGenerateQuiz = async () => {
    resetOutputs();

    if (!selectedCourseId || !topic.trim() || !sourceText.trim()) {
      setError("Course, topic, and source text are required");
      return;
    }

    try {
      setLoadingAction("quiz");

      const res = await generateAiQuiz({
        courseId: selectedCourseId,
        topic: topic.trim(),
        sourceText: sourceText.trim(),
        questionCount: Number(questionCount)
      });

      setQuiz(res.data.questions || []);
      setMessage("Quiz generated successfully");
      await loadData();
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "Failed to generate quiz");
    } finally {
      setLoadingAction("");
    }
  };

  return (
    <div className="ai-page">
      <style>{`
        .ai-page {
          color: var(--text-main);
          animation: fadeIn 0.35s ease;
        }

        .ai-grid {
          display: grid;
          grid-template-columns: 1.1fr 0.9fr;
          gap: 20px;
        }

        .panel {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 22px;
          padding: 22px;
          box-shadow: var(--shadow-lg);
        }

        .panel h3 {
          margin-top: 0;
          margin-bottom: 16px;
          color: var(--accent);
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
          border: 1px solid var(--border-color);
          background: var(--bg-card-2);
          color: var(--text-main);
          box-sizing: border-box;
          outline: none;
        }

        .input:focus,
        .select:focus,
        .textarea:focus {
          border-color: rgba(255, 213, 72, 0.35);
          box-shadow: 0 0 0 3px rgba(255, 213, 72, 0.08);
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
          background: var(--accent);
          color: #111827;
        }

        .secondary {
          background: var(--accent-soft);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.25);
        }

        .speech-btn {
          border: 1px solid rgba(255, 213, 72, 0.28);
          border-radius: 12px;
          padding: 10px 14px;
          font-weight: 700;
          cursor: pointer;
          display: inline-flex;
          align-items: center;
          gap: 8px;
          transition: 0.25s ease;
          background: rgba(255, 213, 72, 0.12);
          color: var(--accent);
        }

        .speech-btn:hover {
          transform: translateY(-1px);
        }

        .speech-btn.stop {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .tts-btn {
          margin-top: 12px;
        }

        .message-box {
          margin-top: 14px;
          padding: 12px 14px;
          border-radius: 14px;
          font-weight: 600;
        }

        .success {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.25);
        }

        .error {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .info {
          background: rgba(96, 165, 250, 0.10);
          color: #93c5fd;
          border: 1px solid rgba(96, 165, 250, 0.25);
        }

        .answer-box,
        .quiz-box,
        .history-list {
          margin-top: 16px;
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .card {
          border: 1px solid var(--border-color);
          border-radius: 16px;
          padding: 14px;
          background: var(--bg-card-2);
        }

        .muted {
          color: var(--text-muted);
          font-size: 14px;
          line-height: 1.6;
        }

        .question-title {
          margin-bottom: 8px;
          color: var(--text-main);
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }

        @media (max-width: 900px) {
          .ai-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>

      <div className="ai-grid">
        <div className="panel">
          <h3>
            <Brain size={20} />
            AI Tutor & Generative AI
          </h3>

          <div className="form-grid">
            <select
              className="select"
              value={selectedCourseId}
              onChange={(e) => setSelectedCourseId(e.target.value)}
            >
              <option value="">Select course</option>
              {courses.map((course) => (
                <option key={course.courseId} value={course.courseId}>
                  {course.courseCode} - {course.courseTitle}
                </option>
              ))}
            </select>

            <select
              className="select"
              value={selectedMaterialId}
              onChange={(e) => handleMaterialChange(e.target.value)}
              disabled={!selectedCourseId || materialsLoading || materials.length === 0}
            >
              <option value="">
                {materialsLoading
                  ? "Loading materials..."
                  : materials.length === 0
                  ? "No materials available for this course"
                  : "Select material"}
              </option>

              {materials.map((material) => (
                <option key={material.id} value={material.id}>
                  {material.title} ({material.type})
                </option>
              ))}
            </select>

            <div className="message-box info">
              <BookOpen size={16} style={{ marginRight: "8px", verticalAlign: "middle" }} />
              For best AI results, choose a <strong>NOTE</strong> material. LINK and PDF_LINK materials only provide title/description/link info at this stage.
            </div>

            {!ttsSupported && (
              <div className="message-box info">
                Text-to-speech is not supported on this browser.
              </div>
            )}

            {!sttSupported && (
              <div className="message-box info">
                Speech-to-text is not supported on this browser.
              </div>
            )}

            <textarea
              className="textarea"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder="Ask a course-related question..."
            />

            {sttSupported && (
              <button
                className={`speech-btn ${listening ? "stop" : ""}`}
                onClick={toggleListening}
                type="button"
              >
                {listening ? (
                  <>
                    <MicOff size={16} />
                    Stop Voice Input
                  </>
                ) : (
                  <>
                    <Mic size={16} />
                    Start Voice Input
                  </>
                )}
              </button>
            )}

            <textarea
              className="textarea"
              value={contextText}
              onChange={(e) => setContextText(e.target.value)}
              placeholder="AI Tutor / Chatbot context (auto-filled from selected material, but you can edit it)..."
            />

            <div className="btn-row">
              <button
                className="btn primary"
                onClick={handleAskTutor}
                disabled={!!loadingAction}
              >
                <Brain size={16} />
                {loadingAction === "tutor" ? "Generating..." : "Ask AI Tutor"}
              </button>

              <button
                className="btn secondary"
                onClick={handleAskChatbot}
                disabled={!!loadingAction}
              >
                <MessageSquare size={16} />
                {loadingAction === "chatbot" ? "Generating..." : "Ask Chatbot"}
              </button>
            </div>

            <input
              className="input"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="Topic for summary / quiz"
            />

            <textarea
              className="textarea"
              value={sourceText}
              onChange={(e) => setSourceText(e.target.value)}
              placeholder="Summary / quiz source text (auto-filled from selected material, but you can edit it)..."
            />

            <input
              className="input"
              type="number"
              min="1"
              max="3"
              value={questionCount}
              onChange={(e) => setQuestionCount(e.target.value)}
              placeholder="Question count"
            />

            <div className="btn-row">
              <button
                className="btn primary"
                onClick={handleGenerateSummary}
                disabled={!!loadingAction}
              >
                <FileText size={16} />
                {loadingAction === "summary" ? "Generating..." : "Generate Summary"}
              </button>

              <button
                className="btn secondary"
                onClick={handleGenerateQuiz}
                disabled={!!loadingAction}
              >
                <ListChecks size={16} />
                {loadingAction === "quiz" ? "Generating..." : "Generate Quiz"}
              </button>
            </div>
          </div>

          {message && <div className="message-box success">{message}</div>}
          {error && <div className="message-box error">{error}</div>}

          {answer && (
            <div className="answer-box">
              <div className="card">
                <strong>AI Response</strong>
                <div
                  className="muted"
                  style={{ marginTop: "10px", whiteSpace: "pre-wrap" }}
                >
                  {answer}
                </div>

                {ttsSupported && (
                  <button
                    className={`speech-btn tts-btn ${speakingKey === "current-answer" ? "stop" : ""}`}
                    onClick={() => speakText(answer, "current-answer")}
                    type="button"
                  >
                    {speakingKey === "current-answer" ? (
                      <>
                        <Square size={16} />
                        Stop Reading
                      </>
                    ) : (
                      <>
                        <Volume2 size={16} />
                        Read Aloud
                      </>
                    )}
                  </button>
                )}
              </div>
            </div>
          )}

          {quiz.length > 0 && (
            <div className="quiz-box">
              {quiz.map((q, index) => (
                <div className="card" key={index}>
                  <div className="question-title">
                    <strong>Q{index + 1}. {q.question}</strong>
                  </div>

                  <div className="muted">
                    {q.options?.map((opt, i) => (
                      <div key={i}>• {opt}</div>
                    ))}
                  </div>

                  <div className="muted" style={{ marginTop: "8px" }}>
                    <strong>Correct:</strong> {q.correctAnswer}
                  </div>

                  <div className="muted">
                    <strong>Explanation:</strong> {q.explanation}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <h3>
            <MessageSquare size={20} />
            My AI Interaction History
          </h3>

          {history.length === 0 ? (
            <div className="muted">No AI interactions yet.</div>
          ) : (
            <div className="history-list">
              {history.map((item) => (
                <div className="card" key={item.interactionId}>
                  <strong>{item.interactionType}</strong>
                  <div className="muted">
                    {item.courseCode} - {item.courseTitle}
                  </div>
                  <div className="muted" style={{ marginTop: "8px" }}>
                    <strong>Prompt:</strong> {item.promptText}
                  </div>
                  <div className="muted">
                    <strong>Response:</strong> {item.responseText}
                  </div>

                  {ttsSupported && item.responseText && (
                    <button
                      className={`speech-btn tts-btn ${speakingKey === `history-${item.interactionId}` ? "stop" : ""}`}
                      onClick={() => speakText(item.responseText, `history-${item.interactionId}`)}
                      type="button"
                    >
                      {speakingKey === `history-${item.interactionId}` ? (
                        <>
                          <Square size={16} />
                          Stop Reading
                        </>
                      ) : (
                        <>
                          <Volume2 size={16} />
                          Read Aloud
                        </>
                      )}
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default StudentAiTutor;