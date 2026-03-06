import React, { useEffect, useState } from "react";
import { getMyTeacherCourses } from "../services/academicService";
import Menu from "../components/Menu";
import { BookOpen } from "lucide-react";

function TeacherCourses() {
  const [courses, setCourses] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const loadCourses = async () => {
      try {
        const res = await getMyTeacherCourses();
        setCourses(res.data || []);
      } catch (err) {
        console.log("Failed to load teacher courses", err?.response?.status, err?.response?.data);
        setMessage("Failed to load your courses");
      }
    };

    loadCourses();
  }, []);

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

        .panel {
          background: var(--card-bg);
          border: 1px solid var(--border-color);
          border-radius: 22px;
          padding: 22px;
          box-shadow: var(--card-shadow);
        }

        .message {
          color: var(--accent);
          margin-bottom: 14px;
          font-weight: 600;
        }

        .empty-state {
          color: var(--text-secondary);
          padding: 16px 0;
        }

        .table-wrapper {
          overflow-x: auto;
        }

        table {
          width: 100%;
          border-collapse: collapse;
        }

        th, td {
          padding: 14px 12px;
          text-align: left;
          border-bottom: 1px solid var(--border-color);
          color: var(--text-primary);
        }

        th {
          color: var(--accent);
          font-weight: 700;
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <div className="page-header">
          <h2>My Courses</h2>
          <p>These are the courses currently assigned to your teacher account.</p>
        </div>

        <div className="panel">
          {message && <div className="message">{message}</div>}

          {courses.length === 0 ? (
            <div className="empty-state">
              <BookOpen size={28} style={{ marginBottom: "10px", opacity: 0.7 }} />
              <div>No assigned courses found.</div>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Title</th>
                    <th>Department</th>
                    <th>Semester</th>
                    <th>Academic Year</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {courses.map((course) => (
                    <tr key={course.id}>
                      <td>{course.courseCode}</td>
                      <td>{course.title}</td>
                      <td>{course.departmentName || "-"}</td>
                      <td>{course.semester}</td>
                      <td>{course.academicYear}</td>
                      <td>{course.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default TeacherCourses;