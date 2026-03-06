import { Navigate } from "react-router-dom";

/* 🔹 Decode role from JWT */
function getUserRole() {
  const token = localStorage.getItem("token");
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.role;
  } catch {
    return null;
  }
}

/* 🔹 Check if token expired */
function isTokenExpired() {
  const token = localStorage.getItem("token");
  if (!token) return true;

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    const now = Date.now() / 1000; // seconds
    return payload.exp < now;
  } catch {
    return true;
  }
}

/* 🔹 Protected Route */
function ProtectedRoute({ children, allowedRoles }) {
  const token = localStorage.getItem("token");

  if (!token || isTokenExpired()) {
    localStorage.removeItem("token"); // auto logout
    return <Navigate to="/login" />;
  }

  const role = getUserRole();

  if (!allowedRoles.includes(role)) {
  if (role === "ADMIN") return <Navigate to="/admin" replace />;
  if (role === "TEACHER") return <Navigate to="/teacher" replace />;
  if (role === "STUDENT") return <Navigate to="/student" replace />;

  // fallback
  localStorage.removeItem("token");
  return <Navigate to="/login" replace />;
}


  return children;
}

export default ProtectedRoute;
