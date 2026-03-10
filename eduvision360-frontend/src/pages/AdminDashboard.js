import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import Menu from "../components/Menu";
import { getUserInitial } from "../utils/auth";
import {
  CheckCircle,
  ShieldCheck,
  UserPlus,
  Bell,
  LayoutDashboard,
  ShieldAlert,
  Building2,
  BookOpen,
  PlusCircle,
  Trash2,
  Pencil,
  Layers3,
  Brain,
  Users,
  AlertTriangle,
  CheckCircle2,
  BarChart3,
  Settings
} from "lucide-react";
import {
  getAdminPredictionSummary,
  getStudentsByCourse,
  getAllAdminPredictions,
  runDropoutPrediction
} from "../services/predictionService";
import {
  getAdminEngagementSummary,
  getInstitutionSettings,
  updateInstitutionSettings
} from "../services/academicService";

function AdminDashboard() {
  const token = localStorage.getItem("token");
  const initial = getUserInitial();

  const [activeTab, setActiveTab] = useState("OVERVIEW");

  const [teachers, setTeachers] = useState([]);
  const [availableTeachers, setAvailableTeachers] = useState([]);

  const [departments, setDepartments] = useState([]);
  const [deptCode, setDeptCode] = useState("");
  const [deptName, setDeptName] = useState("");
  const [deptDescription, setDeptDescription] = useState("");
  const [message, setMessage] = useState("");
  const [editingDeptId, setEditingDeptId] = useState(null);

  const [courses, setCourses] = useState([]);
  const [courseTitle, setCourseTitle] = useState("");
  const [courseCode, setCourseCode] = useState("");
  const [courseDescription, setCourseDescription] = useState("");
  const [selectedDeptId, setSelectedDeptId] = useState("");
  const [selectedTeacherId, setSelectedTeacherId] = useState("");
  const [creditValue, setCreditValue] = useState("");
  const [semester, setSemester] = useState("");
  const [academicYear, setAcademicYear] = useState("");
  const [courseStatus, setCourseStatus] = useState("ACTIVE");
  const [courseMessage, setCourseMessage] = useState("");
  const [editingCourseId, setEditingCourseId] = useState(null);

  const [dashboardLoading, setDashboardLoading] = useState(false);
  const [dashboardSummary, setDashboardSummary] = useState({
    overview: {
      totalStudents: 0,
      totalTeachers: 0,
      totalAdmins: 0,
      pendingTeachers: 0,
      totalDepartments: 0,
      totalCourses: 0,
      totalSessions: 0,
      totalQuizzes: 0,
      totalAnnouncements: 0,
      totalMaterials: 0,
      totalMessages: 0,
      highRiskCount: 0,
      mediumRiskCount: 0,
      lowRiskCount: 0
    },
    charts: {
      usersByRole: [],
      contentActivity: [],
      riskDistribution: [],
      sessionStatusDistribution: [],
      coursesByDepartment: []
    },
    recentActivities: []
  });

  const [predictionSummary, setPredictionSummary] = useState({
    totalPredictions: 0,
    highRiskCount: 0,
    mediumRiskCount: 0,
    lowRiskCount: 0,
    recentPredictions: []
  });

  const [adminEngagementSummary, setAdminEngagementSummary] = useState({
    totalLogs: 0,
    attentiveCount: 0,
    neutralCount: 0,
    distractedCount: 0,
    averageScore: 0,
    recentLogs: []
  });

  const [predictionCourseId, setPredictionCourseId] = useState("");
  const [predictionStudents, setPredictionStudents] = useState([]);
  const [predictionStudentId, setPredictionStudentId] = useState("");
  const [predictionWindowDays, setPredictionWindowDays] = useState(30);
  const [predictionFeaturesText, setPredictionFeaturesText] = useState("");
  const [predictionMessage, setPredictionMessage] = useState("");
  const [predictionResult, setPredictionResult] = useState(null);
  const [predictionLoading, setPredictionLoading] = useState(false);
  const [allPredictions, setAllPredictions] = useState([]);
  const [adminEngagementLoading, setAdminEngagementLoading] = useState(false);
  const [showPredictionAdvanced, setShowPredictionAdvanced] = useState(false);

  const [institutionSettings, setInstitutionSettings] = useState({
    institutionName: "",
    institutionCode: "",
    contactEmail: "",
    contactNumber: "",
    supportEmail: "",
    address: "",
    websiteUrl: "",
    logoUrl: "",
    academicYear: "",
    currentSemester: "",
    description: "",
    timezone: "Asia/Colombo",
    updatedAt: null,
    updatedByName: ""
  });
  const [institutionLoading, setInstitutionLoading] = useState(false);
  const [institutionMessage, setInstitutionMessage] = useState("");

  const emptyDashboardSummary = {
    overview: {
      totalStudents: 0,
      totalTeachers: 0,
      totalAdmins: 0,
      pendingTeachers: 0,
      totalDepartments: 0,
      totalCourses: 0,
      totalSessions: 0,
      totalQuizzes: 0,
      totalAnnouncements: 0,
      totalMaterials: 0,
      totalMessages: 0,
      highRiskCount: 0,
      mediumRiskCount: 0,
      lowRiskCount: 0
    },
    charts: {
      usersByRole: [],
      contentActivity: [],
      riskDistribution: [],
      sessionStatusDistribution: [],
      coursesByDepartment: []
    },
    recentActivities: []
  };

  const emptyInstitutionSettings = {
    institutionName: "",
    institutionCode: "",
    contactEmail: "",
    contactNumber: "",
    supportEmail: "",
    address: "",
    websiteUrl: "",
    logoUrl: "",
    academicYear: "",
    currentSemester: "",
    description: "",
    timezone: "Asia/Colombo",
    updatedAt: null,
    updatedByName: ""
  };

  const axiosAuth = axios.create({
    baseURL: "http://localhost:8080",
    headers: { Authorization: `Bearer ${token}` }
  });

  const loadAdminDashboardSummary = async () => {
    try {
      setDashboardLoading(true);
      const res = await axiosAuth.get("/api/admin/dashboard/summary");
      setDashboardSummary(res.data || emptyDashboardSummary);
    } catch (err) {
      console.log(
        "Failed to load admin dashboard summary",
        err?.response?.status,
        err?.response?.data
      );
      setDashboardSummary(emptyDashboardSummary);
    } finally {
      setDashboardLoading(false);
    }
  };

  const loadPendingTeachers = async () => {
    try {
      const res = await axiosAuth.get("/api/admin/users/pending-teachers");
      setTeachers(res.data || []);
    } catch (err) {
      console.log(
        "Failed to load pending teachers",
        err?.response?.status,
        err?.response?.data
      );
      setTeachers([]);
    }
  };

  const loadActiveTeachers = async () => {
    try {
      const res = await axiosAuth.get("/api/admin/users", {
        params: {
          role: "TEACHER",
          status: "ACTIVE"
        }
      });
      setAvailableTeachers(res.data || []);
    } catch (err) {
      console.log(
        "Failed to load active teachers",
        err?.response?.status,
        err?.response?.data
      );
      setAvailableTeachers([]);
      setCourseMessage(
        err?.response?.data?.message || "Failed to load active teachers"
      );
    }
  };

  const loadDepartments = async () => {
    try {
      const res = await axiosAuth.get("/api/departments");
      const deptData = res.data || [];
      setDepartments(deptData);

      if (!selectedDeptId && deptData.length > 0) {
        setSelectedDeptId(deptData[0].id);
      }
    } catch (err) {
      console.log(
        "Failed to load departments",
        err?.response?.status,
        err?.response?.data
      );
      setDepartments([]);
      setMessage(err?.response?.data?.message || "Failed to load departments");
    }
  };

  const loadCourses = async () => {
    try {
      const res = await axiosAuth.get("/api/courses");
      const courseData = res.data || [];
      setCourses(courseData);

      if (!predictionCourseId && courseData.length > 0) {
        setPredictionCourseId(courseData[0].id);
      }
    } catch (err) {
      console.log(
        "Failed to load courses",
        err?.response?.status,
        err?.response?.data
      );
      setCourses([]);
      setCourseMessage(err?.response?.data?.message || "Failed to load courses");
    }
  };

  const loadPredictionSummary = async () => {
    try {
      const res = await getAdminPredictionSummary();
      setPredictionSummary(
        res.data || {
          totalPredictions: 0,
          highRiskCount: 0,
          mediumRiskCount: 0,
          lowRiskCount: 0,
          recentPredictions: []
        }
      );
    } catch (err) {
      console.log("Failed to load admin prediction summary", err);
      setPredictionSummary({
        totalPredictions: 0,
        highRiskCount: 0,
        mediumRiskCount: 0,
        lowRiskCount: 0,
        recentPredictions: []
      });
    }
  };

  const getProbabilityPercent = (value) => {
  if (value == null || Number.isNaN(Number(value))) return 0;
  return Math.max(0, Math.min(100, Math.round(Number(value) * 100)));
};

const getRiskMessage = (riskLevel) => {
  if (riskLevel === "HIGH") {
    return "This student should be prioritized for immediate academic follow-up.";
  }
  if (riskLevel === "MEDIUM") {
    return "This student should be monitored for consistency and engagement.";
  }
  return "Current platform activity suggests relatively stable learning behavior.";
};

  const loadAdminEngagementSummary = async () => {
    try {
      setAdminEngagementLoading(true);
      const res = await getAdminEngagementSummary();
      setAdminEngagementSummary(
        res.data || {
          totalLogs: 0,
          attentiveCount: 0,
          neutralCount: 0,
          distractedCount: 0,
          averageScore: 0,
          recentLogs: []
        }
      );
    } catch (err) {
      console.log("Failed to load admin engagement summary", err);
      setAdminEngagementSummary({
        totalLogs: 0,
        attentiveCount: 0,
        neutralCount: 0,
        distractedCount: 0,
        averageScore: 0,
        recentLogs: []
      });
    } finally {
      setAdminEngagementLoading(false);
    }
  };

  const loadPredictionStudents = async (courseId) => {
    if (!courseId) {
      setPredictionStudents([]);
      setPredictionStudentId("");
      return;
    }

    try {
      const res = await getStudentsByCourse(courseId);
      const data = res.data || [];
      setPredictionStudents(data);
      setPredictionStudentId(data.length > 0 ? data[0].id : "");
    } catch (err) {
      console.log("Failed to load students for prediction", err);
      setPredictionStudents([]);
      setPredictionStudentId("");
    }
  };

  const loadAllPredictions = async () => {
    try {
      const res = await getAllAdminPredictions();
      setAllPredictions(res.data || []);
    } catch (err) {
      console.log("Failed to load all predictions", err);
      setAllPredictions([]);
    }
  };

  const loadInstitutionSettings = async () => {
    try {
      setInstitutionLoading(true);
      const res = await getInstitutionSettings();
      setInstitutionSettings(res.data || emptyInstitutionSettings);
    } catch (err) {
      console.log("Failed to load institution settings", err);
      setInstitutionSettings(emptyInstitutionSettings);
    } finally {
      setInstitutionLoading(false);
    }
  };

  useEffect(() => {
    if (!token) {
      console.log("No token found. Login again.");
      return;
    }

    loadAdminDashboardSummary();
    loadPendingTeachers();
    loadActiveTeachers();
    loadDepartments();
    loadCourses();
    loadPredictionSummary();
    loadAllPredictions();
    loadAdminEngagementSummary();
    loadInstitutionSettings();
    // eslint-disable-next-line
  }, [token]);

  useEffect(() => {
    if (predictionCourseId) {
      loadPredictionStudents(predictionCourseId);
    }
    // eslint-disable-next-line
  }, [predictionCourseId]);

  const approveTeacher = async (id) => {
    try {
      await axiosAuth.post(`/api/admin/users/approve/${id}`, {});
      setTeachers((prev) => prev.filter((t) => t.id !== id));
      await loadPendingTeachers();
      await loadActiveTeachers();
      await loadAdminDashboardSummary();
    } catch (err) {
      console.log(
        "Approve teacher failed",
        err?.response?.status,
        err?.response?.data
      );
    }
  };

  const rejectTeacher = async (id) => {
    try {
      await axiosAuth.post(`/api/admin/users/reject/${id}`, {});
      setTeachers((prev) => prev.filter((t) => t.id !== id));
      await loadPendingTeachers();
      await loadAdminDashboardSummary();
    } catch (err) {
      console.log(
        "Reject teacher failed",
        err?.response?.status,
        err?.response?.data
      );
    }
  };

  const resetDepartmentForm = () => {
    setDeptCode("");
    setDeptName("");
    setDeptDescription("");
    setEditingDeptId(null);
  };

  const editDepartment = (dept) => {
    setDeptCode(dept.code || "");
    setDeptName(dept.name || "");
    setDeptDescription(dept.description || "");
    setEditingDeptId(dept.id);
    setMessage("");
    setActiveTab("DEPTS");
  };

  const createDepartment = async () => {
    setMessage("");

    if (!deptCode.trim() || !deptName.trim()) {
      setMessage("Department code and name are required");
      return;
    }

    const payload = {
      code: deptCode.trim().toUpperCase(),
      name: deptName.trim(),
      description: deptDescription.trim()
    };

    try {
      if (editingDeptId) {
        await axiosAuth.put(`/api/departments/${editingDeptId}`, payload);
        setMessage("Department updated successfully");
      } else {
        await axiosAuth.post("/api/departments", payload);
        setMessage("Department created successfully");
      }

      resetDepartmentForm();
      await loadDepartments();
      await loadAdminDashboardSummary();
    } catch (err) {
      console.log(
        "Save department failed",
        err?.response?.status,
        err?.response?.data
      );
      setMessage(err?.response?.data?.message || "Save department failed");
    }
  };

  const deleteDepartment = async (id) => {
    try {
      await axiosAuth.delete(`/api/departments/${id}`);

      if (editingDeptId === id) {
        resetDepartmentForm();
      }

      setMessage("Department deleted successfully");
      await loadDepartments();
      await loadCourses();
      await loadAdminDashboardSummary();
    } catch (err) {
      console.log(
        "Delete department failed",
        err?.response?.status,
        err?.response?.data
      );
      setMessage(err?.response?.data?.message || "Delete department failed");
    }
  };

  const resetCourseForm = () => {
    setCourseTitle("");
    setCourseCode("");
    setCourseDescription("");
    setSelectedDeptId(departments.length > 0 ? departments[0].id : "");
    setSelectedTeacherId("");
    setCreditValue("");
    setSemester("");
    setAcademicYear("");
    setCourseStatus("ACTIVE");
    setEditingCourseId(null);
  };

  const editCourse = (course) => {
    setCourseTitle(course.title || "");
    setCourseCode(course.courseCode || "");
    setCourseDescription(course.description || "");
    setSelectedDeptId(course.departmentId || "");
    setSelectedTeacherId(course.teacherId || "");
    setCreditValue(course.creditValue?.toString() || "");
    setSemester(course.semester?.toString() || "");
    setAcademicYear(course.academicYear || "");
    setCourseStatus(course.status || "ACTIVE");
    setEditingCourseId(course.id);
    setCourseMessage("");
    setActiveTab("COURSES");
  };

  const createCourse = async () => {
    setCourseMessage("");

    if (
      !courseTitle.trim() ||
      !courseCode.trim() ||
      !selectedDeptId ||
      !creditValue ||
      !semester ||
      !academicYear.trim()
    ) {
      setCourseMessage(
        "Course code, title, department, credits, semester, and academic year are required"
      );
      return;
    }

    const basePayload = {
      courseCode: courseCode.trim().toUpperCase(),
      title: courseTitle.trim(),
      description: courseDescription.trim(),
      departmentId: selectedDeptId,
      teacherId: selectedTeacherId || null,
      creditValue: Number(creditValue),
      semester: Number(semester),
      academicYear: academicYear.trim()
    };

    try {
      if (editingCourseId) {
        await axiosAuth.put(`/api/courses/${editingCourseId}`, {
          ...basePayload,
          status: courseStatus
        });
        setCourseMessage("Course updated successfully");
      } else {
        await axiosAuth.post("/api/courses", basePayload);
        setCourseMessage("Course created successfully");
      }

      resetCourseForm();
      await loadCourses();
      await loadAdminDashboardSummary();
    } catch (err) {
      console.log(
        "Save course failed",
        err?.response?.status,
        err?.response?.data
      );
      setCourseMessage(err?.response?.data?.message || "Save course failed");
    }
  };

  const deleteCourse = async (id) => {
    try {
      await axiosAuth.delete(`/api/courses/${id}`);

      if (editingCourseId === id) {
        resetCourseForm();
      }

      setCourseMessage("Course deleted successfully");
      await loadCourses();
      await loadAdminDashboardSummary();
    } catch (err) {
      console.log(
        "Delete course failed",
        err?.response?.status,
        err?.response?.data
      );
      setCourseMessage(err?.response?.data?.message || "Delete course failed");
    }
  };

  const handleAdminRunPrediction = async () => {
    setPredictionMessage("");
    setPredictionResult(null);
    setPredictionLoading(true);

    try {
      if (!predictionCourseId) {
        throw new Error("Please select a course.");
      }

      if (!predictionStudentId) {
        throw new Error("Please select a student.");
      }

      let parsedOverrides = {};
      if (predictionFeaturesText.trim()) {
        try {
          parsedOverrides = JSON.parse(predictionFeaturesText);
        } catch {
          throw new Error("Optional Feature Overrides JSON is not valid.");
        }
      }

      const res = await runDropoutPrediction({
        studentId: predictionStudentId,
        courseId: predictionCourseId,
        windowDays: predictionWindowDays,
        featureOverrides: parsedOverrides
      });

      setPredictionResult(res.data);
      setPredictionMessage("Prediction saved successfully.");

      await loadPredictionSummary();
      await loadAllPredictions();
      await loadAdminDashboardSummary();
    } catch (err) {
      setPredictionMessage(
        err?.response?.data?.message ||
          err?.response?.data?.detail ||
          err?.message ||
          "Prediction failed."
      );
    } finally {
      setPredictionLoading(false);
    }
  };

  const saveInstitutionSettings = async () => {
    setInstitutionMessage("");

    try {
      const payload = {
        institutionName: institutionSettings.institutionName,
        institutionCode: institutionSettings.institutionCode,
        contactEmail: institutionSettings.contactEmail,
        contactNumber: institutionSettings.contactNumber,
        supportEmail: institutionSettings.supportEmail,
        address: institutionSettings.address,
        websiteUrl: institutionSettings.websiteUrl,
        logoUrl: institutionSettings.logoUrl,
        academicYear: institutionSettings.academicYear,
        currentSemester: institutionSettings.currentSemester,
        description: institutionSettings.description,
        timezone: institutionSettings.timezone
      };

      const res = await updateInstitutionSettings(payload);
      setInstitutionSettings(res.data || emptyInstitutionSettings);
      setInstitutionMessage("Institution settings saved successfully.");
    } catch (err) {
      setInstitutionMessage(
        err?.response?.data?.message ||
          err?.response?.data ||
          "Failed to save institution settings."
      );
    }
  };

  const getRiskBadgeClass = (riskLevel) => {
    if (riskLevel === "HIGH") return "inactive";
    if (riskLevel === "MEDIUM") return "medium";
    return "active";
  };

  const formatTimestamp = (value) => {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
  };

  const overviewStats = useMemo(
    () => [
      {
        title: "Students",
        value: dashboardSummary.overview.totalStudents || 0,
        icon: <Users size={20} />
      },
      {
        title: "Teachers",
        value: dashboardSummary.overview.totalTeachers || 0,
        icon: <ShieldCheck size={20} />
      },
      {
        title: "Courses",
        value: dashboardSummary.overview.totalCourses || 0,
        icon: <BookOpen size={20} />
      },
      {
        title: "Departments",
        value: dashboardSummary.overview.totalDepartments || 0,
        icon: <Building2 size={20} />
      },
      {
        title: "High Risk",
        value: dashboardSummary.overview.highRiskCount || 0,
        icon: <ShieldAlert size={20} />
      },
      {
        title: "Quizzes",
        value: dashboardSummary.overview.totalQuizzes || 0,
        icon: <Brain size={20} />
      }
    ],
    [dashboardSummary]
  );

  const renderChartRows = (items, colorClass = "fill-low") => {
    const maxValue = Math.max(1, ...(items || []).map((item) => item.value || 0));

    if (!items || items.length === 0) {
      return <div className="muted">No chart data yet.</div>;
    }

    return items.map((item) => (
      <div className="chart-row" key={item.label}>
        <div className="chart-label">
          <span>{item.label}</span>
          <span>{item.value}</span>
        </div>
        <div className="chart-bar-bg">
          <div
            className={`chart-bar-fill ${colorClass}`}
            style={{ width: `${((item.value || 0) / maxValue) * 100}%` }}
          />
        </div>
      </div>
    ));
  };

  const renderRiskRows = (items) => {
    const maxValue = Math.max(1, ...(items || []).map((item) => item.value || 0));

    if (!items || items.length === 0) {
      return <div className="muted">No chart data yet.</div>;
    }

    return items.map((item) => {
      const fillClass =
        item.label === "High"
          ? "fill-high"
          : item.label === "Medium"
          ? "fill-medium"
          : "fill-low";

      return (
        <div className="chart-row" key={item.label}>
          <div className="chart-label">
            <span>{item.label}</span>
            <span>{item.value}</span>
          </div>
          <div className="chart-bar-bg">
            <div
              className={`chart-bar-fill ${fillClass}`}
              style={{ width: `${((item.value || 0) / maxValue) * 100}%` }}
            />
          </div>
        </div>
      );
    });
  };

  return (
    <div className="admin-layout">
      <style>{`
        * { box-sizing: border-box; }

        .admin-layout {
          display: flex;
          min-height: 100vh;
          background: var(--bg-main);
          color: var(--text-main);
          font-family: Inter, sans-serif;
        }

        .main-content {
          flex: 1;
          padding: 28px;
          overflow-x: hidden;
          color: var(--text-main);
        }

        .top-nav {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 20px;
          margin-bottom: 26px;
        }

        .breadcrumb {
          display: flex;
          align-items: center;
          gap: 10px;
          color: var(--text-muted);
          font-size: 14px;
        }

        .top-nav-right {
          display: flex;
          align-items: center;
          gap: 16px;
        }

        .icon-circle {
          width: 42px;
          height: 42px;
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          backdrop-filter: blur(10px);
        }

        .avatar {
          width: 42px;
          height: 42px;
          border-radius: 14px;
          background: linear-gradient(135deg, var(--accent), #f5c400);
          color: #111827;
          font-weight: 800;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 10px 24px rgba(255, 213, 72, 0.25);
        }

        .hero {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 20px;
          margin-bottom: 26px;
          flex-wrap: wrap;
        }

        .hero h2 {
          font-size: 34px;
          margin: 0;
          line-height: 1.2;
          background: linear-gradient(90deg, var(--text-main) 0%, var(--accent) 45%, var(--text-main) 100%);
          background-size: 200% auto;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          animation: shine 5s linear infinite;
        }

        .hero p {
          margin-top: 10px;
          color: var(--text-muted);
          max-width: 760px;
          line-height: 1.6;
        }

        @keyframes shine {
          to { background-position: 200% center; }
        }

        .overview-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
          gap: 16px;
          margin-bottom: 26px;
        }

        .stat-card {
          position: relative;
          overflow: hidden;
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 24px;
          padding: 20px;
          backdrop-filter: blur(14px);
          box-shadow: 0 10px 30px rgba(0,0,0,0.12);
        }

        .stat-card::before {
          content: "";
          position: absolute;
          inset: 0;
          background: linear-gradient(135deg, rgba(255,213,72,0.08), transparent 50%);
          pointer-events: none;
        }

        .stat-top {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;
        }

        .stat-icon {
          width: 44px;
          height: 44px;
          border-radius: 14px;
          background: var(--accent-soft);
          border: 1px solid rgba(255, 213, 72, 0.22);
          color: var(--accent);
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .stat-title {
          color: var(--text-muted);
          font-size: 13px;
          font-weight: 600;
        }

        .stat-value {
          font-size: 28px;
          font-weight: 800;
          color: var(--text-main);
        }

        .section-switch {
          display: flex;
          gap: 10px;
          margin-bottom: 22px;
          flex-wrap: wrap;
        }

        .tab-btn {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          color: var(--text-soft);
          padding: 12px 16px;
          border-radius: 16px;
          cursor: pointer;
          transition: 0.25s ease;
          display: flex;
          gap: 10px;
          align-items: center;
          font-weight: 700;
          font-size: 13px;
        }

        .tab-btn:hover {
          background: var(--bg-card-2);
          transform: translateY(-1px);
        }

        .tab-btn.active {
          background: var(--accent-soft);
          border-color: rgba(255, 213, 72, 0.35);
          color: var(--accent);
          box-shadow: 0 10px 24px rgba(255, 213, 72, 0.12);
        }

        .content-card {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 26px;
          padding: 22px;
          backdrop-filter: blur(14px);
          box-shadow: 0 10px 34px rgba(0,0,0,0.12);
        }

        .section-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 10px;
          margin-bottom: 18px;
          flex-wrap: wrap;
        }

        .section-title {
          display: flex;
          align-items: center;
          gap: 10px;
          margin: 0;
          color: var(--accent);
          font-size: 20px;
        }

        .section-subtitle {
          color: var(--text-muted);
          font-size: 14px;
        }

        .management-grid {
          display: grid;
          grid-template-columns: 370px minmax(0, 1fr);
          gap: 20px;
        }

        .form-panel,
        .list-panel {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 22px;
          padding: 18px;
        }

        .panel-heading {
          margin: 0 0 16px 0;
          font-size: 17px;
          color: var(--text-main);
        }

        .form-grid {
          display: grid;
          grid-template-columns: 1fr;
          gap: 12px;
        }

        .input,
        .select,
        .textarea {
          width: 100%;
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
          padding: 12px 14px;
          border-radius: 14px;
          outline: none;
          font-size: 14px;
          transition: 0.2s ease;
        }

        .input:focus,
        .select:focus,
        .textarea:focus {
          border-color: rgba(255, 213, 72, 0.45);
          box-shadow: 0 0 0 3px rgba(255, 213, 72, 0.08);
        }

        .textarea {
          min-height: 100px;
          resize: vertical;
        }

        .input::placeholder,
        .textarea::placeholder {
          color: var(--text-muted);
        }

        .select option {
          color: #111827;
        }

        .form-row-2 {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 12px;
        }

        .action-row {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
          margin-top: 4px;
        }

        .primary-btn,
        .secondary-btn,
        .danger-btn,
        .btn-approve,
        .btn-reject,
        .edit-btn {
          border: none;
          border-radius: 14px;
          padding: 11px 15px;
          font-weight: 700;
          cursor: pointer;
          display: inline-flex;
          align-items: center;
          gap: 8px;
          transition: 0.25s ease;
        }

        .primary-btn {
          background: var(--accent-soft);
          border: 1px solid rgba(255,213,72,0.35);
          color: var(--accent);
        }

        .primary-btn:disabled {
          opacity: 0.7;
          cursor: not-allowed;
        }

        .primary-btn:hover,
        .secondary-btn:hover,
        .danger-btn:hover,
        .btn-approve:hover,
        .btn-reject:hover,
        .edit-btn:hover {
          transform: translateY(-1px);
        }

        .secondary-btn {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
        }

        .danger-btn,
        .btn-reject {
          background: rgba(248,113,113,0.08);
          border: 1px solid rgba(248,113,113,0.25);
          color: var(--danger);
        }

        .btn-approve {
          background: rgba(74,222,128,0.12);
          border: 1px solid rgba(74,222,128,0.28);
          color: var(--success);
        }

        .edit-btn {
          background: rgba(96,165,250,0.10);
          border: 1px solid rgba(96,165,250,0.24);
          color: #60a5fa;
        }

        .message-text {
          color: var(--accent);
          margin-bottom: 14px;
          font-size: 14px;
          font-weight: 600;
        }

        .info-box {
          margin-bottom: 14px;
          padding: 12px 14px;
          border-radius: 14px;
          background: rgba(245, 158, 11, 0.10);
          border: 1px solid rgba(245, 158, 11, 0.28);
          color: var(--text-main);
          font-size: 14px;
          line-height: 1.6;
        }

        .list-panel-inner {
          display: flex;
          flex-direction: column;
          gap: 12px;
          max-height: 560px;
          overflow-y: auto;
          padding-right: 4px;
        }

        .list-panel-inner::-webkit-scrollbar {
          width: 8px;
        }

        .list-panel-inner::-webkit-scrollbar-thumb {
          background: var(--border-color);
          border-radius: 10px;
        }

        .list-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 14px;
          padding: 16px;
          border-radius: 18px;
          border: 1px solid var(--border-soft);
          background: var(--bg-card-2);
        }

        .list-item-title {
          font-weight: 800;
          margin-bottom: 6px;
          color: var(--text-main);
        }

        .muted {
          color: var(--text-muted);
          font-size: 13px;
          line-height: 1.6;
        }

        .item-actions {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
          justify-content: flex-end;
        }

        .badge {
          display: inline-flex;
          align-items: center;
          padding: 4px 10px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 700;
          margin-top: 6px;
          width: fit-content;
          white-space: nowrap;
        }

        .badge.active {
          background: rgba(74,222,128,0.12);
          border: 1px solid rgba(74,222,128,0.25);
          color: var(--success);
        }

        .badge.inactive {
          background: rgba(248,113,113,0.08);
          border: 1px solid rgba(248,113,113,0.22);
          color: var(--danger);
        }

        .badge.medium {
          background: rgba(245, 158, 11, 0.10);
          border: 1px solid rgba(245, 158, 11, 0.26);
          color: #d97706;
        }

        .teacher-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(290px, 1fr));
          gap: 16px;
        }

        .teacher-card {
          background: var(--bg-card-2);
          border: 1px solid var(--border-soft);
          border-radius: 22px;
          padding: 18px;
          display: flex;
          flex-direction: column;
          gap: 14px;
        }

        .teacher-top {
          display: flex;
          align-items: center;
          gap: 14px;
        }

        .teacher-avatar {
          width: 48px;
          height: 48px;
          border-radius: 16px;
          background: var(--accent-soft);
          color: var(--accent);
          display: flex;
          align-items: center;
          justify-content: center;
          border: 1px solid rgba(255, 213, 72, 0.25);
          flex-shrink: 0;
        }

        .teacher-name {
          font-weight: 800;
          margin-bottom: 4px;
          color: var(--text-main);
        }

        .teacher-actions {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .empty-state {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          gap: 10px;
          min-height: 220px;
          border: 1px dashed var(--border-color);
          border-radius: 22px;
          color: var(--text-muted);
          text-align: center;
        }

        .chart-box {
          margin-top: 16px;
          padding: 16px;
          border-radius: 16px;
          border: 1px solid var(--border-soft);
          background: var(--bg-card-2);
          margin-bottom: 22px;
        }

        .chart-row {
          margin-bottom: 14px;
        }

        .chart-label {
          display: flex;
          justify-content: space-between;
          margin-bottom: 6px;
          font-size: 14px;
          color: var(--text-muted);
          gap: 12px;
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
        }

        .fill-high {
          background: rgba(239, 68, 68, 0.8);
        }

        .fill-medium {
          background: rgba(245, 158, 11, 0.8);
        }

        .fill-low {
          background: rgba(34, 197, 94, 0.8);
        }

        .logo-preview {
          max-width: 180px;
          max-height: 180px;
          object-fit: contain;
          margin-top: 10px;
          border-radius: 12px;
          border: 1px solid var(--border-soft);
          padding: 8px;
          background: var(--bg-card);
        }

        @media (max-width: 1100px) {
          .management-grid {
            grid-template-columns: 1fr;
          }
        }

        @media (max-width: 700px) {
          .main-content {
            padding: 18px;
          }

          .top-nav {
            flex-direction: column;
            align-items: flex-start;
          }

          .form-row-2 {
            grid-template-columns: 1fr;
          }

          .hero h2 {
            font-size: 28px;
          }

          .list-item {
            flex-direction: column;
          }

          .item-actions {
            justify-content: flex-start;
          }
        }
          .advanced-toggle {
  background: var(--bg-card-2);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 11px 15px;
  border-radius: 14px;
  cursor: pointer;
  font-weight: 700;
  transition: 0.25s ease;
}

.advanced-toggle:hover {
  transform: translateY(-1px);
  border-color: rgba(255,213,72,0.35);
}

.advanced-panel {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  border: 1px dashed var(--border-color);
  background: var(--bg-card-2);
}

.prediction-result-card {
  margin-top: 16px;
  border: 1px solid var(--border-soft);
  border-radius: 20px;
  padding: 18px;
  background: var(--bg-card-2);
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
  color: var(--text-main);
}

.prediction-result-subtitle {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 13.5px;
}

.prediction-probability-box {
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-soft);
}

.prediction-probability-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.prediction-probability-value {
  font-size: 24px;
  font-weight: 900;
  color: var(--text-main);
}

.prediction-progress {
  width: 100%;
  height: 14px;
  border-radius: 999px;
  background: rgba(255,255,255,0.08);
  overflow: hidden;
}

.prediction-progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.3s ease;
}

.prediction-progress-fill.high {
  background: rgba(239, 68, 68, 0.85);
}

.prediction-progress-fill.medium {
  background: rgba(245, 158, 11, 0.85);
}

.prediction-progress-fill.low {
  background: rgba(34, 197, 94, 0.85);
}

.prediction-metric-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 12px;
}

.prediction-metric-card {
  grid-column: span 4;
  padding: 14px;
  border-radius: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-soft);
}

.prediction-metric-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.prediction-metric-value {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-main);
  word-break: break-word;
}

.prediction-interpretation {
  margin-top: 16px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 213, 72, 0.08);
  border: 1px solid rgba(255, 213, 72, 0.20);
  color: var(--text-main);
  line-height: 1.6;
}

@media (max-width: 700px) {
  .prediction-metric-card {
    grid-column: span 12;
  }
}
      `}</style>

      <Menu />

      <main className="main-content">
        <header className="top-nav">
          <div className="breadcrumb">
            <LayoutDashboard size={18} />
            <span>Administration / Overview</span>
          </div>

          <div className="top-nav-right">
            <div className="icon-circle">
              <Bell size={18} color="var(--text-soft)" />
            </div>
            <div className="avatar">{initial}</div>
          </div>
        </header>

        <section className="hero">
          <div>
            <h2>Admin Control Center</h2>
            <p>
              Manage institutional operations, academic structure, dropout risk,
              communication activity, engagement indicators, and institution
              profile settings from one structured administrative dashboard.
            </p>
          </div>
        </section>

        <div className="section-switch">
          <button
            className={`tab-btn ${activeTab === "OVERVIEW" ? "active" : ""}`}
            onClick={() => setActiveTab("OVERVIEW")}
          >
            <LayoutDashboard size={16} /> Overview
          </button>

          <button
            className={`tab-btn ${activeTab === "TEACHERS" ? "active" : ""}`}
            onClick={() => setActiveTab("TEACHERS")}
          >
            <ShieldCheck size={16} /> Teacher Requests
          </button>

          <button
            className={`tab-btn ${activeTab === "DEPTS" ? "active" : ""}`}
            onClick={() => setActiveTab("DEPTS")}
          >
            <Building2 size={16} /> Departments
          </button>

          <button
            className={`tab-btn ${activeTab === "COURSES" ? "active" : ""}`}
            onClick={() => setActiveTab("COURSES")}
          >
            <BookOpen size={16} /> Courses
          </button>

          <button
            className={`tab-btn ${activeTab === "PREDICTIONS" ? "active" : ""}`}
            onClick={() => setActiveTab("PREDICTIONS")}
          >
            <Brain size={16} /> Predictions
          </button>

          <button
            className={`tab-btn ${activeTab === "INSTITUTION" ? "active" : ""}`}
            onClick={() => setActiveTab("INSTITUTION")}
          >
            <Settings size={16} /> Institution
          </button>
        </div>

        {activeTab === "OVERVIEW" && (
          <div className="content-card">
            <div className="section-header">
              <h3 className="section-title">
                <LayoutDashboard size={19} />
                Administrative Overview
              </h3>
              <span className="section-subtitle">
                Real analytics based on live system data
              </span>
            </div>

            {dashboardLoading ? (
              <div className="empty-state">
                <BarChart3 size={40} style={{ opacity: 0.5 }} />
                <div>Loading dashboard summary...</div>
              </div>
            ) : (
              <>
                <div className="overview-grid" style={{ marginBottom: "22px" }}>
                  {overviewStats.map((item) => (
                    <div className="stat-card" key={item.title}>
                      <div className="stat-top">
                        <div>
                          <div className="stat-title">{item.title}</div>
                          <div className="stat-value">{item.value}</div>
                        </div>
                        <div className="stat-icon">{item.icon}</div>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="management-grid">
                  <div className="form-panel">
                    <h4 className="panel-heading">Users by Role</h4>
                    <div className="chart-box">
                      {renderChartRows(
                        dashboardSummary.charts.usersByRole,
                        "fill-low"
                      )}
                    </div>

                    <h4 className="panel-heading">Content Activity</h4>
                    <div className="chart-box">
                      {renderChartRows(
                        dashboardSummary.charts.contentActivity,
                        "fill-medium"
                      )}
                    </div>

                    <h4 className="panel-heading">Risk Distribution</h4>
                    <div className="chart-box">
                      {renderRiskRows(dashboardSummary.charts.riskDistribution)}
                    </div>
                  </div>

                  <div className="list-panel">
                    <h4 className="panel-heading">Recent Activity</h4>

                    {dashboardSummary.recentActivities?.length === 0 ? (
                      <div className="empty-state">
                        <BarChart3 size={40} style={{ opacity: 0.5 }} />
                        <div>No recent activity found.</div>
                      </div>
                    ) : (
                      <div className="list-panel-inner">
                        {dashboardSummary.recentActivities.map((item, index) => (
                          <div
                            className="list-item"
                            key={`${item.type}-${item.timestamp}-${index}`}
                          >
                            <div>
                              <div className="list-item-title">{item.title}</div>
                              <div className="muted">{item.subtitle}</div>
                              <div className="muted">
                                {formatTimestamp(item.timestamp)}
                              </div>
                            </div>

                            <div className="item-actions">
                              <div className="badge active">{item.type}</div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                <div className="management-grid" style={{ marginTop: "20px" }}>
                  <div className="form-panel">
                    <h4 className="panel-heading">Session Status Distribution</h4>
                    <div className="chart-box">
                      {renderChartRows(
                        dashboardSummary.charts.sessionStatusDistribution,
                        "fill-low"
                      )}
                    </div>

                    <h4 className="panel-heading">Courses by Department</h4>
                    <div className="chart-box">
                      {renderChartRows(
                        dashboardSummary.charts.coursesByDepartment,
                        "fill-medium"
                      )}
                    </div>
                  </div>

                  <div className="list-panel">
                    <h4 className="panel-heading">Engagement Snapshot</h4>

                    <div className="overview-grid" style={{ marginBottom: "14px" }}>
                      <div className="stat-card">
                        <div className="stat-top">
                          <div>
                            <div className="stat-title">Total Logs</div>
                            <div className="stat-value">
                              {adminEngagementSummary.totalLogs || 0}
                            </div>
                          </div>
                          <div className="stat-icon">
                            <Brain size={20} />
                          </div>
                        </div>
                      </div>

                      <div className="stat-card">
                        <div className="stat-top">
                          <div>
                            <div className="stat-title">Attentive</div>
                            <div className="stat-value">
                              {adminEngagementSummary.attentiveCount || 0}
                            </div>
                          </div>
                          <div className="stat-icon">
                            <CheckCircle2 size={20} />
                          </div>
                        </div>
                      </div>

                      <div className="stat-card">
                        <div className="stat-top">
                          <div>
                            <div className="stat-title">Neutral</div>
                            <div className="stat-value">
                              {adminEngagementSummary.neutralCount || 0}
                            </div>
                          </div>
                          <div className="stat-icon">
                            <AlertTriangle size={20} />
                          </div>
                        </div>
                      </div>

                      <div className="stat-card">
                        <div className="stat-top">
                          <div>
                            <div className="stat-title">Distracted</div>
                            <div className="stat-value">
                              {adminEngagementSummary.distractedCount || 0}
                            </div>
                          </div>
                          <div className="stat-icon">
                            <ShieldAlert size={20} />
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="info-box">
                      {adminEngagementLoading ? (
                        "Loading engagement summary..."
                      ) : (
                        <>
                          Average Engagement Score:{" "}
                          <strong>
                            {Number(
                              adminEngagementSummary.averageScore || 0
                            ).toFixed(2)}
                          </strong>
                        </>
                      )}
                    </div>

                    <h4 className="panel-heading">Recent Engagement Logs</h4>

                    {adminEngagementLoading ? (
                      <div className="empty-state">
                        <Brain size={40} style={{ opacity: 0.5 }} />
                        <div>Loading recent engagement logs...</div>
                      </div>
                    ) : adminEngagementSummary.recentLogs?.length === 0 ? (
                      <div className="empty-state">
                        <Brain size={40} style={{ opacity: 0.5 }} />
                        <div>No engagement logs found yet.</div>
                      </div>
                    ) : (
                      <div className="list-panel-inner">
                        {adminEngagementSummary.recentLogs
                          .slice(0, 8)
                          .map((item, index) => (
                            <div
                              className="list-item"
                              key={`${item.studentId}-${item.capturedAt}-${index}`}
                            >
                              <div>
                                <div className="list-item-title">
                                  {item.studentName || "Student"}
                                </div>
                                <div className="muted">
                                  {item.studentEmail || "-"}
                                </div>
                                <div className="muted">
                                  Course: {item.courseId || "-"} | Session:{" "}
                                  {item.sessionId || "-"}
                                </div>
                                <div className="muted">
                                  Emotion:{" "}
                                  <strong>{item.dominantEmotion || "N/A"}</strong>
                                </div>
                                <div className="muted">
                                  Score: {item.engagementScore ?? "-"} |
                                  Confidence:{" "}
                                  {item.confidence != null
                                    ? Number(item.confidence).toFixed(2)
                                    : "-"}
                                </div>
                              </div>

                              <div className="item-actions">
                                <div
                                  className={`badge ${
                                    item.label === "ATTENTIVE"
                                      ? "active"
                                      : item.label === "NEUTRAL"
                                      ? "medium"
                                      : "inactive"
                                  }`}
                                >
                                  {item.label || "UNKNOWN"}
                                </div>
                              </div>
                            </div>
                          ))}
                      </div>
                    )}
                  </div>
                </div>
              </>
            )}
          </div>
        )}

        {activeTab === "TEACHERS" && (
          <div className="content-card">
            <div className="section-header">
              <h3 className="section-title">
                <ShieldAlert size={19} />
                Verification Queue
              </h3>
              <span className="section-subtitle">
                Review and approve pending teacher registrations
              </span>
            </div>

            {teachers.length === 0 ? (
              <div className="empty-state">
                <UserPlus size={42} style={{ opacity: 0.5 }} />
                <div>No pending teacher requests right now.</div>
              </div>
            ) : (
              <div className="teacher-grid">
                {teachers.map((t) => (
                  <div key={t.id} className="teacher-card">
                    <div className="teacher-top">
                      <div className="teacher-avatar">
                        <UserPlus size={20} />
                      </div>

                      <div>
                        <div className="teacher-name">{t.fullName}</div>
                        <div className="muted">{t.email}</div>
                      </div>
                    </div>

                    <div className="teacher-actions">
                      <button
                        className="btn-reject"
                        onClick={() => rejectTeacher(t.id)}
                      >
                        Reject
                      </button>
                      <button
                        className="btn-approve"
                        onClick={() => approveTeacher(t.id)}
                      >
                        <CheckCircle size={16} />
                        Approve
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === "DEPTS" && (
          <div className="content-card">
            <div className="section-header">
              <h3 className="section-title">
                <Building2 size={19} />
                Department Management
              </h3>
              <span className="section-subtitle">
                Create, edit, and remove departments
              </span>
            </div>

            <div className="management-grid">
              <div className="form-panel">
                <h4 className="panel-heading">
                  {editingDeptId ? "Edit Department" : "Add New Department"}
                </h4>

                {message && <div className="message-text">{message}</div>}

                <div className="form-grid">
                  <input
                    className="input"
                    placeholder="Department code (e.g., CSE)"
                    value={deptCode}
                    onChange={(e) => setDeptCode(e.target.value)}
                  />

                  <input
                    className="input"
                    placeholder="Department name (e.g., Computing)"
                    value={deptName}
                    onChange={(e) => setDeptName(e.target.value)}
                  />

                  <textarea
                    className="textarea"
                    placeholder="Description (optional)"
                    value={deptDescription}
                    onChange={(e) => setDeptDescription(e.target.value)}
                  />

                  <div className="action-row">
                    <button className="primary-btn" onClick={createDepartment}>
                      <PlusCircle size={17} />
                      {editingDeptId ? "Update Department" : "Add Department"}
                    </button>

                    {editingDeptId && (
                      <button
                        type="button"
                        className="secondary-btn"
                        onClick={resetDepartmentForm}
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </div>
              </div>

              <div className="list-panel">
                <h4 className="panel-heading">Department List</h4>

                {departments.length === 0 ? (
                  <div className="empty-state">
                    <Building2 size={40} style={{ opacity: 0.5 }} />
                    <div>No departments added yet.</div>
                  </div>
                ) : (
                  <div className="list-panel-inner">
                    {departments.map((d) => (
                      <div className="list-item" key={d.id}>
                        <div>
                          <div className="list-item-title">
                            {d.code} — {d.name}
                          </div>
                          <div className="muted">
                            {d.description || "No description provided"}
                          </div>
                        </div>

                        <div className="item-actions">
                          <button
                            type="button"
                            className="edit-btn"
                            onClick={() => editDepartment(d)}
                          >
                            <Pencil size={15} />
                            Edit
                          </button>

                          <button
                            type="button"
                            className="danger-btn"
                            onClick={() => deleteDepartment(d.id)}
                          >
                            <Trash2 size={15} />
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {activeTab === "COURSES" && (
          <div className="content-card">
            <div className="section-header">
              <h3 className="section-title">
                <Layers3 size={19} />
                Course Management
              </h3>
              <span className="section-subtitle">
                Add, update, and maintain academic course details
              </span>
            </div>

            <div className="management-grid">
              <div className="form-panel">
                <h4 className="panel-heading">
                  {editingCourseId ? "Edit Course" : "Add New Course"}
                </h4>

                {courseMessage && (
                  <div className="message-text">{courseMessage}</div>
                )}

                <div className="form-grid">
                  <input
                    className="input"
                    placeholder="Course title (e.g., Software Engineering)"
                    value={courseTitle}
                    onChange={(e) => setCourseTitle(e.target.value)}
                  />

                  <input
                    className="input"
                    placeholder="Course code (e.g., SE01)"
                    value={courseCode}
                    onChange={(e) => setCourseCode(e.target.value)}
                  />

                  <textarea
                    className="textarea"
                    placeholder="Description (optional)"
                    value={courseDescription}
                    onChange={(e) => setCourseDescription(e.target.value)}
                  />

                  <select
                    className="select"
                    value={selectedDeptId}
                    onChange={(e) => setSelectedDeptId(e.target.value)}
                  >
                    <option value="">Select department</option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name}
                      </option>
                    ))}
                  </select>

                  <select
                    className="select"
                    value={selectedTeacherId}
                    onChange={(e) => setSelectedTeacherId(e.target.value)}
                  >
                    <option value="">Assign teacher (optional)</option>
                    {availableTeachers.map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.fullName} - {t.email}
                      </option>
                    ))}
                  </select>

                  <div className="form-row-2">
                    <input
                      className="input"
                      type="number"
                      min="1"
                      placeholder="Credit value"
                      value={creditValue}
                      onChange={(e) => setCreditValue(e.target.value)}
                    />

                    <input
                      className="input"
                      type="number"
                      min="1"
                      max="8"
                      placeholder="Semester"
                      value={semester}
                      onChange={(e) => setSemester(e.target.value)}
                    />
                  </div>

                  <input
                    className="input"
                    placeholder="Academic year (e.g., 2025)"
                    value={academicYear}
                    onChange={(e) => setAcademicYear(e.target.value)}
                  />

                  {editingCourseId && (
                    <select
                      className="select"
                      value={courseStatus}
                      onChange={(e) => setCourseStatus(e.target.value)}
                    >
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="INACTIVE">INACTIVE</option>
                    </select>
                  )}

                  <div className="action-row">
                    <button className="primary-btn" onClick={createCourse}>
                      <PlusCircle size={17} />
                      {editingCourseId ? "Update Course" : "Add Course"}
                    </button>

                    {editingCourseId && (
                      <button
                        type="button"
                        className="secondary-btn"
                        onClick={resetCourseForm}
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </div>
              </div>

              <div className="list-panel">
                <h4 className="panel-heading">Course List</h4>

                {courses.length === 0 ? (
                  <div className="empty-state">
                    <BookOpen size={40} style={{ opacity: 0.5 }} />
                    <div>No courses added yet.</div>
                  </div>
                ) : (
                  <div className="list-panel-inner">
                    {courses.map((c) => (
                      <div className="list-item" key={c.id}>
                        <div>
                          <div className="list-item-title">
                            {c.courseCode} — {c.title}
                          </div>

                          <div className="muted">
                            Department: {c.departmentName || "-"} | Credits:{" "}
                            {c.creditValue} | Semester: {c.semester} | Year:{" "}
                            {c.academicYear}
                          </div>

                          <div className="muted">
                            Teacher: {c.teacherName || "Not assigned"}
                          </div>

                          <div
                            className={`badge ${
                              (c.status || "ACTIVE") === "ACTIVE"
                                ? "active"
                                : "inactive"
                            }`}
                          >
                            {c.status || "ACTIVE"}
                          </div>

                          <div className="muted" style={{ marginTop: "8px" }}>
                            {c.description || "No description provided"}
                          </div>
                        </div>

                        <div className="item-actions">
                          <button
                            type="button"
                            className="edit-btn"
                            onClick={() => editCourse(c)}
                          >
                            <Pencil size={15} />
                            Edit
                          </button>

                          <button
                            type="button"
                            className="danger-btn"
                            onClick={() => deleteCourse(c.id)}
                          >
                            <Trash2 size={15} />
                            Delete
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {activeTab === "PREDICTIONS" && (
  <div className="content-card">
    <div className="section-header">
      <h3 className="section-title">
        <Brain size={19} />
        Dropout Prediction Analytics
      </h3>
      <span className="section-subtitle">
        Run, save, and monitor student dropout risk predictions
      </span>
    </div>

    <div className="overview-grid" style={{ marginBottom: "22px" }}>
      <div className="stat-card">
        <div className="stat-top">
          <div>
            <div className="stat-title">Total Predictions</div>
            <div className="stat-value">
              {predictionSummary.totalPredictions}
            </div>
          </div>
          <div className="stat-icon">
            <Users size={20} />
          </div>
        </div>
      </div>

      <div className="stat-card">
        <div className="stat-top">
          <div>
            <div className="stat-title">High Risk</div>
            <div className="stat-value">
              {predictionSummary.highRiskCount}
            </div>
          </div>
          <div className="stat-icon">
            <ShieldAlert size={20} />
          </div>
        </div>
      </div>

      <div className="stat-card">
        <div className="stat-top">
          <div>
            <div className="stat-title">Medium Risk</div>
            <div className="stat-value">
              {predictionSummary.mediumRiskCount}
            </div>
          </div>
          <div className="stat-icon">
            <AlertTriangle size={20} />
          </div>
        </div>
      </div>

      <div className="stat-card">
        <div className="stat-top">
          <div>
            <div className="stat-title">Low Risk</div>
            <div className="stat-value">
              {predictionSummary.lowRiskCount}
            </div>
          </div>
          <div className="stat-icon">
            <CheckCircle2 size={20} />
          </div>
        </div>
      </div>
    </div>

    <div className="chart-box">
      <h4 className="panel-heading">Risk Distribution Chart</h4>
      {renderRiskRows([
        { label: "High", value: predictionSummary.highRiskCount || 0 },
        { label: "Medium", value: predictionSummary.mediumRiskCount || 0 },
        { label: "Low", value: predictionSummary.lowRiskCount || 0 }
      ])}
    </div>

    <div className="management-grid">
      <div className="form-panel">
        <h4 className="panel-heading">Run Prediction</h4>

        <div className="info-box">
          This analysis uses the deployed XGBoost model with live behavioral learning features from the system.
        </div>

        {predictionMessage && (
          <div className="message-text">{predictionMessage}</div>
        )}

        <div className="form-grid">
          <select
            className="select"
            value={predictionCourseId}
            onChange={(e) => setPredictionCourseId(e.target.value)}
          >
            <option value="">Select course</option>
            {courses.map((c) => (
              <option key={c.id} value={c.id}>
                {c.courseCode} - {c.title}
              </option>
            ))}
          </select>

          <select
            className="select"
            value={predictionStudentId}
            onChange={(e) => setPredictionStudentId(e.target.value)}
          >
            <option value="">Select student</option>
            {predictionStudents.map((s) => (
              <option key={s.id} value={s.id}>
                {s.fullName} {s.studentCode ? `(${s.studentCode})` : ""}
              </option>
            ))}
          </select>

          <select
            className="select"
            value={predictionWindowDays}
            onChange={(e) => setPredictionWindowDays(Number(e.target.value))}
          >
            <option value={7}>Last 7 days</option>
            <option value={14}>Last 14 days</option>
            <option value={30}>Last 30 days</option>
          </select>

          <div className="action-row">
            <button
              className="primary-btn"
              onClick={handleAdminRunPrediction}
              disabled={predictionLoading}
            >
              <Brain size={16} />
              {predictionLoading ? "Running..." : "Run & Save Prediction"}
            </button>

            <button
              type="button"
              className="advanced-toggle"
              onClick={() => setShowPredictionAdvanced((prev) => !prev)}
            >
              {showPredictionAdvanced
                ? "Hide Advanced Options"
                : "Show Advanced Options"}
            </button>
          </div>
        </div>

        {showPredictionAdvanced && (
          <div className="advanced-panel">
            <textarea
              className="textarea"
              value={predictionFeaturesText}
              onChange={(e) => setPredictionFeaturesText(e.target.value)}
              placeholder={`Optional JSON, for example:
{
  "days_since_last_login": 10,
  "material_views_count_window": 2
}`}
            />
          </div>
        )}

        {predictionResult && (
          <div className="prediction-result-card">
            <div className="prediction-result-header">
              <div>
                <h4 className="prediction-result-title">
                  Latest Prediction Result
                </h4>
                <div className="prediction-result-subtitle">
                  {predictionResult.studentName} • {predictionResult.courseCode} -{" "}
                  {predictionResult.courseTitle}
                </div>
              </div>

              <div
                className={`badge ${getRiskBadgeClass(
                  predictionResult.riskLevel
                )}`}
              >
                {predictionResult.riskLevel} RISK
              </div>
            </div>

            <div className="prediction-probability-box">
              <div className="prediction-probability-top">
                <div className="muted">Dropout Probability</div>
                <div className="prediction-probability-value">
                  {getProbabilityPercent(predictionResult.dropoutProbability)}%
                </div>
              </div>

              <div className="prediction-progress">
                <div
                  className={`prediction-progress-fill ${
                    predictionResult.riskLevel === "HIGH"
                      ? "high"
                      : predictionResult.riskLevel === "MEDIUM"
                      ? "medium"
                      : "low"
                  }`}
                  style={{
                    width: `${getProbabilityPercent(
                      predictionResult.dropoutProbability
                    )}%`
                  }}
                />
              </div>
            </div>

            <div className="prediction-metric-grid">
              <div className="prediction-metric-card">
                <div className="prediction-metric-label">Predicted Label</div>
                <div className="prediction-metric-value">
                  {predictionResult.predictedLabel || "-"}
                </div>
              </div>

              <div className="prediction-metric-card">
                <div className="prediction-metric-label">Threshold</div>
                <div className="prediction-metric-value">
                  {predictionResult.threshold ?? "-"}
                </div>
              </div>

              <div className="prediction-metric-card">
                <div className="prediction-metric-label">Generated At</div>
                <div className="prediction-metric-value">
                  {formatTimestamp(predictionResult.predictedAt)}
                </div>
              </div>

              <div className="prediction-metric-card">
                <div className="prediction-metric-label">Model</div>
                <div className="prediction-metric-value">
                  {predictionResult.modelName || "XGBoost"}
                </div>
              </div>

              <div className="prediction-metric-card">
                <div className="prediction-metric-label">Scoring Mode</div>
                <div className="prediction-metric-value">
                  {predictionResult.scoringMode || "-"}
                </div>
              </div>

              <div className="prediction-metric-card">
                <div className="prediction-metric-label">Student Email</div>
                <div className="prediction-metric-value">
                  {predictionResult.studentEmail || "-"}
                </div>
              </div>
            </div>

            <div className="prediction-interpretation">
              <strong>Interpretation:</strong>{" "}
              {getRiskMessage(predictionResult.riskLevel)}
            </div>
          </div>
        )}
      </div>

      <div className="list-panel">
        <h4 className="panel-heading">All Saved Predictions</h4>

        {allPredictions.length === 0 ? (
          <div className="empty-state">
            <Brain size={40} style={{ opacity: 0.5 }} />
            <div>No saved predictions yet.</div>
          </div>
        ) : (
          <div className="list-panel-inner">
            {allPredictions.map((item, index) => (
              <div
                className="list-item"
                key={`${item.studentId}-${item.courseId}-${index}`}
              >
                <div>
                  <div className="list-item-title">{item.studentName}</div>
                  <div className="muted">{item.studentEmail}</div>
                  <div className="muted">
                    {item.courseCode} — {item.courseTitle}
                  </div>
                  <div className="muted">
                    Probability: {item.dropoutProbability} | Label:{" "}
                    {item.predictedLabel}
                  </div>
                  <div className="muted">
                    Model: {item.modelName || "XGBoost"} | Scoring:{" "}
                    {item.scoringMode || "-"}
                  </div>
                  <div className="muted">
                    Predicted At: {formatTimestamp(item.predictedAt)}
                  </div>
                </div>

                <div className="item-actions">
                  <div className={`badge ${getRiskBadgeClass(item.riskLevel)}`}>
                    {item.riskLevel}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  </div>
)}

        {activeTab === "INSTITUTION" && (
          <div className="content-card">
            <div className="section-header">
              <h3 className="section-title">
                <Settings size={19} />
                Institution Settings
              </h3>
              <span className="section-subtitle">
                Maintain core institutional profile and academic context
              </span>
            </div>

            <div className="management-grid">
              <div className="form-panel">
                <h4 className="panel-heading">Edit Institution Details</h4>

                {institutionMessage && (
                  <div className="message-text">{institutionMessage}</div>
                )}

                {institutionLoading ? (
                  <div className="muted">Loading institution settings...</div>
                ) : (
                  <div className="form-grid">
                    <input
                      className="input"
                      placeholder="Institution name"
                      value={institutionSettings.institutionName || ""}
                      onChange={(e) =>
                        setInstitutionSettings((prev) => ({
                          ...prev,
                          institutionName: e.target.value
                        }))
                      }
                    />

                    <input
                      className="input"
                      placeholder="Institution code"
                      value={institutionSettings.institutionCode || ""}
                      onChange={(e) =>
                        setInstitutionSettings((prev) => ({
                          ...prev,
                          institutionCode: e.target.value
                        }))
                      }
                    />

                    <div className="form-row-2">
                      <input
                        className="input"
                        placeholder="Contact email"
                        value={institutionSettings.contactEmail || ""}
                        onChange={(e) =>
                          setInstitutionSettings((prev) => ({
                            ...prev,
                            contactEmail: e.target.value
                          }))
                        }
                      />

                      <input
                        className="input"
                        placeholder="Support email"
                        value={institutionSettings.supportEmail || ""}
                        onChange={(e) =>
                          setInstitutionSettings((prev) => ({
                            ...prev,
                            supportEmail: e.target.value
                          }))
                        }
                      />
                    </div>

                    <input
                      className="input"
                      placeholder="Contact number"
                      value={institutionSettings.contactNumber || ""}
                      onChange={(e) =>
                        setInstitutionSettings((prev) => ({
                          ...prev,
                          contactNumber: e.target.value
                        }))
                      }
                    />

                    <input
                      className="input"
                      placeholder="Address"
                      value={institutionSettings.address || ""}
                      onChange={(e) =>
                        setInstitutionSettings((prev) => ({
                          ...prev,
                          address: e.target.value
                        }))
                      }
                    />

                    <div className="form-row-2">
                      <input
                        className="input"
                        placeholder="Academic year"
                        value={institutionSettings.academicYear || ""}
                        onChange={(e) =>
                          setInstitutionSettings((prev) => ({
                            ...prev,
                            academicYear: e.target.value
                          }))
                        }
                      />

                      <input
                        className="input"
                        placeholder="Current semester"
                        value={institutionSettings.currentSemester || ""}
                        onChange={(e) =>
                          setInstitutionSettings((prev) => ({
                            ...prev,
                            currentSemester: e.target.value
                          }))
                        }
                      />
                    </div>

                    <div className="form-row-2">
                      <input
                        className="input"
                        placeholder="Website URL"
                        value={institutionSettings.websiteUrl || ""}
                        onChange={(e) =>
                          setInstitutionSettings((prev) => ({
                            ...prev,
                            websiteUrl: e.target.value
                          }))
                        }
                      />

                      <input
                        className="input"
                        placeholder="Logo URL"
                        value={institutionSettings.logoUrl || ""}
                        onChange={(e) =>
                          setInstitutionSettings((prev) => ({
                            ...prev,
                            logoUrl: e.target.value
                          }))
                        }
                      />
                    </div>

                    <input
                      className="input"
                      placeholder="Timezone"
                      value={institutionSettings.timezone || ""}
                      onChange={(e) =>
                        setInstitutionSettings((prev) => ({
                          ...prev,
                          timezone: e.target.value
                        }))
                      }
                    />

                    <textarea
                      className="textarea"
                      placeholder="Institution description / about"
                      value={institutionSettings.description || ""}
                      onChange={(e) =>
                        setInstitutionSettings((prev) => ({
                          ...prev,
                          description: e.target.value
                        }))
                      }
                    />

                    <div className="action-row">
                      <button
                        className="primary-btn"
                        onClick={saveInstitutionSettings}
                      >
                        <CheckCircle size={16} />
                        Save Settings
                      </button>

                      <button
                        type="button"
                        className="secondary-btn"
                        onClick={loadInstitutionSettings}
                      >
                        Reload
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="list-panel">
                <h4 className="panel-heading">Institution Preview</h4>

                <div className="list-panel-inner">
                  <div className="list-item">
                    <div>
                      <div className="list-item-title">
                        {institutionSettings.institutionName || "Institution Name"}
                      </div>
                      <div className="muted">
                        Code: {institutionSettings.institutionCode || "-"}
                      </div>
                      <div className="muted">
                        Contact: {institutionSettings.contactEmail || "-"}
                      </div>
                      <div className="muted">
                        Support: {institutionSettings.supportEmail || "-"}
                      </div>
                      <div className="muted">
                        Phone: {institutionSettings.contactNumber || "-"}
                      </div>
                      <div className="muted">
                        Address: {institutionSettings.address || "-"}
                      </div>
                      <div className="muted">
                        Academic Year: {institutionSettings.academicYear || "-"} |
                        Semester: {institutionSettings.currentSemester || "-"}
                      </div>
                      <div className="muted">
                        Website: {institutionSettings.websiteUrl || "-"}
                      </div>
                      <div className="muted">
                        Timezone: {institutionSettings.timezone || "-"}
                      </div>
                      <div className="muted" style={{ marginTop: "8px" }}>
                        {institutionSettings.description ||
                          "No institution description added yet."}
                      </div>
                    </div>
                  </div>

                  <div className="info-box">
                    Last Updated:{" "}
                    <strong>{formatTimestamp(institutionSettings.updatedAt)}</strong>
                    <br />
                    Updated By:{" "}
                    <strong>{institutionSettings.updatedByName || "-"}</strong>
                  </div>

                  {institutionSettings.logoUrl && (
                    <div className="list-item">
                      <div style={{ width: "100%" }}>
                        <div className="list-item-title">Logo Preview</div>
                        <img
                          src={institutionSettings.logoUrl}
                          alt="Institution Logo"
                          className="logo-preview"
                        />
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default AdminDashboard;