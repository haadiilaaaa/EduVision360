import React, { useEffect, useState } from "react";
import {
  Brain,
  Eye,
  AlertTriangle,
  Activity,
  BookOpen,
  Users
} from "lucide-react";
import { getTeacherCourses } from "../services/academicService";
import { getTeacherEngagementSummary } from "../services/academicService";

function TeacherEngagementSummary() {
  const [courses, setCourses] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [summary, setSummary] = useState(null);
  const [loadingCourses, setLoadingCourses] = useState(true);
  const [loadingSummary, setLoadingSummary] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadCourses = async () => {
      try {
        setLoadingCourses(true);
        setError("");

        const res = await getTeacherCourses();
        const courseList = res?.data || res || [];

        setCourses(courseList);

        if (courseList.length > 0) {
          setSelectedCourseId(courseList[0].id);
        }
      } catch (err) {
        console.error("Failed to load teacher courses", err);
        setError("Failed to load teacher courses");
      } finally {
        setLoadingCourses(false);
      }
    };

    loadCourses();
  }, []);

  useEffect(() => {
    const loadSummary = async () => {
      if (!selectedCourseId) return;

      try {
        setLoadingSummary(true);
        setError("");

        const data = await getTeacherEngagementSummary(selectedCourseId);
        setSummary(data);
      } catch (err) {
        console.error("Failed to load engagement summary", err);
        setError("Failed to load engagement summary");
      } finally {
        setLoadingSummary(false);
      }
    };

    loadSummary();
  }, [selectedCourseId]);

  const formatDateTime = (value) => {
    if (!value) return "-";
    try {
      return new Date(value).toLocaleString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      });
    } catch {
      return value;
    }
  };

  const getLabelClass = (label) => {
    if (label === "ATTENTIVE") return "attentive";
    if (label === "NEUTRAL") return "neutral";
    return "distracted";
  };

  const selectedCourse = courses.find((c) => c.id === selectedCourseId);

  return (
    <div className="teacher-engagement-panel">
      <style>{`
        .teacher-engagement-panel {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-top: 1px solid var(--border-strong);
          border-left: 1px solid var(--border-mid);
          border-radius: 24px;
          padding: 24px;
          margin-bottom: 30px;
          box-shadow: var(--shadow-lg);
        }

        .engagement-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 16px;
          flex-wrap: wrap;
          margin-bottom: 20px;
        }

        .engagement-title {
          margin: 0;
          display: flex;
          align-items: center;
          gap: 10px;
          color: var(--accent);
          font-size: 22px;
        }

        .engagement-subtext {
          margin-top: 8px;
          color: var(--text-muted);
          font-size: 14px;
        }

        .course-select {
          min-width: 260px;
          padding: 12px 14px;
          border-radius: 14px;
          border: 1px solid var(--border-color);
          background: var(--bg-card-2);
          color: var(--text-main);
          font-size: 14px;
          outline: none;
        }

        .engagement-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 16px;
          margin-bottom: 22px;
        }

        .engagement-stat {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          border-radius: 18px;
          padding: 18px;
          display: flex;
          align-items: center;
          gap: 14px;
        }

        .engagement-stat-icon {
          width: 46px;
          height: 46px;
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(255, 213, 72, 0.10);
          color: var(--accent);
        }

        .engagement-stat p {
          margin: 0;
          font-size: 12px;
          color: var(--text-muted);
          text-transform: uppercase;
          letter-spacing: 1px;
        }

        .engagement-stat h3 {
          margin: 6px 0 0 0;
          font-size: 24px;
          color: var(--text-main);
        }

        .recent-box {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          border-radius: 18px;
          padding: 18px;
        }

        .recent-title {
          margin: 0 0 14px 0;
          color: var(--text-main);
          font-size: 18px;
        }

        .recent-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .recent-item {
          border: 1px solid var(--border-color);
          border-radius: 14px;
          padding: 14px;
          background: rgba(255,255,255,0.02);
          display: flex;
          justify-content: space-between;
          gap: 14px;
          flex-wrap: wrap;
        }

        .recent-main {
          flex: 1;
          min-width: 220px;
        }

        .recent-name {
          font-weight: 700;
          color: var(--text-main);
          margin-bottom: 6px;
        }

        .recent-meta {
          color: var(--text-muted);
          font-size: 14px;
          line-height: 1.6;
        }

        .label-pill {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 800;
        }

        .label-pill.attentive {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.28);
        }

        .label-pill.neutral {
          background: rgba(255, 213, 72, 0.10);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.25);
        }

        .label-pill.distracted {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .error-box {
          margin-bottom: 16px;
          color: var(--danger);
          padding: 14px;
          background: rgba(248, 113, 113, 0.10);
          border-radius: 12px;
          border: 1px solid rgba(248, 113, 113, 0.22);
        }

        .empty-state {
          text-align: center;
          padding: 28px 20px;
          color: var(--text-muted);
          border: 1px dashed var(--border-color);
          border-radius: 18px;
          background: rgba(255,255,255,0.02);
        }

        .loading-text {
          color: var(--text-muted);
          font-size: 14px;
        }

        @media (max-width: 768px) {
          .recent-item {
            flex-direction: column;
            align-items: flex-start;
          }

          .course-select {
            width: 100%;
          }
        }
      `}</style>

      <div className="engagement-header">
        <div>
          <h3 className="engagement-title">
            <Brain size={22} />
            Course Engagement Analytics
          </h3>
          <div className="engagement-subtext">
            View attentive, neutral, and distracted trends for your course sessions.
          </div>
        </div>

        <select
          className="course-select"
          value={selectedCourseId}
          onChange={(e) => setSelectedCourseId(e.target.value)}
          disabled={loadingCourses || courses.length === 0}
        >
          {courses.length === 0 ? (
            <option value="">No courses available</option>
          ) : (
            courses.map((course) => (
              <option key={course.id} value={course.id}>
                {course.courseCode} - {course.title}
              </option>
            ))
          )}
        </select>
      </div>

      {error && <div className="error-box">{error}</div>}

      {loadingCourses ? (
        <div className="loading-text">Loading courses...</div>
      ) : courses.length === 0 ? (
        <div className="empty-state">
          <BookOpen size={34} style={{ marginBottom: "12px", opacity: 0.5 }} />
          <div>No teacher courses found.</div>
        </div>
      ) : loadingSummary ? (
        <div className="loading-text">Loading engagement analytics...</div>
      ) : !summary ? (
        <div className="empty-state">
          <Activity size={34} style={{ marginBottom: "12px", opacity: 0.5 }} />
          <div>No engagement summary available.</div>
        </div>
      ) : (
        <>
          <div style={{ marginBottom: "16px", color: "var(--text-muted)", fontSize: "14px" }}>
            Selected course:{" "}
            <strong style={{ color: "var(--text-main)" }}>
              {selectedCourse?.courseCode} - {selectedCourse?.title}
            </strong>
          </div>

          <div className="engagement-grid">
            <div className="engagement-stat">
              <div className="engagement-stat-icon">
                <Activity size={22} />
              </div>
              <div>
                <p>Total Logs</p>
                <h3>{summary.totalLogs ?? 0}</h3>
              </div>
            </div>

            <div className="engagement-stat">
              <div
                className="engagement-stat-icon"
                style={{ background: "rgba(74, 222, 128, 0.10)", color: "var(--success)" }}
              >
                <Eye size={22} />
              </div>
              <div>
                <p>Attentive</p>
                <h3>{summary.attentiveCount ?? 0}</h3>
              </div>
            </div>

            <div className="engagement-stat">
              <div
                className="engagement-stat-icon"
                style={{ background: "rgba(255, 213, 72, 0.10)", color: "var(--accent)" }}
              >
                <Users size={22} />
              </div>
              <div>
                <p>Neutral</p>
                <h3>{summary.neutralCount ?? 0}</h3>
              </div>
            </div>

            <div className="engagement-stat">
              <div
                className="engagement-stat-icon"
                style={{ background: "rgba(248, 113, 113, 0.10)", color: "var(--danger)" }}
              >
                <AlertTriangle size={22} />
              </div>
              <div>
                <p>Distracted</p>
                <h3>{summary.distractedCount ?? 0}</h3>
              </div>
            </div>

            <div className="engagement-stat">
              <div
                className="engagement-stat-icon"
                style={{ background: "rgba(96, 165, 250, 0.10)", color: "#60a5fa" }}
              >
                <Brain size={22} />
              </div>
              <div>
                <p>Average Score</p>
                <h3>
                  {summary.averageScore != null
                    ? Number(summary.averageScore).toFixed(2)
                    : "0.00"}
                </h3>
              </div>
            </div>
          </div>

          <div className="recent-box">
            <h4 className="recent-title">Recent Engagement Logs</h4>

            {!summary.recentLogs || summary.recentLogs.length === 0 ? (
              <div className="empty-state">
                <Activity size={30} style={{ marginBottom: "10px", opacity: 0.5 }} />
                <div>No engagement logs recorded for this course yet.</div>
              </div>
            ) : (
              <div className="recent-list">
                {summary.recentLogs.map((item, index) => (
                  <div className="recent-item" key={`${item.studentId}-${index}`}>
                    <div className="recent-main">
                      <div className="recent-name">
                        {item.studentName || item.studentEmail || "Student"}
                      </div>
                      <div className="recent-meta">
                        Score: <strong>{item.engagementScore ?? "-"}</strong>
                        <br />
                        Confidence: <strong>{item.confidence != null ? Number(item.confidence).toFixed(2) : "-"}</strong>
                        <br />
                        Captured At: <strong>{formatDateTime(item.capturedAt)}</strong>
                      </div>
                    </div>

                    <div className={`label-pill ${getLabelClass(item.label)}`}>
                      {item.label || "UNKNOWN"}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default TeacherEngagementSummary;