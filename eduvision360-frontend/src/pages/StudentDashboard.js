import React, { useEffect, useMemo, useState } from "react";
import { logout, getUserEmail, getUserFullName, getUserInitial } from "../utils/auth";
import { Outlet, useNavigate } from "react-router-dom";
import Menu from "../components/Menu";
import {
  LogOut,
  LayoutDashboard,
  Bell,
  BookOpen,
  CalendarCheck,
  History,
  Clock3,
  AlertTriangle,
  CalendarDays,
  Brain,
  ShieldAlert
} from "lucide-react";
import {
  getMyEnrollments,
  getStudentSessions,
  getMyAttendanceHistory
} from "../services/academicService";
import {
  getMyLatestStudentPrediction,
  getMyAllStudentPredictions
} from "../services/predictionService";

function StudentDashboard() {
  const navigate = useNavigate();

  const email = getUserEmail();
  const fullName = getUserFullName();
  const initial = getUserInitial();
  const displayName = fullName || email || "System User";

  const [enrollments, setEnrollments] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [attendanceRecords, setAttendanceRecords] = useState([]);
  const [latestPrediction, setLatestPrediction] = useState(null);
  const [allPredictions, setAllPredictions] = useState([]);
  const [error, setError] = useState("");

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        const latestPredictionPromise = getMyLatestStudentPrediction().catch((err) => {
          console.error("Latest student prediction failed", err?.response?.status, err?.response?.data);
          return { data: null };
        });

        const allPredictionsPromise = getMyAllStudentPredictions().catch((err) => {
          console.error("All student predictions failed", err?.response?.status, err?.response?.data);
          return { data: [] };
        });

        const [enrollmentsRes, sessionsRes, attendanceRes, latestPredictionRes, allPredictionsRes] =
          await Promise.all([
            getMyEnrollments(),
            getStudentSessions(),
            getMyAttendanceHistory(),
            latestPredictionPromise,
            allPredictionsPromise
          ]);

        setEnrollments(enrollmentsRes.data || []);
        setSessions(sessionsRes.data || []);
        setAttendanceRecords(attendanceRes.data || []);
        setLatestPrediction(latestPredictionRes.data || null);
        setAllPredictions(allPredictionsRes.data || []);
      } catch (err) {
        console.error("Failed to load student dashboard data", err);
        setError("Failed to load dashboard data");
      }
    };

    loadDashboardData();
  }, []);

  const scheduledSessions = useMemo(() => {
    return (sessions || []).filter((session) => session.status === "SCHEDULED");
  }, [sessions]);

  const openSessions = useMemo(() => {
    return (sessions || []).filter((session) => session.status === "OPEN");
  }, [sessions]);

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

  const getRiskClass = (riskLevel) => {
    if (riskLevel === "HIGH") return "high";
    if (riskLevel === "MEDIUM") return "medium";
    return "low";
  };

  return (
    <>
      <style>{`
        .dashboard-layout { display: flex; min-height: 100vh; background: var(--bg-main); color: var(--text-main); font-family: 'Inter', sans-serif; }
        .main-content { flex: 1; display: flex; flex-direction: column; }
        .top-nav { height: 70px; background: var(--bg-glass); backdrop-filter: blur(10px); border-bottom: 1px solid var(--border-color);
          display: flex; align-items: center; justify-content: space-between; padding: 0 40px; position: sticky; top: 0; z-index: 90; }
        .content-area { padding: 40px; }
        .welcome-banner { background: var(--bg-card); border: 1px solid var(--border-soft); border-top: 1px solid var(--border-strong);
          border-left: 1px solid var(--border-mid); padding: 30px 40px; border-radius: 24px; margin-bottom: 30px; backdrop-filter: blur(20px); box-shadow: var(--shadow-lg); }
        .welcome-banner h2 { margin: 0; font-size: 32px; background: linear-gradient(to right, var(--text-main), var(--accent), var(--text-main));
          background-size: 200% auto; -webkit-background-clip: text; -webkit-text-fill-color: transparent; animation: textShine 4s linear infinite; }
        @keyframes textShine { to { background-position: 200% center; } }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .stat-card { background: var(--bg-card); border: 1px solid var(--border-soft); border-top: 1px solid var(--border-strong);
          padding: 24px; border-radius: 20px; display: flex; align-items: center; gap: 20px; position: relative; overflow: hidden;
          transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
        .stat-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center;
          background: var(--accent-soft); color: var(--accent); box-shadow: inset 0 0 10px rgba(255, 213, 72, 0.1); }
        .stat-info p { margin: 0; font-size: 11px; color: var(--text-dim); text-transform: uppercase; letter-spacing: 1.5px; }
        .stat-info h3 { margin: 5px 0 0 0; font-size: 26px; font-weight: 700; color: var(--text-main); }
        .profile-pill { display: flex; align-items: center; gap: 12px; background: var(--bg-card-2); padding: 6px 16px; border-radius: 50px; border: 1px solid var(--border-color); }
        .logout-icon-btn { background: none; border: none; color: var(--danger); cursor: pointer; padding: 8px; transition: 0.3s; }
        .logout-icon-btn:hover { transform: scale(1.1); filter: drop-shadow(0 0 5px rgba(248, 113, 113, 0.4)); }
        .sessions-panel, .risk-panel, .predictions-panel { background: var(--bg-card); border: 1px solid var(--border-soft); border-top: 1px solid var(--border-strong);
          border-left: 1px solid var(--border-mid); border-radius: 24px; padding: 24px; margin-bottom: 30px; box-shadow: var(--shadow-lg); }
        .sessions-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 18px; flex-wrap: wrap; }
        .sessions-header h3, .risk-title, .predictions-title { margin: 0; display: flex; align-items: center; gap: 10px; color: var(--accent); font-size: 22px; }
        .sessions-subtext { color: var(--text-muted); font-size: 14px; }
        .session-list, .prediction-list { display: flex; flex-direction: column; gap: 14px; }
        .session-card, .prediction-card { background: var(--bg-card-2); border: 1px solid var(--border-color); border-radius: 18px;
          padding: 18px; display: flex; justify-content: space-between; gap: 16px; align-items: center; flex-wrap: wrap; }
        .session-title, .prediction-title { font-size: 17px; font-weight: 700; color: var(--text-main); margin-bottom: 6px; }
        .session-meta, .prediction-meta { color: var(--text-muted); font-size: 14px; line-height: 1.6; }
        .session-badge { display: inline-flex; align-items: center; gap: 8px; padding: 6px 12px; border-radius: 999px; font-size: 12px; font-weight: 700; margin-bottom: 10px; }
        .badge-scheduled { background: rgba(255, 213, 72, 0.10); color: var(--accent); border: 1px solid rgba(255, 213, 72, 0.28); }
        .badge-open { background: rgba(74, 222, 128, 0.10); color: var(--success); border: 1px solid rgba(74, 222, 128, 0.28); }
        .attendance-btn { background: var(--accent); color: #111827; border: none; padding: 12px 16px; border-radius: 14px; font-weight: 700; cursor: pointer; transition: 0.25s ease; }
        .attendance-btn:hover { transform: translateY(-2px); opacity: 0.95; }
        .empty-state { text-align: center; padding: 32px 20px; color: var(--text-muted); border: 1px dashed var(--border-color); border-radius: 18px; background: rgba(255,255,255,0.02); }
        .error-box { margin-bottom: 20px; color: var(--danger); padding: 15px; background: rgba(248, 113, 113, 0.1); border-radius: 12px; border: 1px solid rgba(248, 113, 113, 0.2); }
        .risk-pill { display: inline-flex; align-items: center; gap: 8px; padding: 8px 14px; border-radius: 999px; font-size: 13px; font-weight: 700; margin-top: 10px; }
        .risk-pill.high { background: rgba(239, 68, 68, 0.18); color: #dc2626; border: 1px solid rgba(239, 68, 68, 0.35); }
        .risk-pill.medium { background: rgba(245, 158, 11, 0.18); color: #d97706; border: 1px solid rgba(245, 158, 11, 0.35); }
        .risk-pill.low { background: rgba(34, 197, 94, 0.18); color: #16a34a; border: 1px solid rgba(34, 197, 94, 0.35); }
        .action-btn-group { display: flex; gap: 10px; flex-wrap: wrap; }
        .engagement-btn { background: rgba(96, 165, 250, 0.14); color: #60a5fa; border: 1px solid rgba(96, 165, 250, 0.28);
          padding: 12px 16px; border-radius: 14px; font-weight: 700; cursor: pointer; transition: 0.25s ease; }
        .engagement-btn:hover { transform: translateY(-2px); opacity: 0.95; }
        @media (max-width: 768px) {
          .top-nav { padding: 0 18px; }
          .content-area { padding: 20px; }
          .welcome-banner { padding: 24px; }
          .welcome-banner h2 { font-size: 26px; }
          .session-card, .prediction-card { flex-direction: column; align-items: flex-start; }
          .attendance-btn { width: 100%; }
          .action-btn-group { width: 100%; flex-direction: column; }
          .engagement-btn { width: 100%; }
        }
      `}</style>

      <div className="dashboard-layout">
        <Menu />

        <main className="main-content">
          <header className="top-nav">
            <div style={{ display: "flex", alignItems: "center", gap: "10px", color: "var(--text-dim)" }}>
              <LayoutDashboard size={18} />
              <span style={{ fontSize: "14px" }}>Student Portal / Overview</span>
            </div>

            <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
              <Bell size={20} color="var(--text-dim)" />

              <div className="profile-pill">
                <div style={{ display: "flex", flexDirection: "column", lineHeight: 1.2 }}>
                  <span style={{ fontSize: "13px", fontWeight: 700, color: "var(--text-main)" }}>
                    {displayName}
                  </span>
                  <span style={{ fontSize: "12px", color: "var(--text-muted)" }}>
                    {email || ""}
                  </span>
                </div>

                <div
                  style={{
                    width: 30,
                    height: 30,
                    background: "var(--accent)",
                    borderRadius: "50%",
                    color: "#000",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontWeight: "bold",
                    boxShadow: "0 0 10px rgba(255, 213, 72, 0.3)"
                  }}
                >
                  {initial}
                </div>
              </div>

              <button onClick={handleLogout} className="logout-icon-btn" title="Logout">
                <LogOut size={20} />
              </button>
            </div>
          </header>

          <div className="content-area">
            <div className="welcome-banner">
              <h2>Student Dashboard</h2>
              <p style={{ color: "var(--text-muted)", marginTop: "10px", fontSize: "16px" }}>
                Welcome back, <span style={{ color: "var(--accent)" }}>{displayName}</span>. Ready to continue your learning journey?
              </p>
            </div>

            {error && <div className="error-box">{error}</div>}

            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-icon"><BookOpen size={24} /></div>
                <div className="stat-info">
                  <p>Enrolled Courses</p>
                  <h3>{enrollments.length}</h3>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ color: "var(--success)", background: "rgba(74, 222, 128, 0.1)" }}>
                  <CalendarCheck size={24} />
                </div>
                <div className="stat-info">
                  <p>Attendance Records</p>
                  <h3>{attendanceRecords.length}</h3>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ color: "#60a5fa", background: "rgba(96, 165, 250, 0.1)" }}>
                  <History size={24} />
                </div>
                <div className="stat-info">
                  <p>Open Sessions</p>
                  <h3>{openSessions.length}</h3>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-icon" style={{ color: "#f59e0b", background: "rgba(245, 158, 11, 0.1)" }}>
                  <Brain size={24} />
                </div>
                <div className="stat-info">
                  <p>Latest Risk Level</p>
                  <h3>{latestPrediction?.riskLevel || "N/A"}</h3>
                </div>
              </div>
            </div>

            <div className="risk-panel">
              <h3 className="risk-title"><ShieldAlert size={22} /> My Dropout Risk Snapshot</h3>

              {!latestPrediction ? (
                <div className="empty-state">
                  <Brain size={34} style={{ marginBottom: "12px", opacity: 0.5 }} />
                  <div>No prediction has been generated for you yet.</div>
                </div>
              ) : (
                <>
                  <div style={{ color: "var(--text-muted)", lineHeight: 1.7 }}>
                    Course: <strong>{latestPrediction.courseCode} - {latestPrediction.courseTitle}</strong><br />
                    Dropout Probability: <strong>{latestPrediction.dropoutProbability}</strong><br />
                    Predicted Label: <strong>{latestPrediction.predictedLabel}</strong>
                  </div>

                  <div className={`risk-pill ${getRiskClass(latestPrediction.riskLevel)}`}>
                    Risk Level: {latestPrediction.riskLevel}
                  </div>

                  <p style={{ color: "var(--text-muted)", marginTop: "14px" }}>
                    This prediction is used to support learning interventions. It is not a final academic decision.
                  </p>
                </>
              )}
            </div>

            <div className="predictions-panel">
              <h3 className="predictions-title"><Brain size={22} /> My Course Risk Predictions</h3>

              {allPredictions.length === 0 ? (
                <div className="empty-state">
                  <Brain size={34} style={{ marginBottom: "12px", opacity: 0.5 }} />
                  <div>No saved course predictions yet.</div>
                </div>
              ) : (
                <div className="prediction-list">
                  {allPredictions.map((item, index) => (
                    <div className="prediction-card" key={`${item.studentId}-${item.courseId}-${index}`}>
                      <div>
                        <div className="prediction-title">{item.courseCode} - {item.courseTitle}</div>
                        <div className="prediction-meta">
                          Dropout Probability: {item.dropoutProbability}<br />
                          Predicted Label: {item.predictedLabel}
                        </div>
                      </div>
                      <div className={`risk-pill ${getRiskClass(item.riskLevel)}`}>{item.riskLevel}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="sessions-panel">
              <div className="sessions-header">
                <h3><CalendarDays size={22} /> Scheduled Sessions</h3>
                <span className="sessions-subtext">Upcoming sessions created by your teacher.</span>
              </div>

              {scheduledSessions.length === 0 ? (
                <div className="empty-state">
                  <AlertTriangle size={34} style={{ marginBottom: "12px", opacity: 0.5 }} />
                  <div>No scheduled sessions right now.</div>
                </div>
              ) : (
                <div className="session-list">
                  {scheduledSessions.map((session) => (
                    <div className="session-card" key={session.id}>
                      <div>
                        <div className="session-badge badge-scheduled"><CalendarDays size={14} />{session.status}</div>
                        <div className="session-title">{session.courseCode} - {session.courseTitle}</div>
                        <div className="session-meta">
                          Date: {formatDate(session.sessionDate)}<br />
                          Time: {formatTime(session.startTime)} - {formatTime(session.endTime)}<br />
                          Teacher: {session.teacherName || "-"}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="sessions-panel">
              <div className="sessions-header">
                <h3><Clock3 size={22} /> Open Attendance Sessions</h3>
                <span className="sessions-subtext">Attendance can only be marked for sessions that are currently open.</span>
              </div>

              {openSessions.length === 0 ? (
                <div className="empty-state">
                  <AlertTriangle size={34} style={{ marginBottom: "12px", opacity: 0.5 }} />
                  <div>No open sessions are available right now.</div>
                </div>
              ) : (
                <div className="session-list">
                  {openSessions.map((session) => (
                    <div className="session-card" key={session.id}>
                      <div>
                        <div className="session-badge badge-open"><CalendarCheck size={14} />{session.status}</div>
                        <div className="session-title">{session.courseCode} - {session.courseTitle}</div>
                        <div className="session-meta">
                          Date: {formatDate(session.sessionDate)}<br />
                          Time: {formatTime(session.startTime)} - {formatTime(session.endTime)}<br />
                          Teacher: {session.teacherName || "-"}
                        </div>
                      </div>

                      <div className="action-btn-group">
                        <button
                          className="attendance-btn"
                          onClick={() =>
                            navigate(`/student/attendance/${session.id}`, {
                              state: {
                                sessionName: session.sessionName || `${session.courseCode} - ${session.courseTitle}`,
                                courseId: session.courseId,
                              },
                            })
                          }
                        >
                          Mark Attendance
                        </button>

                        <button
                          className="engagement-btn"
                          onClick={() =>
                            navigate(`/student/engagement/${session.id}`, {
                              state: {
                                sessionName: session.sessionName || `${session.courseCode} - ${session.courseTitle}`,
                                courseId: session.courseId,
                              },
                            })
                          }
                        >
                          Open Engagement
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="outlet-holder">
              <Outlet />
            </div>
          </div>
        </main>
      </div>
    </>
  );
}

export default StudentDashboard;