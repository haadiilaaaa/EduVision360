import React, { useEffect, useState } from "react";
import Menu from "../components/Menu";
import { getMyTeacherCourses, getTeacherAttendance } from "../services/academicService";
import { Search, RefreshCcw, BarChart3 } from "lucide-react";

function TeacherAttendanceView() {
  const [courses, setCourses] = useState([]);
  const [classId, setClassId] = useState("");
  const [date, setDate] = useState("");
  const [status, setStatus] = useState("");
  const [search, setSearch] = useState("");

  const [records, setRecords] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const loadCourses = async () => {
    try {
      const res = await getMyTeacherCourses();
      setCourses(res.data || []);
    } catch (err) {
      setMessage("Failed to load your courses");
    }
  };

  const loadAttendance = async (override = null) => {
    setLoading(true);
    setMessage("");

    try {
      const f = override || { classId, date, status, search };

      const params = {};
      if (f.classId) params.classId = f.classId;
      if (f.date) params.date = f.date;
      if (f.status) params.status = f.status;
      if (f.search?.trim()) params.search = f.search.trim();

      const res = await getTeacherAttendance(params);
      setRecords(res.data || []);
    } catch (err) {
      console.log("Teacher attendance load failed", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to load attendance");
      setRecords([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCourses();
    loadAttendance();
  }, []);

  const handleReset = () => {
    setClassId("");
    setDate("");
    setStatus("");
    setSearch("");
    setMessage("");
    loadAttendance({ classId: "", date: "", status: "", search: "" });
  };

  const total = records.length;
  const presentCount = records.filter((r) => r.status === "PRESENT").length;
  const uncertainCount = records.filter((r) => r.status === "UNCERTAIN").length;
  const rejectedCount = records.filter((r) => r.status === "REJECTED").length;

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
          margin: 0 0 22px 0;
        }

        .panel {
          background: var(--card-bg);
          border: 1px solid var(--border-color);
          border-radius: 22px;
          padding: 22px;
          margin-bottom: 18px;
          box-shadow: var(--card-shadow);
        }

        .filters {
          display: flex;
          gap: 12px;
          flex-wrap: wrap;
        }

        .input,
        .select {
          background: var(--input-bg);
          border: 1px solid var(--input-border);
          color: var(--text-primary);
          padding: 12px 14px;
          border-radius: 14px;
          outline: none;
          min-width: 200px;
        }

        .select option {
          background: var(--card-bg);
          color: var(--text-primary);
        }

        .primary-btn,
        .secondary-btn {
          padding: 12px 16px;
          border-radius: 14px;
          cursor: pointer;
          font-weight: 700;
          display: flex;
          align-items: center;
          gap: 8px;
          border: none;
        }

        .primary-btn {
          background: var(--button-secondary-bg);
          border: 1px solid var(--button-secondary-border);
          color: var(--button-secondary-text);
        }

        .secondary-btn {
          background: var(--input-bg);
          border: 1px solid var(--input-border);
          color: var(--text-primary);
        }

        .message {
          color: var(--accent);
          font-weight: 600;
          margin-bottom: 10px;
        }

        .summary-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
          gap: 14px;
          margin-bottom: 18px;
        }

        .summary-card {
          background: var(--card-bg);
          border: 1px solid var(--border-color);
          border-radius: 18px;
          padding: 18px;
          box-shadow: var(--card-shadow);
        }

        .summary-card h4 {
          margin: 0 0 10px 0;
          color: var(--text-secondary);
          font-size: 14px;
        }

        .summary-card .value {
          font-size: 28px;
          font-weight: 800;
          color: var(--accent);
        }

        .table-wrapper {
          overflow-x: auto;
        }

        table {
          width: 100%;
          border-collapse: collapse;
        }

        th,
        td {
          padding: 14px 12px;
          border-bottom: 1px solid var(--border-color);
          text-align: left;
          color: var(--text-primary);
        }

        th {
          color: var(--accent);
          font-weight: 700;
        }

        .status-badge {
          display: inline-block;
          padding: 6px 10px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 700;
        }

        .status-PRESENT {
          background: rgba(74,222,128,0.10);
          color: #4ade80;
          border: 1px solid rgba(74,222,128,0.30);
        }

        .status-UNCERTAIN {
          background: rgba(255,213,72,0.12);
          color: #ffd548;
          border: 1px solid rgba(255,213,72,0.35);
        }

        .status-REJECTED {
          background: rgba(248,113,113,0.08);
          color: #f87171;
          border: 1px solid rgba(248,113,113,0.25);
        }

        .loading-text,
        .empty-text {
          color: var(--text-secondary);
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <div className="page-header">
          <h2>Teacher Attendance View</h2>
          <p>View attendance only for the courses assigned to you.</p>
        </div>

        <div className="panel">
          {message && <div className="message">{message}</div>}

          <div className="filters">
            <select className="select" value={classId} onChange={(e) => setClassId(e.target.value)}>
              <option value="">All My Courses</option>
              {courses.map((c) => (
                <option key={c.id} value={c.courseCode}>
                  {c.courseCode} — {c.title}
                </option>
              ))}
            </select>

            <input className="input" type="date" value={date} onChange={(e) => setDate(e.target.value)} />

            <select className="select" value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="">All Statuses</option>
              <option value="PRESENT">PRESENT</option>
              <option value="UNCERTAIN">UNCERTAIN</option>
              <option value="REJECTED">REJECTED</option>
            </select>

            <input
              className="input"
              placeholder="Search by student name/email"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

            <button className="primary-btn" onClick={() => loadAttendance()}>
              <Search size={16} /> Search
            </button>

            <button className="secondary-btn" onClick={handleReset}>
              <RefreshCcw size={16} /> Reset
            </button>
          </div>
        </div>

        <div className="summary-grid">
          <div className="summary-card"><h4>Total</h4><div className="value">{total}</div></div>
          <div className="summary-card"><h4>Present</h4><div className="value">{presentCount}</div></div>
          <div className="summary-card"><h4>Uncertain</h4><div className="value">{uncertainCount}</div></div>
          <div className="summary-card"><h4>Rejected</h4><div className="value">{rejectedCount}</div></div>
        </div>

        <div className="panel">
          <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 14 }}>
            <BarChart3 size={18} style={{ color: "var(--accent)" }} />
            <h3 style={{ margin: 0, color: "var(--accent)" }}>Attendance Records</h3>
          </div>

          {loading ? (
            <div className="loading-text">Loading...</div>
          ) : records.length === 0 ? (
            <div className="empty-text">No attendance records found.</div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Student</th>
                    <th>Email</th>
                    <th>Class</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Status</th>
                    <th>Confidence</th>
                  </tr>
                </thead>
                <tbody>
                  {records.map((r) => (
                    <tr key={r.id}>
                      <td>{r.studentName}</td>
                      <td>{r.studentEmail}</td>
                      <td>{r.classId}</td>
                      <td>{r.date}</td>
                      <td>{r.time}</td>
                      <td><span className={`status-badge status-${r.status}`}>{r.status}</span></td>
                      <td>{r.confidence ?? "N/A"}</td>
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

export default TeacherAttendanceView;