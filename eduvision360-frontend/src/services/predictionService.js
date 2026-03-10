import axios from "axios";

const API_BASE = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

function authHeaders() {
  const token = localStorage.getItem("token");
  if (!token) return {};
  return {
    Authorization: `Bearer ${token}`
  };
}

export const getStudentsByCourse = (courseId) =>
  axios.get(`${API_BASE}/api/predictions/courses/${courseId}/students`, {
    headers: authHeaders()
  });

export const runDropoutPrediction = (payload) =>
  axios.post(`${API_BASE}/api/predictions/dropout/run`, payload, {
    headers: authHeaders()
  });

export const getTeacherPredictionSummary = () =>
  axios.get(`${API_BASE}/api/predictions/teacher/summary`, {
    headers: authHeaders()
  });

export const getAdminPredictionSummary = () =>
  axios.get(`${API_BASE}/api/predictions/admin/summary`, {
    headers: authHeaders()
  });

export const getMyLatestStudentPrediction = () =>
  axios.get(`${API_BASE}/api/predictions/student/my-latest`, {
    headers: authHeaders()
  });

export const getMyAllStudentPredictions = () =>
  axios.get(`${API_BASE}/api/predictions/student/my-all`, {
    headers: authHeaders()
  });

export const getTeacherPredictionsByCourse = (courseId) =>
  axios.get(`${API_BASE}/api/predictions/teacher/course/${courseId}`, {
    headers: authHeaders()
  });

export const getAllAdminPredictions = () =>
  axios.get(`${API_BASE}/api/predictions/admin/all`, {
    headers: authHeaders()
  });