import React, { useEffect, useMemo, useState } from "react";
import { getMyEnrollments, getStudentAnnouncements } from "../services/academicService";
import { Megaphone } from "lucide-react";

function StudentAnnouncements() {
  const [enrollments, setEnrollments] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [message, setMessage] = useState("");

  const loadData = async () => {
    try {
      setMessage("");

      const [enrollmentRes, announcementRes] = await Promise.all([
        getMyEnrollments(),
        getStudentAnnouncements()
      ]);

      const enrolledCourses = enrollmentRes.data || [];
      const announcementList = announcementRes.data || [];

      setEnrollments(enrolledCourses);
      setAnnouncements(announcementList);

      if (enrolledCourses.length > 0) {
        setSelectedCourseId((prev) => prev || enrolledCourses[0].courseId);
      } else {
        setSelectedCourseId("");
      }
    } catch (err) {
      console.log("Load student announcements error:", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to load announcements");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filteredAnnouncements = useMemo(() => {
    if (!selectedCourseId) return announcements;
    return announcements.filter((a) => a.courseId === selectedCourseId);
  }, [announcements, selectedCourseId]);

  return (
    <div className="student-announcements-page">
      <style>{`
        .student-announcements-page {
          color: var(--text-main);
        }

        .page-title {
          margin-top: 0;
          display: flex;
          gap: 10px;
          align-items: center;
        }

        .message-box {
          color: var(--accent);
          margin-bottom: 12px;
          font-weight: 600;
        }

        .muted-text {
          color: var(--text-muted);
        }

        .select-label {
          display: block;
          margin-bottom: 8px;
          color: var(--text-main);
          font-weight: 600;
        }

        .course-select {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
          padding: 12px 14px;
          border-radius: 14px;
          min-width: 300px;
          outline: none;
        }

        .announcements-grid {
          display: grid;
          gap: 14px;
        }

        .announcement-card {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 18px;
          padding: 16px;
        }

        .announcement-title {
          font-weight: 800;
          font-size: 17px;
          color: var(--text-main);
        }

        .announcement-meta {
          color: var(--text-muted);
          font-size: 13px;
          margin-top: 6px;
          line-height: 1.6;
        }

        .announcement-message {
          margin-top: 12px;
          color: var(--text-main);
          white-space: pre-wrap;
          word-break: break-word;
        }

        .announcement-time {
          color: var(--text-dim);
          font-size: 12px;
          margin-top: 10px;
        }
      `}</style>

      <h2 className="page-title">
        <Megaphone /> Course Announcements
      </h2>

      {message && <div className="message-box">{message}</div>}

      {enrollments.length === 0 ? (
        <div className="muted-text">You are not enrolled in any courses yet.</div>
      ) : (
        <>
          <div style={{ marginBottom: 18 }}>
            <label className="select-label">Select Course</label>
            <select
              className="course-select"
              value={selectedCourseId}
              onChange={(e) => setSelectedCourseId(e.target.value)}
            >
              {enrollments.map((c) => (
                <option key={c.courseId} value={c.courseId}>
                  {c.courseCode} - {c.title}
                </option>
              ))}
            </select>
          </div>

          {filteredAnnouncements.length === 0 ? (
            <div className="muted-text">No announcements available for this course yet.</div>
          ) : (
            <div className="announcements-grid">
              {filteredAnnouncements.map((a) => (
                <div key={a.id} className="announcement-card">
                  <div className="announcement-title">{a.title}</div>

                  <div className="announcement-meta">
                    Course: {a.courseCode} - {a.courseTitle || "-"} <br />
                    Teacher: {a.teacherName || "-"}
                  </div>

                  <div className="announcement-message">{a.message}</div>

                  {(a.updatedAt || a.createdAt) && (
                    <div className="announcement-time">
                      {a.updatedAt ? "Updated: " : "Posted: "}
                      {new Date(a.updatedAt || a.createdAt).toLocaleString()}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default StudentAnnouncements;