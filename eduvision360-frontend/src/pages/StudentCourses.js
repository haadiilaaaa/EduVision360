import React, { useEffect, useState } from "react";
import { getCourses, enrollCourse } from "../services/academicService";
import { BookOpen, PlusCircle } from "lucide-react";

function StudentCourses() {
  const [courses, setCourses] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    setMessage("");
    try {
      const res = await getCourses();
      setCourses(res.data || []);
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to load courses");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleEnroll = async (courseId) => {
    setMessage("");
    try {
      await enrollCourse(courseId);
      setMessage("✅ Enrolled successfully");
    } catch (err) {
      setMessage(err?.response?.data?.message || "Enroll failed");
    }
  };

  return (
    <div className="student-courses-page">
      <style>{`
        .student-courses-page {
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

        .enroll-btn {
          margin-top: 12px;
          width: 100%;
          padding: 10px 12px;
          border-radius: 14px;
          border: 1px solid rgba(255, 213, 72, 0.35);
          background: var(--accent-soft);
          color: var(--accent);
          font-weight: 800;
          cursor: pointer;
          display: flex;
          gap: 8px;
          justify-content: center;
          align-items: center;
        }
      `}</style>

      <h2 className="page-title">
        <BookOpen /> Available Courses
      </h2>

      {message && <div className="message-box">{message}</div>}

      {loading ? (
        <div className="muted-text">Loading...</div>
      ) : courses.length === 0 ? (
        <div className="muted-text">No courses found.</div>
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

              <button className="enroll-btn" onClick={() => handleEnroll(c.id)}>
                <PlusCircle size={16} /> Enroll
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default StudentCourses;