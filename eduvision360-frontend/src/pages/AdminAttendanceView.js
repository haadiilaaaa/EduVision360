import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import Menu from "../components/Menu";
import { Search, RefreshCcw, BarChart3 } from "lucide-react";

function AdminAttendanceView() {
  const token = localStorage.getItem("token");

  const axiosAuth = useMemo(() => {
    return axios.create({
      baseURL: "http://localhost:8080",
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
  }, [token]);

  const [classId, setClassId] = useState("");
  const [date, setDate] = useState("");
  const [status, setStatus] = useState("");
  const [search, setSearch] = useState("");
  const [records, setRecords] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const fetchAttendance = async (overrideFilters = null) => {
    setLoading(true);
    setMessage("");

    try {
      const filters = overrideFilters || {
        classId,
        date,
        status,
        search
      };

      const params = {};

      if (filters.classId?.trim()) params.classId = filters.classId.trim();
      if (filters.date) params.date = filters.date;
      if (filters.status) params.status = filters.status;
      if (filters.search?.trim()) params.search = filters.search.trim();

      const res = await axiosAuth.get("/api/admin/attendance", { params });
      setRecords(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.log("Failed to load attendance", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to load attendance records");
      setRecords([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) return;
    fetchAttendance();
    // eslint-disable-next-line
  }, [token]);

  const handleReset = () => {
    const cleared = {
      classId: "",
      date: "",
      status: "",
      search: ""
    };

    setClassId("");
    setDate("");
    setStatus("");
    setSearch("");
    setMessage("");
    fetchAttendance(cleared);
  };

  const total = records.length;
  const presentCount = records.filter((r) => r.status === "PRESENT").length;
  const uncertainCount = records.filter((r) => r.status === "UNCERTAIN").length;
  const rejectedCount = records.filter((r) => r.status === "REJECTED").length;

  return (
    <div className="attendance-layout">
      <style>{`
        .attendance-layout {
          display: flex;
          min-height: 100vh;
          background: var(--bg-main);
          font-family: 'Inter', sans-serif;
        }

        .main-content {
          flex: 1;
          padding: 40px;
          color: var(--text-main);
        }

        .page-header {
          margin-bottom: 24px;
        }

        .page-header h2 {
          margin: 0 0 10px 0;
          font-size: 32px;
          background: linear-gradient(to right, var(--text-main), var(--accent), var(--text-main));
          background-size: 200% auto;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .page-header p {
          color: var(--text-muted);
          margin: 0;
        }

        .panel {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 20px;
          padding: 22px;
          backdrop-filter: blur(14px);
          margin-bottom: 20px;
        }

        .filters {
          display: flex;
          gap: 12px;
          flex-wrap: wrap;
          margin-bottom: 20px;
        }

        .input, .select {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
          padding: 12px 14px;
          border-radius: 14px;
          outline: none;
          min-width: 200px;
        }

        .input::placeholder {
          color: var(--text-muted);
        }

        .primary-btn, .secondary-btn {
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
          background: var(--accent-soft);
          border: 1px solid rgba(255, 213, 72, 0.35);
          color: var(--accent);
        }

        .secondary-btn {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
        }

        .message-text {
          color: var(--accent);
          margin-bottom: 14px;
          font-size: 14px;
          font-weight: 600;
        }

        .summary-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
          gap: 14px;
          margin-bottom: 20px;
        }

        .summary-card {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 18px;
          padding: 18px;
        }

        .summary-card h4 {
          margin: 0 0 10px 0;
          color: var(--text-muted);
          font-size: 14px;
          font-weight: 600;
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
          color: var(--text-main);
        }

        th, td {
          padding: 14px 12px;
          text-align: left;
          border-bottom: 1px solid var(--border-soft);
          font-size: 14px;
        }

        th {
          color: var(--accent);
          font-weight: 700;
        }

        .empty-state {
          color: var(--text-muted);
          padding: 20px 0;
        }

        .status-badge {
          display: inline-block;
          padding: 6px 10px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 700;
        }

        .status-PRESENT {
          background: rgba(74, 222, 128, 0.1);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.3);
        }

        .status-UNCERTAIN {
          background: rgba(255, 213, 72, 0.12);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.35);
        }

        .status-REJECTED {
          background: rgba(248, 113, 113, 0.08);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <div className="page-header">
          <h2>Admin Attendance View</h2>
          <p>Filter and review student attendance records.</p>
        </div>

        <div className="panel">
          {message && <div className="message-text">{message}</div>}

          <div className="filters">
            <input
              className="input"
              placeholder="Class ID"
              value={classId}
              onChange={(e) => setClassId(e.target.value)}
            />

            <input
              className="input"
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />

            <select
              className="select"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="PRESENT">PRESENT</option>
              <option value="UNCERTAIN">UNCERTAIN</option>
              <option value="REJECTED">REJECTED</option>
            </select>

            <input
              className="input"
              placeholder="Search by name, email, or class"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

            <button className="primary-btn" onClick={() => fetchAttendance()}>
              <Search size={16} /> Search
            </button>

            <button className="secondary-btn" onClick={handleReset}>
              <RefreshCcw size={16} /> Reset
            </button>
          </div>
        </div>

        <div className="summary-grid">
          <div className="summary-card">
            <h4>Total Records</h4>
            <div className="value">{total}</div>
          </div>

          <div className="summary-card">
            <h4>Present</h4>
            <div className="value">{presentCount}</div>
          </div>

          <div className="summary-card">
            <h4>Uncertain</h4>
            <div className="value">{uncertainCount}</div>
          </div>

          <div className="summary-card">
            <h4>Rejected</h4>
            <div className="value">{rejectedCount}</div>
          </div>
        </div>

        <div className="panel">
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "15px" }}>
            <BarChart3 size={18} color="var(--accent)" />
            <h3 style={{ margin: 0, color: "var(--accent)" }}>Attendance Records</h3>
          </div>

          {loading ? (
            <div className="empty-state">Loading attendance records...</div>
          ) : records.length === 0 ? (
            <div className="empty-state">No attendance records found.</div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Student Name</th>
                    <th>Email</th>
                    <th>Class ID</th>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Status</th>
                    <th>Confidence</th>
                  </tr>
                </thead>
                <tbody>
                  {records.map((r) => (
                    <tr key={r.id}>
                      <td>{r.studentName || "-"}</td>
                      <td>{r.studentEmail || "-"}</td>
                      <td>{r.classId || "-"}</td>
                      <td>{r.date || "-"}</td>
                      <td>{r.time || "-"}</td>
                      <td>
                        <span className={`status-badge status-${r.status}`}>
                          {r.status}
                        </span>
                      </td>
                      <td>{r.confidence != null ? r.confidence : "N/A"}</td>
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

export default AdminAttendanceView;