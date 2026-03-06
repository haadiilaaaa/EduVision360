import React, { useEffect, useState } from "react";
import { getMyEnrollments } from "../services/academicService";
import { useNavigate } from "react-router-dom";
import { BookOpen, CalendarCheck } from "lucide-react";

function StudentMyCourses() {
  const [courses, setCourses] = useState([]);
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const load = async () => {
    setMessage("");
    try {
      const res = await getMyEnrollments();
      setCourses(res.data || []);
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to load enrollments");
    }
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="student-my-courses-page">
      <style>{`
        .student-my-courses-page {
          color: var(--text-main);
        }

        .page-title {
          margin-top: 0;
          display: flex;
          gap: 10px;
          align-items: center;
          color: var(--text-main);
        }

        .message-box {
          color: var(--accent);
          margin-bottom: 12px;
          font-weight: 600;
        }

        .muted-text {
          color: var(--text-muted);
        }

        .cards-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
          gap: 14px;
        }

        .course-card {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 18px;
          padding: 16px;
        }

        .course-code {
          font-weight: 800;
          color: var(--accent);
        }

        .course-title {
          font-size: 16px;
          font-weight: 700;
          color: var(--text-main);
        }

        .course-meta {
          color: var(--text-muted);
          font-size: 13px;
          margin-top: 6px;
          line-height: 1.6;
        }

        .attendance-btn {
          margin-top: 12px;
          width: 100%;
          padding: 10px 12px;
          border-radius: 14px;
          border: 1px solid rgba(74, 222, 128, 0.35);
          background: rgba(74, 222, 128, 0.14);
          color: var(--success);
          font-weight: 800;
          cursor: pointer;
          display: flex;
          gap: 8px;
          justify-content: center;
          align-items: center;
        }
      `}</style>

      <h2 className="page-title">
        <BookOpen /> My Enrolled Courses
      </h2>

      {message && <div className="message-box">{message}</div>}

      {courses.length === 0 ? (
        <div className="muted-text">You haven’t enrolled in any courses yet.</div>
      ) : (
        <div className="cards-grid">
          {courses.map((c) => (
            <div key={c.id} className="course-card">
              <div className="course-code">{c.courseCode}</div>
              <div className="course-title">{c.title}</div>
              <div className="course-meta">
                Dept: {c.departmentName || "-"} <br />
                Teacher: {c.teacherName || "-"}
              </div>

              <button
                className="attendance-btn"
                onClick={() => navigate(`/student/attendance/${c.courseCode}`)}
              >
                <CalendarCheck size={16} /> Mark Attendance
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default StudentMyCourses;