import React, { useEffect, useState } from "react";
import {
  getMyEnrollments,
  submitFeedback,
  getMyFeedback
} from "../services/academicService";
import { MessageSquare, Send, Star } from "lucide-react";

function StudentFeedback() {
  const [courses, setCourses] = useState([]);
  const [feedbackList, setFeedbackList] = useState([]);
  const [courseId, setCourseId] = useState("");
  const [category, setCategory] = useState("GENERAL");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadData = async () => {
    try {
      const [coursesRes, feedbackRes] = await Promise.all([
        getMyEnrollments(),
        getMyFeedback()
      ]);

      const enrolledCourses = coursesRes.data || [];
      setCourses(enrolledCourses);
      setFeedbackList(feedbackRes.data || []);

      if (enrolledCourses.length > 0 && !courseId) {
        setCourseId(enrolledCourses[0].courseId);
      }
    } catch (err) {
      setError("Failed to load feedback data");
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line
  }, []);

  const handleSubmit = async () => {
    setMessage("");
    setError("");

    if (!courseId || !comment.trim()) {
      setError("Course and comment are required");
      return;
    }

    try {
      await submitFeedback({
        courseId,
        category,
        rating: Number(rating),
        comment
      });

      setMessage("Feedback submitted successfully");
      setComment("");
      setCategory("GENERAL");
      setRating(5);
      loadData();
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to submit feedback");
    }
  };

  return (
    <div className="feedback-page">
      <style>{`
        .feedback-page {
          color: var(--text-main);
        }

        .feedback-grid {
          display: grid;
          grid-template-columns: 360px minmax(0, 1fr);
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
          color: var(--accent);
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
        }

        .textarea {
          min-height: 130px;
          resize: vertical;
        }

        .submit-btn {
          border: none;
          border-radius: 14px;
          padding: 12px 16px;
          font-weight: 700;
          cursor: pointer;
          background: var(--accent);
          color: #111827;
          display: inline-flex;
          align-items: center;
          gap: 8px;
        }

        .message-box {
          margin-bottom: 12px;
          padding: 12px 14px;
          border-radius: 14px;
          font-weight: 600;
        }

        .success-box {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.28);
        }

        .error-box {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .feedback-list {
          display: flex;
          flex-direction: column;
          gap: 14px;
        }

        .feedback-card {
          border: 1px solid var(--border-color);
          border-radius: 16px;
          padding: 16px;
          background: var(--bg-card-2);
        }

        .feedback-top {
          display: flex;
          justify-content: space-between;
          gap: 12px;
          flex-wrap: wrap;
          margin-bottom: 8px;
        }

        .badge {
          display: inline-block;
          padding: 6px 10px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 700;
        }

        .status-NEW {
          background: rgba(255, 213, 72, 0.10);
          color: var(--accent);
        }

        .status-REVIEWED {
          background: rgba(96, 165, 250, 0.12);
          color: #60a5fa;
        }

        .status-RESOLVED {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
        }

        .muted {
          color: var(--text-muted);
          font-size: 14px;
          line-height: 1.6;
        }

        @media (max-width: 900px) {
          .feedback-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>

      <div className="feedback-grid">
        <div className="panel">
          <h3>Submit Feedback</h3>

          {message && <div className="message-box success-box">{message}</div>}
          {error && <div className="message-box error-box">{error}</div>}

          <div className="form-grid">
            <select className="select" value={courseId} onChange={(e) => setCourseId(e.target.value)}>
              <option value="">Select course</option>
              {courses.map((course) => (
                <option key={course.courseId} value={course.courseId}>
                  {course.courseCode} - {course.courseTitle}
                </option>
              ))}
            </select>

            <select className="select" value={category} onChange={(e) => setCategory(e.target.value)}>
              <option value="GENERAL">GENERAL</option>
              <option value="COURSE_CONTENT">COURSE_CONTENT</option>
              <option value="CLASS_SESSION">CLASS_SESSION</option>
              <option value="LEARNING_MATERIAL">LEARNING_MATERIAL</option>
              <option value="ANNOUNCEMENT">ANNOUNCEMENT</option>
              <option value="AI_TUTOR">AI_TUTOR</option>
              <option value="CHATBOT">CHATBOT</option>
              <option value="TECHNICAL_ISSUE">TECHNICAL_ISSUE</option>
            </select>

            <select className="select" value={rating} onChange={(e) => setRating(e.target.value)}>
              <option value={5}>5 - Excellent</option>
              <option value={4}>4 - Good</option>
              <option value={3}>3 - Average</option>
              <option value={2}>2 - Poor</option>
              <option value={1}>1 - Very Poor</option>
            </select>

            <textarea
              className="textarea"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Write your feedback here..."
            />

            <button className="submit-btn" onClick={handleSubmit}>
              <Send size={16} />
              Submit Feedback
            </button>
          </div>
        </div>

        <div className="panel">
          <h3>My Feedback</h3>

          {feedbackList.length === 0 ? (
            <div className="muted">No feedback submitted yet.</div>
          ) : (
            <div className="feedback-list">
              {feedbackList.map((item) => (
                <div className="feedback-card" key={item.id}>
                  <div className="feedback-top">
                    <div>
                      <strong>{item.courseCode} - {item.courseTitle}</strong>
                      <div className="muted">
                        {item.category} • <Star size={14} style={{ verticalAlign: "middle" }} /> {item.rating}/5
                      </div>
                    </div>
                    <span className={`badge status-${item.status}`}>{item.status}</span>
                  </div>

                  <div className="muted">{item.comment}</div>

                  {item.responseComment && (
                    <div className="muted" style={{ marginTop: "10px" }}>
                      <strong>Response:</strong> {item.responseComment}
                    </div>
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

export default StudentFeedback;