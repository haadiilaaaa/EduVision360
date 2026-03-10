import axios from "axios";

const API_BASE = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

function authHeaders() {
  const token = localStorage.getItem("token");
  if (!token) return {};
  return {
    Authorization: `Bearer ${token}`
  };
}

export const runProgressAnalytics = (payload) =>
  axios.post(`${API_BASE}/api/progress-analytics/run`, payload, {
    headers: authHeaders()
  });

export const getMyProgressAnalytics = () =>
  axios.get(`${API_BASE}/api/progress-analytics/student/my`, {
    headers: authHeaders()
  });

export const getTeacherCourseProgress = (courseId) =>
  axios.get(`${API_BASE}/api/progress-analytics/teacher/course/${courseId}`, {
    headers: authHeaders()
  });

export const getAllAdminProgressAnalytics = () =>
  axios.get(`${API_BASE}/api/progress-analytics/admin/all`, {
    headers: authHeaders()
  });