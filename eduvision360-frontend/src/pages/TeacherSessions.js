import React, { useEffect, useState } from "react";
import Menu from "../components/Menu";
import {
  CalendarDays,
  Clock3,
  PlusCircle,
  PlayCircle,
  CheckCircle2,
  Ban,
  BookOpen
} from "lucide-react";
import {
  getMyTeacherCourses,
  getTeacherSessions,
  createClassSession,
  updateClassSessionStatus
} from "../services/academicService";

function TeacherSessions() {
  const [courses, setCourses] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [message, setMessage] = useState("");

  const [courseId, setCourseId] = useState("");
  const [sessionDate, setSessionDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  const loadCourses = async () => {
    try {
      const res = await getMyTeacherCourses();
      const data = res.data || [];
      setCourses(data);

      if (data.length > 0 && !courseId) {
        setCourseId(data[0].id);
      }
    } catch (err) {
      console.error("Failed to load teacher courses", err);
      setMessage("Failed to load your courses");
    }
  };

  const loadSessions = async () => {
    try {
      const res = await getTeacherSessions();
      setSessions(res.data || []);
    } catch (err) {
      console.error("Failed to load teacher sessions", err);
      setMessage("Failed to load class sessions");
    }
  };

  useEffect(() => {
    loadCourses();
    loadSessions();
    // eslint-disable-next-line
  }, []);

  const resetForm = () => {
    setSessionDate("");
    setStartTime("");
    setEndTime("");
  };

  const handleCreateSession = async () => {
    setMessage("");

    if (!courseId || !sessionDate || !startTime || !endTime) {
      setMessage("Course, date, start time, and end time are required");
      return;
    }

    try {
      await createClassSession({
        courseId,
        sessionDate,
        startTime: `${startTime}:00`,
        endTime: `${endTime}:00`
      });

      setMessage("Class session created successfully");
      resetForm();
      loadSessions();
    } catch (err) {
      console.error("Create session failed", err);
      setMessage(err?.response?.data?.message || "Failed to create class session");
    }
  };

  const handleStatusChange = async (sessionId, status) => {
    setMessage("");

    try {
      await updateClassSessionStatus(sessionId, status);
      setMessage(`Session marked as ${status}`);
      loadSessions();
    } catch (err) {
      console.error("Update session status failed", err);
      setMessage(err?.response?.data?.message || "Failed to update session status");
    }
  };

  const formatDate = (dateValue) => {
    if (!dateValue) return "-";
    try {
      return new Date(`${dateValue}T00:00:00`).toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric"
      });
    } catch {
      return dateValue;
    }
  };

  const formatTime = (timeValue) => {
    if (!timeValue) return "-";
    return timeValue.slice(0, 5);
  };

  return (
    <div className="teacher-layout">
      <style>{`
        .teacher-layout {
          display: flex;
          min-height: 100vh;
          background: var(--page-bg);
          font-family: 'Inter', sans-serif;
        }

        .main-content {
          flex: 1;
          padding: 40px;
          color: var(--text-primary);
        }

        .page-header {
          margin-bottom: 24px;
        }

        .page-header h2 {
          margin: 0 0 10px 0;
          font-size: 32px;
          background: linear-gradient(to right, var(--text-primary), var(--accent), var(--text-primary));
          background-size: 200% auto;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .page-header p {
          color: var(--text-secondary);
          margin: 0;
        }

        .layout-grid {
          display: grid;
          grid-template-columns: 360px minmax(0, 1fr);
          gap: 20px;
        }

        .panel {
          background: var(--card-bg);
          border: 1px solid var(--border-color);
          border-radius: 22px;
          padding: 22px;
          box-shadow: var(--card-shadow);
        }

        .panel h3 {
          margin-top: 0;
          margin-bottom: 14px;
          color: var(--accent);
        }

        .message {
          color: var(--accent);
          margin-bottom: 14px;
          font-weight: 600;
        }

        .form-grid {
          display: grid;
          gap: 12px;
        }

        .input,
        .select {
          width: 100%;
          padding: 12px 14px;
          border-radius: 14px;
          border: 1px solid var(--border-color);
          background: var(--input-bg, var(--card-bg));
          color: var(--text-primary);
          outline: none;
          font-size: 14px;
          box-sizing: border-box;
        }

        .primary-btn,
        .action-btn,
        .danger-btn,
        .success-btn,
        .complete-btn {
          border: none;
          border-radius: 14px;
          padding: 11px 14px;
          font-weight: 700;
          cursor: pointer;
          display: inline-flex;
          align-items: center;
          gap: 8px;
          transition: 0.25s ease;
        }

        .primary-btn {
          background: var(--accent-soft);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.35);
        }

        .success-btn {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.28);
        }

        .complete-btn {
          background: rgba(96, 165, 250, 0.12);
          color: #60a5fa;
          border: 1px solid rgba(96, 165, 250, 0.28);
        }

        .danger-btn {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.26);
        }

        .primary-btn:hover,
        .success-btn:hover,
        .complete-btn:hover,
        .danger-btn:hover {
          transform: translateY(-1px);
        }

        .session-list {
          display: flex;
          flex-direction: column;
          gap: 14px;
          max-height: 650px;
          overflow-y: auto;
        }

        .session-card {
          border: 1px solid var(--border-color);
          border-radius: 18px;
          padding: 18px;
          background: rgba(255,255,255,0.02);
        }

        .session-top {
          display: flex;
          justify-content: space-between;
          gap: 12px;
          flex-wrap: wrap;
          margin-bottom: 10px;
        }

        .session-title {
          font-size: 17px;
          font-weight: 700;
          color: var(--text-primary);
        }

        .session-meta {
          color: var(--text-secondary);
          line-height: 1.6;
          font-size: 14px;
        }

        .status-badge {
          display: inline-flex;
          align-items: center;
          padding: 6px 12px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 700;
          border: 1px solid var(--border-color);
        }

        .status-SCHEDULED {
          background: rgba(255, 213, 72, 0.10);
          color: var(--accent);
          border-color: rgba(255, 213, 72, 0.28);
        }

        .status-OPEN {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border-color: rgba(74, 222, 128, 0.28);
        }

        .status-COMPLETED {
          background: rgba(96, 165, 250, 0.12);
          color: #60a5fa;
          border-color: rgba(96, 165, 250, 0.28);
        }

        .status-CANCELLED {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border-color: rgba(248, 113, 113, 0.26);
        }

        .actions-row {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
          margin-top: 14px;
        }

        .empty-state {
          color: var(--text-secondary);
          padding: 30px;
          text-align: center;
          border: 1px dashed var(--border-color);
          border-radius: 18px;
        }

        @media (max-width: 1000px) {
          .layout-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <div className="page-header">
          <h2>Manage Class Sessions</h2>
          <p>Create sessions for your courses and control when attendance is open.</p>
        </div>

        <div className="layout-grid">
          <div className="panel">
            <h3>Create Session</h3>

            {message && <div className="message">{message}</div>}

            <div className="form-grid">
              <select
                className="select"
                value={courseId}
                onChange={(e) => setCourseId(e.target.value)}
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
                type="date"
                value={sessionDate}
                onChange={(e) => setSessionDate(e.target.value)}
              />

              <input
                className="input"
                type="time"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
              />

              <input
                className="input"
                type="time"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
              />

              <button className="primary-btn" onClick={handleCreateSession}>
                <PlusCircle size={17} />
                Create Session
              </button>
            </div>
          </div>

          <div className="panel">
            <h3>My Sessions</h3>

            {sessions.length === 0 ? (
              <div className="empty-state">
                <CalendarDays size={36} style={{ marginBottom: "10px", opacity: 0.6 }} />
                <div>No class sessions created yet.</div>
              </div>
            ) : (
              <div className="session-list">
                {sessions.map((session) => (
                  <div className="session-card" key={session.id}>
                    <div className="session-top">
                      <div>
                        <div className="session-title">
                          {session.courseCode} - {session.courseTitle}
                        </div>
                        <div className="session-meta">
                          <CalendarDays size={14} style={{ verticalAlign: "middle", marginRight: 6 }} />
                          {formatDate(session.sessionDate)}
                          <br />
                          <Clock3 size={14} style={{ verticalAlign: "middle", marginRight: 6 }} />
                          {formatTime(session.startTime)} - {formatTime(session.endTime)}
                        </div>
                      </div>

                      <div className={`status-badge status-${session.status}`}>
                        {session.status}
                      </div>
                    </div>

                    <div className="actions-row">
                      {session.status !== "OPEN" && session.status !== "COMPLETED" && session.status !== "CANCELLED" && (
                        <button
                          className="success-btn"
                          onClick={() => handleStatusChange(session.id, "OPEN")}
                        >
                          <PlayCircle size={16} />
                          Open
                        </button>
                      )}

                      {session.status === "OPEN" && (
                        <button
                          className="complete-btn"
                          onClick={() => handleStatusChange(session.id, "COMPLETED")}
                        >
                          <CheckCircle2 size={16} />
                          Complete
                        </button>
                      )}

                      {session.status !== "COMPLETED" && session.status !== "CANCELLED" && (
                        <button
                          className="danger-btn"
                          onClick={() => handleStatusChange(session.id, "CANCELLED")}
                        >
                          <Ban size={16} />
                          Cancel
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default TeacherSessions;