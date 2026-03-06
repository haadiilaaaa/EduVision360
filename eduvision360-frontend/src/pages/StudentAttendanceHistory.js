import React, { useEffect, useState } from "react";
import {
  Calendar,
  BookOpen,
  CheckCircle,
  XCircle,
  History,
  AlertTriangle
} from "lucide-react";

function StudentAttendanceHistory() {
  const [records, setRecords] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadHistory = async () => {
      try {
        const token = localStorage.getItem("token");
        const res = await fetch("http://localhost:8080/api/attendance/my", {
          headers: { Authorization: `Bearer ${token}` }
        });

        if (!res.ok) {
          throw new Error("Failed to load attendance history");
        }

        const data = await res.json();
        setRecords(Array.isArray(data) ? data : []);
      } catch {
        setError("Failed to load attendance history");
      }
    };

    loadHistory();
  }, []);

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

  return (
    <div className="history-container">
      <style>{`
        .history-container {
          animation: fadeIn 0.5s ease-out;
          color: var(--text-main);
        }

        .history-header {
          margin-bottom: 25px;
          display: flex;
          align-items: center;
          gap: 12px;
        }

        .history-header h2 {
          font-size: 24px;
          margin: 0;
          background: linear-gradient(to right, var(--text-main), var(--accent));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .table-wrapper {
          background: var(--bg-card);
          backdrop-filter: blur(20px);
          border-radius: 20px;
          border: 1px solid var(--border-soft);
          border-top: 1px solid var(--border-strong);
          border-left: 1px solid var(--border-mid);
          overflow: hidden;
          box-shadow: var(--shadow-lg);
          position: relative;
        }

        .table-wrapper:hover {
          border-top: 1px solid rgba(255, 213, 72, 0.4);
          transition: 0.4s;
        }

        table {
          width: 100%;
          border-collapse: collapse;
          text-align: left;
        }

        th {
          background: rgba(255, 213, 72, 0.07);
          padding: 18px 25px;
          font-size: 13px;
          text-transform: uppercase;
          letter-spacing: 1.5px;
          color: var(--accent);
          border-bottom: 1px solid var(--border-color);
        }

        td {
          padding: 18px 25px;
          font-size: 14px;
          color: var(--text-muted);
          border-bottom: 1px solid rgba(255, 255, 255, 0.05);
          transition: 0.3s;
        }

        tr:hover td {
          background: rgba(255, 255, 255, 0.03);
          color: var(--text-main);
        }

        .status-badge {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 6px 14px;
          border-radius: 50px;
          font-size: 12px;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .status-PRESENT {
          background: rgba(74, 222, 128, 0.1);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.3);
          box-shadow: 0 0 10px rgba(74, 222, 128, 0.1);
        }

        .status-UNCERTAIN {
          background: rgba(255, 213, 72, 0.12);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.35);
          box-shadow: 0 0 10px rgba(255, 213, 72, 0.1);
        }

        .status-REJECTED {
          background: rgba(248, 113, 113, 0.1);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.3);
          box-shadow: 0 0 10px rgba(248, 113, 113, 0.1);
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(15px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>

      <div className="history-header">
        <History
          size={26}
          color="var(--accent)"
          style={{ filter: "drop-shadow(0 0 5px rgba(255, 213, 72, 0.5))" }}
        />
        <h2>Attendance Records</h2>
      </div>

      {error && (
        <div
          style={{
            color: "var(--danger)",
            padding: "15px",
            background: "rgba(248, 113, 113, 0.1)",
            borderRadius: "12px",
            marginBottom: "20px",
            border: "1px solid rgba(248, 113, 113, 0.2)"
          }}
        >
          {error}
        </div>
      )}

      <div className="table-wrapper">
        {records.length === 0 ? (
          <div style={{ padding: "60px", textAlign: "center", color: "var(--text-dim)", fontSize: "16px" }}>
            <Calendar size={40} style={{ marginBottom: "15px", opacity: 0.25 }} />
            <p>No attendance records found yet.</p>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th><Calendar size={14} style={{ marginRight: 8, verticalAlign: "middle" }} /> Date</th>
                <th><BookOpen size={14} style={{ marginRight: 8, verticalAlign: "middle" }} /> Course ID</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {records.map((r) => (
                <tr key={r.id}>
                  <td>{formatDate(r.date)}</td>
                  <td style={{ fontWeight: "600", color: "var(--text-main)", letterSpacing: "0.5px" }}>
                    {r.classId}
                  </td>
                  <td>
                    <span className={`status-badge status-${r.status}`}>
                      {r.status === "PRESENT" ? (
                        <CheckCircle size={14} />
                      ) : r.status === "UNCERTAIN" ? (
                        <AlertTriangle size={14} />
                      ) : (
                        <XCircle size={14} />
                      )}
                      {r.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default StudentAttendanceHistory;