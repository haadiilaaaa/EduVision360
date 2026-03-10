import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Menu from "../components/Menu";
import { getDisplayName, getUserInitial } from "../utils/auth";
import {
  LayoutDashboard,
  BookOpen,
  Brain,
  AlertTriangle,
  Users,
  ShieldAlert,
  CheckCircle2,
  CalendarDays,
  TrendingUp,
  Activity
} from "lucide-react";
import {
  getMyTeacherCourses,
  getTeacherEngagementSummary
} from "../services/academicService";
import {
  getStudentsByCourse,
  getTeacherPredictionSummary,
  getTeacherPredictionsByCourse,
  runDropoutPrediction
} from "../services/predictionService";
import {
  runProgressAnalytics,
  getTeacherCourseProgress
} from "../services/progressService";

function TeacherDashboard() {
  const navigate = useNavigate();

  const [courses, setCourses] = useState([]);
  const [students, setStudents] = useState([]);
  const [summary, setSummary] = useState({
    totalPredictions: 0,
    highRiskCount: 0,
    mediumRiskCount: 0,
    lowRiskCount: 0,
    recentPredictions: []
  });

  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [selectedStudentId, setSelectedStudentId] = useState("");
  const [windowDays, setWindowDays] = useState(30);
  const [featuresText, setFeaturesText] = useState("");
  const [coursePredictions, setCoursePredictions] = useState([]);

  const [loading, setLoading] = useState(false);
  const [showPredictionAdvanced, setShowPredictionAdvanced] = useState(false);
  const [predictionResult, setPredictionResult] = useState(null);
  const [error, setError] = useState("");

  const [engSummary, setEngSummary] = useState(null);
  const [engLoading, setEngLoading] = useState(false);

  const [progressLoading, setProgressLoading] = useState(false);
  const [progressMessage, setProgressMessage] = useState("");
  const [progressResult, setProgressResult] = useState(null);
  const [courseProgressAnalytics, setCourseProgressAnalytics] = useState([]);

  const displayName = getDisplayName();
  const initial = getUserInitial();

  const loadCourses = async () => {
    try {
      const res = await getMyTeacherCourses();
      const data = res.data || [];
      setCourses(data);

      if (data.length > 0 && !selectedCourseId) {
        setSelectedCourseId(data[0].id);
      }
    } catch (err) {
      setError("Failed to load teacher courses.");
    }
  };

  const loadStudents = async (courseId) => {
    if (!courseId) {
      setStudents([]);
      setSelectedStudentId("");
      return;
    }

    try {
      const res = await getStudentsByCourse(courseId);
      const data = res.data || [];
      setStudents(data);

      if (data.length > 0) {
        setSelectedStudentId(data[0].id);
      } else {
        setSelectedStudentId("");
      }
    } catch (err) {
      setStudents([]);
      setSelectedStudentId("");
      setError("Failed to load students for selected course.");
    }
  };

  const loadSummary = async () => {
    try {
      const res = await getTeacherPredictionSummary();
      setSummary(
        res.data || {
          totalPredictions: 0,
          highRiskCount: 0,
          mediumRiskCount: 0,
          lowRiskCount: 0,
          recentPredictions: []
        }
      );
    } catch (err) {
      console.log("Failed to load teacher prediction summary", err);
    }
  };

  const loadCoursePredictions = async (courseId) => {
    if (!courseId) {
      setCoursePredictions([]);
      return;
    }

    try {
      const res = await getTeacherPredictionsByCourse(courseId);
      setCoursePredictions(res.data || []);
    } catch (err) {
      console.log("Failed to load course predictions", err);
      setCoursePredictions([]);
    }
  };

  const loadCourseProgressAnalytics = async (courseId) => {
    if (!courseId) {
      setCourseProgressAnalytics([]);
      return;
    }

    try {
      const res = await getTeacherCourseProgress(courseId);
      setCourseProgressAnalytics(res.data || []);
    } catch (err) {
      console.log("Failed to load teacher course progress analytics", err);
      setCourseProgressAnalytics([]);
    }
  };

  const loadEngagementSummary = async (courseId) => {
    if (!courseId) {
      setEngSummary(null);
      return;
    }

    try {
      setEngLoading(true);
      const res = await getTeacherEngagementSummary(courseId);
      setEngSummary(res.data || null);
    } catch (err) {
      console.log("Failed to load engagement summary", err);
      setEngSummary(null);
    } finally {
      setEngLoading(false);
    }
  };

  useEffect(() => {
    loadCourses();
    loadSummary();
    // eslint-disable-next-line
  }, []);

  useEffect(() => {
    if (selectedCourseId) {
      loadStudents(selectedCourseId);
      loadCoursePredictions(selectedCourseId);
      loadEngagementSummary(selectedCourseId);
      loadCourseProgressAnalytics(selectedCourseId);
    }
    // eslint-disable-next-line
  }, [selectedCourseId]);

  const handlePredict = async () => {
    setLoading(true);
    setError("");
    setPredictionResult(null);

    try {
      if (!selectedCourseId) {
        throw new Error("Please select a course.");
      }

      if (!selectedStudentId) {
        throw new Error("Please select a student.");
      }

      let parsedOverrides = {};
      if (featuresText.trim()) {
        try {
          parsedOverrides = JSON.parse(featuresText);
        } catch {
          throw new Error("Optional Feature Overrides JSON is not valid.");
        }
      }

      const res = await runDropoutPrediction({
        studentId: selectedStudentId,
        courseId: selectedCourseId,
        windowDays,
        featureOverrides: parsedOverrides
      });

      setPredictionResult(res.data);
      await loadSummary();
      await loadCoursePredictions(selectedCourseId);
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          err?.response?.data?.detail ||
          err.message ||
          "Prediction failed."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleRunProgressAnalytics = async () => {
    setProgressLoading(true);
    setProgressMessage("");
    setProgressResult(null);

    try {
      if (!selectedCourseId) {
        throw new Error("Please select a course.");
      }

      if (!selectedStudentId) {
        throw new Error("Please select a student.");
      }

      const res = await runProgressAnalytics({
        studentId: selectedStudentId,
        courseId: selectedCourseId,
        windowDays
      });

      setProgressResult(res.data);
      setProgressMessage("Progress analytics generated successfully.");
      await loadCourseProgressAnalytics(selectedCourseId);
    } catch (err) {
      setProgressMessage(
        err?.response?.data?.message ||
          err?.message ||
          "Failed to generate progress analytics."
      );
    } finally {
      setProgressLoading(false);
    }
  };

  const getRiskClass = (riskLevel) => {
    if (riskLevel === "HIGH") return "risk-high";
    if (riskLevel === "MEDIUM") return "risk-medium";
    return "risk-low";
  };

  const formatTimestamp = (value) => {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString();
};

const getProbabilityPercent = (value) => {
  if (value == null || Number.isNaN(Number(value))) return 0;
  return Math.max(0, Math.min(100, Math.round(Number(value) * 100)));
};

const getRiskMessage = (riskLevel) => {
  if (riskLevel === "HIGH") {
    return "Immediate teacher follow-up is recommended based on the latest behavioral learning evidence.";
  }
  if (riskLevel === "MEDIUM") {
    return "Student activity should be monitored and learning consistency should be reinforced.";
  }
  return "Current behavioral evidence suggests a relatively stable learning pattern.";
};

  const getProgressStatusClass = (status) => {
    if (status === "AT_RISK") return "risk-high";
    if (status === "NEEDS_ATTENTION") return "risk-medium";
    return "risk-low";
  };

  const riskChartData = [
    { label: "High", value: summary.highRiskCount, fillClass: "fill-high" },
    { label: "Medium", value: summary.mediumRiskCount, fillClass: "fill-medium" },
    { label: "Low", value: summary.lowRiskCount, fillClass: "fill-low" }
  ];

  const maxRiskValue = Math.max(
    1,
    summary.highRiskCount,
    summary.mediumRiskCount,
    summary.lowRiskCount
  );

  return (
    <div className="teacher-layout">
      <style>{`
  :root {
    --page-bg: var(--page-bg, radial-gradient(circle at top right, rgba(56,189,248,0.10), transparent 45%),
                          radial-gradient(circle at bottom left, rgba(99,102,241,0.10), transparent 45%),
                          #0b1220);
    --card-bg: var(--card-bg, rgba(255,255,255,0.04));
    --border-color: var(--border-color, rgba(255,255,255,0.10));
    --text-primary: var(--text-primary, rgba(255,255,255,0.92));
    --text-secondary: var(--text-secondary, rgba(255,255,255,0.65));
    --accent: var(--accent, #60a5fa);
    --card-shadow: var(--card-shadow, 0 18px 45px rgba(0,0,0,0.28));
    --input-bg: var(--input-bg, rgba(255,255,255,0.03));
    --button-secondary-bg: var(--button-secondary-bg, rgba(255,255,255,0.06));
    --button-secondary-border: var(--button-secondary-border, rgba(255,255,255,0.12));
    --button-secondary-text: var(--button-secondary-text, rgba(255,255,255,0.9));
  }

  .teacher-layout {
    display: flex;
    min-height: 100vh;
    background: var(--page-bg);
    font-family: 'Inter', sans-serif;
  }

  .main-content {
    flex: 1;
    padding: 34px 34px 60px;
    color: var(--text-primary);
    max-width: 1180px;
    margin: 0 auto;
  }

  .hero {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 22px;
    padding-bottom: 18px;
    border-bottom: 1px solid var(--border-color);
  }

  .hero h2 {
    margin: 0 0 8px 0;
    font-size: 34px;
    letter-spacing: -0.02em;
    color: var(--text-primary);
  }

  .hero p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.65;
    font-size: 14.5px;
  }

  .hero-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .avatar {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    display: grid;
    place-items: center;
    font-weight: 900;
    color: white;
    background: linear-gradient(135deg, rgba(96,165,250,0.9), rgba(99,102,241,0.9));
    box-shadow: 0 14px 30px rgba(0,0,0,0.25);
    border: 1px solid rgba(255,255,255,0.18);
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 14px;
    margin-bottom: 18px;
  }

  .summary-card {
    grid-column: span 3;
    background: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: 20px;
    padding: 18px 18px 16px;
    box-shadow: var(--card-shadow);
    position: relative;
    overflow: hidden;
    transition: transform 160ms ease, border-color 160ms ease, background 160ms ease;
  }

  .summary-card::before {
    content: "";
    position: absolute;
    inset: -60px -60px auto auto;
    width: 160px;
    height: 160px;
    background: radial-gradient(circle, rgba(96,165,250,0.18), transparent 60%);
    transform: rotate(14deg);
    pointer-events: none;
  }

  .summary-card:hover {
    transform: translateY(-2px);
    border-color: rgba(96,165,250,0.25);
    background: rgba(255,255,255,0.05);
  }

  .summary-card h4 {
    margin: 10px 0 6px;
    color: var(--text-secondary);
    font-size: 12.5px;
    letter-spacing: 0.02em;
    text-transform: uppercase;
  }

  .summary-value {
    font-size: 30px;
    font-weight: 900;
    letter-spacing: -0.02em;
    color: var(--text-primary);
  }

  .card-grid {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 14px;
    margin-top: 18px;
  }

  .card {
    grid-column: span 4;
    background: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: 22px;
    padding: 20px;
    box-shadow: var(--card-shadow);
    transition: transform 160ms ease, border-color 160ms ease, background 160ms ease;
  }

  .card:hover {
    transform: translateY(-2px);
    border-color: rgba(96,165,250,0.25);
    background: rgba(255,255,255,0.05);
  }

  .card.full-width-card {
    grid-column: 1 / -1;
  }

  .card h3 {
    margin: 10px 0 10px 0;
    font-size: 18px;
    letter-spacing: -0.01em;
    color: var(--text-primary);
  }

  .card p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.65;
    font-size: 14.5px;
  }

  .action-btn {
    margin-top: 14px;
    background: var(--button-secondary-bg);
    border: 1px solid var(--button-secondary-border);
    color: var(--button-secondary-text);
    padding: 11px 14px;
    border-radius: 14px;
    cursor: pointer;
    font-weight: 800;
    transition: transform 120ms ease, opacity 120ms ease, border-color 120ms ease;
  }

  .action-btn:hover {
    opacity: 0.95;
    transform: translateY(-1px);
    border-color: rgba(96,165,250,0.28);
  }

  .action-btn:active {
    transform: translateY(0px);
  }

  .predict-btn {
    margin-top: 18px;
    background: linear-gradient(135deg, rgba(96,165,250,1), rgba(99,102,241,1));
    color: white;
    border: 1px solid rgba(255,255,255,0.14);
    padding: 13px 18px;
    border-radius: 14px;
    font-weight: 900;
    cursor: pointer;
    box-shadow: 0 16px 32px rgba(0,0,0,0.25);
    transition: transform 120ms ease, opacity 120ms ease;
  }

  .predict-btn:hover {
    transform: translateY(-1px);
    opacity: 0.98;
  }

  .predict-btn:disabled {
    opacity: 0.65;
    cursor: not-allowed;
    transform: none;
  }

  .form-grid {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 12px;
    margin-top: 16px;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .form-grid .form-group {
    grid-column: span 4;
  }

  .form-group label {
    font-weight: 800;
    font-size: 13px;
    color: var(--text-secondary);
    letter-spacing: 0.01em;
  }

  .form-group select,
  .form-group textarea {
    width: 100%;
    padding: 12px 14px;
    border-radius: 14px;
    border: 1px solid var(--border-color);
    background: var(--input-bg);
    color: var(--text-primary);
    outline: none;
    font-size: 14px;
    box-sizing: border-box;
    transition: border-color 120ms ease, box-shadow 120ms ease, background 120ms ease;
  }

  .form-group select:focus,
  .form-group textarea:focus {
    border-color: rgba(96,165,250,0.45);
    box-shadow: 0 0 0 4px rgba(96,165,250,0.10);
    background: rgba(255,255,255,0.04);
  }

  .form-group textarea {
    min-height: 170px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
    resize: vertical;
  }

  .message-box {
    margin-top: 16px;
    padding: 14px;
    border-radius: 14px;
    font-weight: 600;
    line-height: 1.55;
  }

  .error-box {
    background: rgba(239, 68, 68, 0.10);
    border: 1px solid rgba(239, 68, 68, 0.35);
    color: #fecaca;
  }

  .result-box {
    background: rgba(34, 197, 94, 0.10);
    border: 1px solid rgba(34, 197, 94, 0.25);
    color: var(--text-primary);
  }

  .info-box {
    margin-top: 14px;
    padding: 12px 14px;
    border-radius: 14px;
    background: rgba(96,165,250,0.10);
    border: 1px solid rgba(96,165,250,0.22);
    color: var(--text-primary);
    font-size: 14px;
  }

  .recent-list {
    margin-top: 14px;
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    gap: 12px;
  }

  .recent-item {
    grid-column: span 6;
    border: 1px solid var(--border-color);
    border-radius: 18px;
    padding: 14px;
    background: rgba(255,255,255,0.03);
    transition: transform 140ms ease, border-color 140ms ease, background 140ms ease;
  }

  .recent-item:hover {
    transform: translateY(-1px);
    border-color: rgba(96,165,250,0.22);
    background: rgba(255,255,255,0.05);
  }

  .recent-top {
    display: flex;
    justify-content: space-between;
    gap: 14px;
    flex-wrap: wrap;
    margin-bottom: 8px;
  }

  .muted {
    color: var(--text-secondary);
    font-size: 13.5px;
  }

  .risk-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    margin-top: 6px;
    padding: 7px 12px;
    border-radius: 999px;
    font-size: 12.5px;
    font-weight: 900;
    letter-spacing: 0.01em;
    border: 1px solid transparent;
    white-space: nowrap;
  }

  .risk-high {
    background: rgba(239, 68, 68, 0.14);
    color: #fecaca;
    border-color: rgba(239, 68, 68, 0.35);
  }

  .risk-medium {
    background: rgba(245, 158, 11, 0.14);
    color: #fde68a;
    border-color: rgba(245, 158, 11, 0.35);
  }

  .risk-low {
    background: rgba(34, 197, 94, 0.14);
    color: #bbf7d0;
    border-color: rgba(34, 197, 94, 0.28);
  }

  .chart-box {
    margin-top: 18px;
    padding: 16px;
    border: 1px solid var(--border-color);
    border-radius: 18px;
    background: rgba(255,255,255,0.03);
  }

  .chart-row {
    margin-bottom: 14px;
  }

  .chart-label {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
    font-size: 13.5px;
    color: var(--text-secondary);
  }

  .chart-bar-bg {
    width: 100%;
    height: 12px;
    border-radius: 999px;
    background: rgba(255,255,255,0.08);
    overflow: hidden;
  }

  .chart-bar-fill {
    height: 100%;
    border-radius: 999px;
    transition: width 240ms ease;
  }

  .fill-high { background: rgba(239, 68, 68, 0.85); }
  .fill-medium { background: rgba(245, 158, 11, 0.85); }
  .fill-low { background: rgba(34, 197, 94, 0.85); }

  @media (max-width: 1100px) {
    .summary-card { grid-column: span 6; }
    .card { grid-column: span 6; }
    .recent-item { grid-column: span 12; }
    .form-grid .form-group { grid-column: span 6; }
  }

  @media (max-width: 720px) {
    .main-content { padding: 22px 16px 50px; }
    .summary-card { grid-column: span 12; }
    .card { grid-column: span 12; }
    .form-grid .form-group { grid-column: span 12; }
    .hero { flex-direction: column; align-items: flex-start; }
  }

  .prediction-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.advanced-toggle {
  background: rgba(255,255,255,0.06);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 700;
  transition: 0.2s ease;
}

.advanced-toggle:hover {
  border-color: rgba(96,165,250,0.28);
  transform: translateY(-1px);
}

.advanced-panel {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  border: 1px dashed rgba(255,255,255,0.14);
  background: rgba(255,255,255,0.03);
}

.prediction-result-card {
  margin-top: 18px;
  border: 1px solid rgba(255,255,255,0.10);
  border-radius: 20px;
  padding: 18px;
  background: rgba(255,255,255,0.04);
  box-shadow: var(--card-shadow);
}

.prediction-result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.prediction-result-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
}

.prediction-subtitle {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 13.5px;
}

.probability-panel {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255,255,255,0.03);
  border: 1px solid var(--border-color);
}

.probability-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 10px;
  flex-wrap: wrap;
}

.probability-value {
  font-size: 24px;
  font-weight: 900;
  color: var(--text-primary);
}

.probability-bar {
  width: 100%;
  height: 14px;
  border-radius: 999px;
  background: rgba(255,255,255,0.08);
  overflow: hidden;
}

.probability-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.3s ease;
}

.probability-fill.risk-high {
  background: rgba(239, 68, 68, 0.9);
}

.probability-fill.risk-medium {
  background: rgba(245, 158, 11, 0.9);
}

.probability-fill.risk-low {
  background: rgba(34, 197, 94, 0.9);
}

.metric-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 12px;
}

.metric-card {
  grid-column: span 4;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255,255,255,0.03);
  border: 1px solid var(--border-color);
}

.metric-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.metric-value {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-primary);
  word-break: break-word;
}

.prediction-interpretation {
  margin-top: 16px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(96,165,250,0.08);
  border: 1px solid rgba(96,165,250,0.20);
  color: var(--text-primary);
  line-height: 1.6;
}

@media (max-width: 1100px) {
  .metric-card {
    grid-column: span 6;
  }
}

@media (max-width: 720px) {
  .metric-card {
    grid-column: span 12;
  }
}
`}</style>
      <Menu />

      <main className="main-content">
        <div className="hero">
          <div>
            <h2>Teacher Dashboard</h2>
            <p>
              Welcome, <strong>{displayName}</strong>. Manage your assigned courses, class sessions,
              and student learning support from here.
            </p>
          </div>

          <div className="hero-right">
            <div className="avatar">{initial}</div>
          </div>
        </div>

        <div className="summary-grid">
          <div className="summary-card">
            <Users size={22} color="var(--accent)" />
            <h4>Total Predictions</h4>
            <div className="summary-value">{summary.totalPredictions}</div>
          </div>

          <div className="summary-card">
            <ShieldAlert size={22} color="#dc2626" />
            <h4>High Risk</h4>
            <div className="summary-value">{summary.highRiskCount}</div>
          </div>

          <div className="summary-card">
            <AlertTriangle size={22} color="#d97706" />
            <h4>Medium Risk</h4>
            <div className="summary-value">{summary.mediumRiskCount}</div>
          </div>

          <div className="summary-card">
            <CheckCircle2 size={22} color="#16a34a" />
            <h4>Low Risk</h4>
            <div className="summary-value">{summary.lowRiskCount}</div>
          </div>
        </div>

        <div className="card-grid">
          <div className="card">
            <LayoutDashboard size={22} color="var(--accent)" />
            <h3>Overview</h3>
            <p>
              Access your teacher workspace and prepare for course, attendance,
              session, and AI-supported academic monitoring.
            </p>
          </div>

          <div className="card">
            <BookOpen size={22} color="var(--accent)" />
            <h3>My Courses</h3>
            <p>
              See the courses assigned to you by the admin and use them as the
              base for teacher features.
            </p>
            <button
              className="action-btn"
              onClick={() => navigate("/teacher/courses")}
            >
              Open My Courses
            </button>
          </div>

          <div className="card">
            <CalendarDays size={22} color="var(--accent)" />
            <h3>Class Sessions</h3>
            <p>
              Create class sessions, open attendance at the right time, and
              complete sessions when teaching is finished.
            </p>
            <button
              className="action-btn"
              onClick={() => navigate("/teacher/sessions")}
            >
              Manage Sessions
            </button>
          </div>

          <div className="card full-width-card">
  <Brain size={24} color="var(--accent)" />
  <h3>Student Dropout Prediction</h3>
  <p>
    Generate a live XGBoost-based dropout risk analysis using recent attendance,
    login recency, material interaction, and AI-supported learning activity.
  </p>

  <div className="form-grid">
    <div className="form-group">
      <label>Course</label>
      <select
        value={selectedCourseId}
        onChange={(e) => setSelectedCourseId(e.target.value)}
      >
        <option value="">Select course</option>
        {courses.map((course) => (
          <option key={course.id} value={course.id}>
            {course.courseCode} - {course.title}
          </option>
        ))}
      </select>
    </div>

    <div className="form-group">
      <label>Student</label>
      <select
        value={selectedStudentId}
        onChange={(e) => setSelectedStudentId(e.target.value)}
      >
        <option value="">Select student</option>
        {students.map((student) => (
          <option key={student.id} value={student.id}>
            {student.fullName} {student.studentCode ? `(${student.studentCode})` : ""}
          </option>
        ))}
      </select>
    </div>

    <div className="form-group">
      <label>Window</label>
      <select
        value={windowDays}
        onChange={(e) => setWindowDays(Number(e.target.value))}
      >
        <option value={7}>Last 7 days</option>
        <option value={14}>Last 14 days</option>
        <option value={30}>Last 30 days</option>
      </select>
    </div>
  </div>

  <div className="prediction-toolbar">
    <button className="predict-btn" onClick={handlePredict} disabled={loading}>
      {loading ? "Predicting..." : "Run & Save Dropout Prediction"}
    </button>

    <button
      type="button"
      className="advanced-toggle"
      onClick={() => setShowPredictionAdvanced((prev) => !prev)}
    >
      {showPredictionAdvanced ? "Hide Advanced Options" : "Show Advanced Options"}
    </button>
  </div>

  {showPredictionAdvanced && (
    <div className="advanced-panel">
      <div className="form-group">
        <label>Optional Feature Overrides JSON</label>
        <textarea
          value={featuresText}
          onChange={(e) => setFeaturesText(e.target.value)}
          placeholder={`Example:
{
  "days_since_last_login": 10,
  "material_views_count_window": 2
}`}
        />
      </div>
    </div>
  )}

  {error && <div className="message-box error-box">{error}</div>}

  {predictionResult && (
    <div className="prediction-result-card">
      <div className="prediction-result-header">
        <div>
          <h4 className="prediction-result-title">Latest Prediction Result</h4>
          <div className="prediction-subtitle">
            {predictionResult.studentName} • {predictionResult.courseCode} - {predictionResult.courseTitle}
          </div>
        </div>

        <span className={`risk-badge ${getRiskClass(predictionResult.riskLevel)}`}>
          {predictionResult.riskLevel} RISK
        </span>
      </div>

      <div className="probability-panel">
        <div className="probability-top">
          <div className="muted">Dropout Probability</div>
          <div className="probability-value">
            {getProbabilityPercent(predictionResult.dropoutProbability)}%
          </div>
        </div>

        <div className="probability-bar">
          <div
            className={`probability-fill ${getRiskClass(predictionResult.riskLevel)}`}
            style={{ width: `${getProbabilityPercent(predictionResult.dropoutProbability)}%` }}
          />
        </div>
      </div>

      <div className="metric-grid">
        <div className="metric-card">
          <div className="metric-label">Predicted Label</div>
          <div className="metric-value">{predictionResult.predictedLabel || "-"}</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Threshold</div>
          <div className="metric-value">{predictionResult.threshold ?? "-"}</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Generated At</div>
          <div className="metric-value">{formatTimestamp(predictionResult.predictedAt)}</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Model</div>
          <div className="metric-value">{predictionResult.modelName || "XGBoost"}</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Scoring Mode</div>
          <div className="metric-value">{predictionResult.scoringMode || "-"}</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Student Email</div>
          <div className="metric-value">{predictionResult.studentEmail || "-"}</div>
        </div>
      </div>

      <div className="prediction-interpretation">
        <strong>Interpretation:</strong> {getRiskMessage(predictionResult.riskLevel)}
      </div>
    </div>
  )}

  <div className="chart-box">
    <h4 style={{ marginTop: 0, marginBottom: "14px" }}>Risk Distribution</h4>
    {riskChartData.map((item) => (
      <div className="chart-row" key={item.label}>
        <div className="chart-label">
          <span>{item.label}</span>
          <span>{item.value}</span>
        </div>
        <div className="chart-bar-bg">
          <div
            className={`chart-bar-fill ${item.fillClass}`}
            style={{
              width: `${(item.value / maxRiskValue) * 100}%`
            }}
          />
        </div>
      </div>
    ))}
  </div>
</div>

          <div className="card full-width-card">
            <TrendingUp size={24} color="var(--accent)" />
            <h3>Student Progress Analytics</h3>
            <p>
              Generate an interpretable progress profile using attendance,
              learning-material interaction, AI learning support usage,
              engagement evidence, and dropout-risk context.
            </p>

            <div className="info-box">
              This is an indicator-based analytics module, not a second ML model.
              It is designed to support intervention decisions in a realistic and
              explainable way.
            </div>

            <div className="form-grid">
              <div className="form-group">
                <label>Course</label>
                <select
                  value={selectedCourseId}
                  onChange={(e) => setSelectedCourseId(e.target.value)}
                >
                  <option value="">Select course</option>
                  {courses.map((course) => (
                    <option key={course.id} value={course.id}>
                      {course.courseCode} - {course.title}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Student</label>
                <select
                  value={selectedStudentId}
                  onChange={(e) => setSelectedStudentId(e.target.value)}
                >
                  <option value="">Select student</option>
                  {students.map((student) => (
                    <option key={student.id} value={student.id}>
                      {student.fullName}{" "}
                      {student.studentCode ? `(${student.studentCode})` : ""}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Window</label>
                <select
                  value={windowDays}
                  onChange={(e) => setWindowDays(Number(e.target.value))}
                >
                  <option value={7}>Last 7 days</option>
                  <option value={14}>Last 14 days</option>
                  <option value={30}>Last 30 days</option>
                </select>
              </div>
            </div>

            <button
              className="predict-btn"
              onClick={handleRunProgressAnalytics}
              disabled={progressLoading}
            >
              {progressLoading ? "Generating..." : "Run Progress Analytics"}
            </button>

            {progressMessage && (
              <div
                className={`message-box ${
                  progressResult ? "result-box" : "error-box"
                }`}
              >
                {progressMessage}
              </div>
            )}

            {progressResult && (
              <div className="message-box result-box">
                <h4 style={{ marginTop: 0, marginBottom: "12px" }}>
                  Latest Progress Result
                </h4>

                <p>
                  <strong>Student:</strong> {progressResult.studentName}
                </p>
                <p>
                  <strong>Course:</strong> {progressResult.courseCode} -{" "}
                  {progressResult.courseTitle}
                </p>
                <p>
                  <strong>Progress Score:</strong> {progressResult.progressScore}
                </p>
                <p>
                  <strong>Trend:</strong> {progressResult.trend}
                </p>
                <p>
                  <strong>Suggestion:</strong>{" "}
                  {progressResult.interventionSuggestion}
                </p>

                <div style={{ display: "flex", gap: "10px", flexWrap: "wrap", marginTop: "12px" }}>
                  <span
                    className={`risk-badge ${getProgressStatusClass(
                      progressResult.status
                    )}`}
                  >
                    Status: {progressResult.status}
                  </span>

                  {progressResult.dropoutRiskLevel && (
                    <span className={`risk-badge ${getRiskClass(progressResult.dropoutRiskLevel)}`}>
                      Dropout Risk: {progressResult.dropoutRiskLevel}
                    </span>
                  )}
                </div>

                <div className="recent-list" style={{ marginTop: "16px" }}>
                  <div className="recent-item">
                    <strong>Signals</strong>
                    <div className="muted" style={{ marginTop: "8px" }}>
                      Attendance Logs: {progressResult.attendancePresentCountWindow}
                      <br />
                      Material Views: {progressResult.materialViewsCountWindow}
                      <br />
                      AI Usage Count: {progressResult.aiTotalCountWindow}
                      <br />
                      Days Since Login: {progressResult.daysSinceLastLogin}
                      <br />
                      Days Since Attendance: {progressResult.daysSinceLastAttendance}
                      <br />
                      Avg Engagement Score:{" "}
                      {progressResult.averageEngagementScore != null
                        ? Number(progressResult.averageEngagementScore).toFixed(2)
                        : "N/A"}
                    </div>
                  </div>

                  <div className="recent-item">
                    <strong>Reasons</strong>
                    <div className="muted" style={{ marginTop: "8px" }}>
                      {progressResult.reasons?.length ? (
                        progressResult.reasons.map((reason, index) => (
                          <div key={index}>• {reason}</div>
                        ))
                      ) : (
                        <div>No reasons available.</div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="card full-width-card">
            <h3>Engagement & Emotion Analytics</h3>

            {!selectedCourseId ? (
              <p>Select a course to view engagement analytics.</p>
            ) : engLoading ? (
              <p>Loading engagement analytics...</p>
            ) : !engSummary ? (
              <p>No engagement logs found for this course yet.</p>
            ) : (
              <>
                <div className="summary-grid" style={{ marginTop: 14 }}>
                  <div className="summary-card">
                    <Users size={22} color="var(--accent)" />
                    <h4>Total Logs</h4>
                    <div className="summary-value">{engSummary.totalLogs}</div>
                  </div>

                  <div className="summary-card">
                    <CheckCircle2 size={22} color="#16a34a" />
                    <h4>Attentive</h4>
                    <div className="summary-value">{engSummary.attentiveCount}</div>
                  </div>

                  <div className="summary-card">
                    <AlertTriangle size={22} color="#d97706" />
                    <h4>Neutral</h4>
                    <div className="summary-value">{engSummary.neutralCount}</div>
                  </div>

                  <div className="summary-card">
                    <ShieldAlert size={22} color="#dc2626" />
                    <h4>Distracted</h4>
                    <div className="summary-value">{engSummary.distractedCount}</div>
                  </div>
                </div>

                <div className="info-box">
                  Average Engagement Score: <strong>{Number(engSummary.averageScore || 0).toFixed(2)}</strong>
                </div>

                <h4 style={{ marginTop: 18 }}>Recent Engagement Logs</h4>

                {engSummary.recentLogs?.length === 0 ? (
                  <p>No recent logs.</p>
                ) : (
                  <div className="recent-list">
                    {engSummary.recentLogs.map((item, index) => (
                      <div className="recent-item" key={`${item.studentId}-${index}`}>
                        <div className="recent-top">
                          <div>
                            <strong>{item.studentName || "Student"}</strong>
                            <div className="muted">{item.studentEmail || ""}</div>
                          </div>

                          <span
                            className="risk-badge"
                            style={{
                              background: "rgba(96,165,250,0.12)",
                              border: "1px solid rgba(96,165,250,0.28)",
                              color: "#60a5fa"
                            }}
                          >
                            {item.label} ({item.engagementScore})
                          </span>
                        </div>

                        <div className="muted">
                          Emotion: <strong>{item.dominantEmotion || "N/A"}</strong>{" "}
                          {item.emotionConfidence != null
                            ? `(conf ${Number(item.emotionConfidence).toFixed(2)})`
                            : ""}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>

          <div className="card full-width-card">
            <h3>Recent Predictions</h3>

            {summary.recentPredictions?.length === 0 ? (
              <p>No saved predictions yet.</p>
            ) : (
              <div className="recent-list">
                {summary.recentPredictions.map((item, index) => (
                  <div className="recent-item" key={`${item.studentId}-${item.courseId}-${index}`}>
                    <div className="recent-top">
                      <div>
                        <strong>{item.studentName}</strong>
                        <div className="muted">{item.studentEmail}</div>
                      </div>
                      <span className={`risk-badge ${getRiskClass(item.riskLevel)}`}>
                        {item.riskLevel}
                      </span>
                    </div>

                    <div className="muted">
                      {item.courseCode} - {item.courseTitle}
                    </div>

                    <div className="muted">
                      Probability: {item.dropoutProbability} | Label: {item.predictedLabel}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="card full-width-card">
            <h3>Selected Course Risk List</h3>

            {coursePredictions.length === 0 ? (
              <p>No saved predictions for this course yet.</p>
            ) : (
              <div className="recent-list">
                {coursePredictions.map((item, index) => (
                  <div className="recent-item" key={`${item.studentId}-${item.courseId}-${index}`}>
                    <div className="recent-top">
                      <div>
                        <strong>{item.studentName}</strong>
                        <div className="muted">{item.studentEmail}</div>
                      </div>
                      <span className={`risk-badge ${getRiskClass(item.riskLevel)}`}>
                        {item.riskLevel}
                      </span>
                    </div>

                    <div className="muted">
                      {item.courseCode} - {item.courseTitle}
                    </div>

                    <div className="muted">
                      Probability: {item.dropoutProbability} | Label: {item.predictedLabel}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="card full-width-card">
            <Activity size={24} color="var(--accent)" />
            <h3>Selected Course Progress Analytics</h3>

            {courseProgressAnalytics.length === 0 ? (
              <p>No saved progress analytics for this course yet.</p>
            ) : (
              <div className="recent-list">
                {courseProgressAnalytics.map((item, index) => (
                  <div
                    className="recent-item"
                    key={`${item.studentId}-${item.courseId}-${index}`}
                  >
                    <div className="recent-top">
                      <div>
                        <strong>{item.studentName}</strong>
                        <div className="muted">{item.studentEmail}</div>
                      </div>

                      <span
                        className={`risk-badge ${getProgressStatusClass(
                          item.status
                        )}`}
                      >
                        {item.status}
                      </span>
                    </div>

                    <div className="muted">
                      {item.courseCode} - {item.courseTitle}
                    </div>

                    <div className="muted">
                      Progress Score: {item.progressScore} | Trend: {item.trend}
                    </div>

                    <div className="muted">
                      Suggestion: {item.interventionSuggestion}
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

export default TeacherDashboard;