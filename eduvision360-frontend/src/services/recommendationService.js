import axios from "axios";

const API_BASE = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

function authHeaders() {
  const token = localStorage.getItem("token");
  if (!token) return {};
  return {
    Authorization: `Bearer ${token}`
  };
}

export const getMyRecommendations = () =>
  axios.get(`${API_BASE}/api/recommendations/student/my`, {
    headers: authHeaders()
  });