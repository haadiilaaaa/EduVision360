// src/utils/auth.js

function parsePayload(token) {
  if (!token) return null;
  try {
    const part = token.split(".")[1];
    if (!part) return null;

    const base64 = part.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(escape(atob(base64))); // ✅ UTF-8 safe
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function logout() {
  localStorage.removeItem("token");
}

export function isTokenExpired() {
  const payload = parsePayload(localStorage.getItem("token"));
  if (!payload?.exp) return true;
  return payload.exp < Date.now() / 1000;
}

export function getUserRole() {
  const payload = parsePayload(localStorage.getItem("token"));
  return payload?.role || null;
}

export function getUserEmail() {
  const payload = parsePayload(localStorage.getItem("token"));
  return payload?.email || null;
}

export function getUserFullName() {
  const payload = parsePayload(localStorage.getItem("token"));
  return payload?.fullName || null;
}

export function getUserId() {
  const payload = parsePayload(localStorage.getItem("token"));
  return payload?.sub || null;
}

export function getDisplayName() {
  const fullName = (getUserFullName() || "").trim();
  const email = getUserEmail();
  return fullName ? fullName : (email || "System User");
}

export function getUserInitial() {
  const fullName = (getUserFullName() || "").trim();
  const email = (getUserEmail() || "").trim();
  const source = fullName || email;
  return source ? source.charAt(0).toUpperCase() : "?";
}