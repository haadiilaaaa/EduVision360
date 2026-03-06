import React, { useEffect, useRef, useState } from "react";
import { logout, getDisplayName, getUserInitial, getUserRole } from "../utils/auth";
import { useNavigate, useLocation } from "react-router-dom";
import { useTheme } from "../context/ThemeContext";

import {
  LayoutDashboard,
  BookOpen,
  Cpu,
  ScanFace,
  CalendarCheck,
  History,
  BarChart3,
  MessageSquare,
  Users,
  LogOut,
  ShieldCheck,
  PanelLeftClose,
  Megaphone,
  User,
  Sun,
  Moon,
  Bell,
  CalendarDays,
  CheckCheck
} from "lucide-react";

import {
  getMyNotifications,
  getMyUnreadCount,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from "../services/academicService";

// ✅ fallback if actionUrl is missing
const notificationFallbackUrl = (role, type) => {
  const map = {
    CLASS_SESSION_CREATED: role === "TEACHER" ? "/teacher/sessions" : "/student",
    CLASS_SESSION_OPENED: "/student",
    CLASS_SESSION_COMPLETED: role === "TEACHER" ? "/teacher/sessions" : "/student/attendance-history",
    ATTENDANCE_MARKED: "/student/attendance-history",
    ATTENDANCE_MISSED: "/student/attendance-history",

    MATERIAL_UPLOADED: role === "TEACHER" ? "/teacher/materials" : "/student/materials",
    ANNOUNCEMENT_POSTED: role === "TEACHER" ? "/teacher/announcements" : "/student/announcements",

    DROPOUT_HIGH_RISK: role === "STUDENT" ? "/student" : "/teacher",
    DROPOUT_MEDIUM_RISK: role === "STUDENT" ? "/student" : "/teacher",

    LOW_ENGAGEMENT: role === "TEACHER" ? "/teacher" : "/student",

    SYSTEM_ALERT: role === "ADMIN" ? "/admin" : role === "TEACHER" ? "/teacher" : "/student"
  };

  return map[type] || (role === "ADMIN" ? "/admin" : role === "TEACHER" ? "/teacher" : "/student");
};

function Menu() {
  const role = getUserRole();
  const name = getDisplayName();     // ✅ from JWT (fullName if present, else email)
  const initial = getUserInitial();  // ✅ from JWT

  const navigate = useNavigate();
  const location = useLocation();
  const { theme, toggleTheme } = useTheme();

  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);

  const notificationRef = useRef(null);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const isExactActive = (path) => location.pathname === path;

  const panelTitle =
    role === "ADMIN"
      ? "Admin Panel"
      : role === "TEACHER"
      ? "Teacher Panel"
      : role === "STUDENT"
      ? "Student Panel"
      : "User Panel";

  const loadNotifications = async () => {
    try {
      const [notificationsRes, unreadRes] = await Promise.all([
        getMyNotifications(30),
        getMyUnreadCount()
      ]);

      setNotifications(notificationsRes.data || []);
      setUnreadCount(unreadRes.data?.unreadCount || 0);
    } catch (err) {
      console.error("Failed to load notifications", err);
    }
  };

  useEffect(() => {
    loadNotifications();
    const interval = setInterval(loadNotifications, 15000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setShowNotifications(false);
      }
    };

    document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, []);

  const handleMarkAsRead = async (notificationId) => {
    try {
      await markNotificationAsRead(notificationId);
      setUnreadCount((prev) => (prev > 0 ? prev - 1 : 0));
      setNotifications((prev) =>
        prev.map((n) => (n.id === notificationId ? { ...n, read: true } : n))
      );
    } catch (err) {
      console.error("Failed to mark notification as read", err);
      loadNotifications();
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllNotificationsAsRead();
      setUnreadCount(0);
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (err) {
      console.error("Failed to mark all notifications as read", err);
      loadNotifications();
    }
  };

  const formatNotificationTime = (createdAt) => {
    if (!createdAt) return "";
    try {
      return new Date(createdAt).toLocaleString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      });
    } catch {
      return createdAt;
    }
  };

  const handleNotificationClick = async (notification) => {
    if (!notification.read) {
      await handleMarkAsRead(notification.id);
    }

    const url = notification.actionUrl || notificationFallbackUrl(role, notification.type);
    if (url) navigate(url);

    setShowNotifications(false);
  };

  return (
    <>
      <style>{`
        .sidebar-shell {
          width: 280px;
          min-height: 100vh;
          position: sticky;
          top: 0;
          background: var(--bg-sidebar);
          border-right: 1px solid var(--border-soft);
          backdrop-filter: blur(18px);
          display: flex;
          flex-direction: column;
          box-shadow: 10px 0 30px rgba(0,0,0,0.18);
        }

        .sidebar-top {
          padding: 22px 18px 12px;
          border-bottom: 1px solid var(--border-soft);
        }

        .brand-box {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 18px;
        }

        .brand-icon {
          width: 42px;
          height: 42px;
          border-radius: 14px;
          background: linear-gradient(135deg, var(--accent), #f5c400);
          color: #111827;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 10px 24px rgba(255, 213, 72, 0.2);
        }

        .brand-title {
          margin: 0;
          color: var(--text-main);
          font-size: 16px;
          font-weight: 800;
        }

        .brand-subtitle {
          margin: 2px 0 0;
          color: var(--text-muted);
          font-size: 12px;
        }

        .sidebar-profile {
          padding: 14px;
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 18px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 12px;
        }

        .profile-left {
          display: flex;
          align-items: center;
          gap: 12px;
          min-width: 0;
        }

        .profile-img {
          width: 44px;
          height: 44px;
          border-radius: 14px;
          background: var(--accent-soft);
          color: var(--accent);
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 900;
          border: 1px solid rgba(255, 213, 72, 0.25);
          flex-shrink: 0;
        }

        .profile-details {
          min-width: 0;
        }

        .profile-details p {
          margin: 0;
          font-size: 13px;
          font-weight: 800;
          color: var(--text-main);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          max-width: 145px;
        }

        .profile-details span {
          font-size: 11px;
          color: var(--text-muted);
        }

        .notification-wrap {
          position: relative;
        }

        .notification-btn {
          width: 40px;
          height: 40px;
          border-radius: 14px;
          border: 1px solid var(--border-soft);
          background: var(--bg-card-2);
          color: var(--text-main);
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          position: relative;
          transition: 0.25s ease;
        }

        .notification-btn:hover {
          background: var(--accent-soft);
          color: var(--accent);
        }

        .notification-badge {
          position: absolute;
          top: -4px;
          right: -4px;
          min-width: 18px;
          height: 18px;
          padding: 0 5px;
          border-radius: 999px;
          background: var(--danger);
          color: white;
          font-size: 10px;
          font-weight: 800;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 2px solid var(--bg-card);
        }

        .notification-panel {
          position: absolute;
          top: 48px;
          right: 0;
          width: 320px;
          max-height: 420px;
          overflow-y: auto;
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 18px;
          box-shadow: var(--shadow-lg);
          z-index: 999;
          padding: 12px;
        }

        .notification-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
          padding-bottom: 10px;
          border-bottom: 1px solid var(--border-soft);
        }

        .notification-header h4 {
          margin: 0;
          color: var(--text-main);
          font-size: 14px;
        }

        .mark-all-btn {
          border: none;
          background: transparent;
          color: var(--accent);
          cursor: pointer;
          font-size: 12px;
          font-weight: 700;
          display: inline-flex;
          align-items: center;
          gap: 6px;
        }

        .notification-list {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }

        .notification-item {
          padding: 12px;
          border-radius: 14px;
          border: 1px solid var(--border-soft);
          background: var(--bg-card-2);
          cursor: pointer;
          transition: 0.2s ease;
        }

        .notification-item:hover {
          transform: translateY(-1px);
        }

        .notification-item.unread {
          border-color: rgba(255, 213, 72, 0.28);
          background: rgba(255, 213, 72, 0.08);
        }

        .notification-title {
          font-size: 13px;
          font-weight: 700;
          color: var(--text-main);
          margin-bottom: 4px;
        }

        .notification-message {
          font-size: 12px;
          color: var(--text-muted);
          line-height: 1.5;
          margin-bottom: 6px;
        }

        .notification-time {
          font-size: 11px;
          color: var(--text-dim);
        }

        .notification-empty {
          text-align: center;
          color: var(--text-muted);
          font-size: 13px;
          padding: 20px 10px;
        }

        .menu-container {
          display: flex;
          flex-direction: column;
          gap: 6px;
          padding: 14px;
          flex: 1;
        }

        .menu-label {
          font-size: 11px;
          text-transform: uppercase;
          letter-spacing: 1px;
          color: var(--text-muted);
          margin: 12px 0 6px 8px;
          font-weight: 800;
        }

        .menu-btn {
          display: flex;
          align-items: center;
          gap: 12px;
          width: 100%;
          padding: 12px 14px;
          background: transparent;
          border: none;
          border-radius: 14px;
          color: var(--text-soft);
          font-size: 14px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.25s ease;
          text-align: left;
        }

        .menu-btn:hover {
          background: var(--bg-card);
          color: var(--text-main);
          transform: translateX(3px);
        }

        .menu-btn.active {
          background: linear-gradient(135deg, rgba(255,213,72,0.18), rgba(255,213,72,0.10));
          color: var(--accent);
          border: 1px solid rgba(255,213,72,0.22);
          box-shadow: 0 10px 20px rgba(255,213,72,0.08);
        }

        .theme-toggle-wrap {
          padding: 14px;
          border-top: 1px solid var(--border-soft);
        }

        .logout-section {
          padding: 14px;
          border-top: 1px solid var(--border-soft);
        }

        @media (max-width: 960px) {
          .sidebar-shell { width: 88px; }

          .brand-title,
          .brand-subtitle,
          .profile-details,
          .menu-label,
          .menu-btn span,
          .notification-panel {
            display: none;
          }

          .brand-box { justify-content: center; }
          .sidebar-profile { justify-content: center; padding: 10px; }
          .profile-left { gap: 0; }
          .menu-btn { justify-content: center; padding: 12px; }
        }

        @media (max-width: 700px) {
          .sidebar-shell { display: none; }
        }
      `}</style>

      <aside className="sidebar-shell">
        <div className="sidebar-top">
          <div className="brand-box">
            <div className="brand-icon">
              <PanelLeftClose size={20} />
            </div>
            <div>
              <p className="brand-title">EduVision 360</p>
              <p className="brand-subtitle">{panelTitle}</p>
            </div>
          </div>

          <div className="sidebar-profile">
            <div className="profile-left">
              {/* ✅ real user initial */}
              <div className="profile-img">{initial}</div>

              <div className="profile-details">
                {/* ✅ real user name */}
                <p>{name}</p>
                <span>{role || "User"}</span>
              </div>
            </div>

            <div className="notification-wrap" ref={notificationRef}>
              <button
                className="notification-btn"
                onClick={() => setShowNotifications((prev) => !prev)}
                title="Notifications"
              >
                <Bell size={18} />
                {unreadCount > 0 && (
                  <span className="notification-badge">
                    {unreadCount > 99 ? "99+" : unreadCount}
                  </span>
                )}
              </button>

              {showNotifications && (
                <div className="notification-panel">
                  <div className="notification-header">
                    <h4>Notifications</h4>
                    {notifications.length > 0 && unreadCount > 0 && (
                      <button className="mark-all-btn" onClick={handleMarkAllAsRead}>
                        <CheckCheck size={14} />
                        Mark all
                      </button>
                    )}
                  </div>

                  {notifications.length === 0 ? (
                    <div className="notification-empty">No notifications yet.</div>
                  ) : (
                    <div className="notification-list">
                      {notifications.map((notification) => (
                        <div
                          key={notification.id}
                          className={`notification-item ${notification.read ? "" : "unread"}`}
                          onClick={() => handleNotificationClick(notification)}
                        >
                          <div className="notification-title">{notification.title}</div>
                          <div className="notification-message">{notification.message}</div>
                          <div className="notification-time">
                            {formatNotificationTime(notification.createdAt)}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="menu-container">
          <p className="menu-label">Main Navigation</p>

          {role === "STUDENT" && (
            <>
              <button className={`menu-btn ${isExactActive("/student") ? "active" : ""}`} onClick={() => navigate("/student")}>
                <LayoutDashboard size={18} /><span>Dashboard</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/profile") ? "active" : ""}`} onClick={() => navigate("/student/profile")}>
                <User size={18} /><span>Profile</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/materials") ? "active" : ""}`} onClick={() => navigate("/student/materials")}>
                <BookOpen size={18} /><span>Learning Materials</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/announcements") ? "active" : ""}`} onClick={() => navigate("/student/announcements")}>
                <Megaphone size={18} /><span>Announcements</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/ai-tutor") ? "active" : ""}`} onClick={() => navigate("/student/ai-tutor")}>
                <Cpu size={18} /><span>AI Tutor</span>
              </button>

              <p className="menu-label">Identity & Attendance</p>

              <button className={`menu-btn ${isExactActive("/student/register-face") ? "active" : ""}`} onClick={() => navigate("/student/register-face")}>
                <ScanFace size={18} /><span>Register Face</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/feedback") ? "active" : ""}`} onClick={() => navigate("/student/feedback")}>
                <MessageSquare size={18} /><span>Feedback</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/courses") ? "active" : ""}`} onClick={() => navigate("/student/courses")}>
                <BookOpen size={18} /><span>Available Courses</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/my-courses") ? "active" : ""}`} onClick={() => navigate("/student/my-courses")}>
                <BookOpen size={18} /><span>My Courses</span>
              </button>

              <button className={`menu-btn ${isExactActive("/student/attendance-history") ? "active" : ""}`} onClick={() => navigate("/student/attendance-history")}>
                <History size={18} /><span>Attendance History</span>
              </button>

              <button className="menu-btn">
                <BarChart3 size={18} /><span>Reports</span>
              </button>
            </>
          )}

          {role === "TEACHER" && (
            <>
              <p className="menu-label">Teacher Panel</p>

              <button className={`menu-btn ${isExactActive("/teacher") ? "active" : ""}`} onClick={() => navigate("/teacher")}>
                <LayoutDashboard size={18} /><span>Dashboard</span>
              </button>

              <button className={`menu-btn ${isExactActive("/teacher/profile") ? "active" : ""}`} onClick={() => navigate("/teacher/profile")}>
                <User size={18} /><span>Profile</span>
              </button>

              <button className={`menu-btn ${isExactActive("/teacher/courses") ? "active" : ""}`} onClick={() => navigate("/teacher/courses")}>
                <BookOpen size={18} /><span>My Courses</span>
              </button>

              <button className={`menu-btn ${isExactActive("/teacher/sessions") ? "active" : ""}`} onClick={() => navigate("/teacher/sessions")}>
                <CalendarDays size={18} /><span>Manage Sessions</span>
              </button>

              <button className={`menu-btn ${isExactActive("/teacher/materials") ? "active" : ""}`} onClick={() => navigate("/teacher/materials")}>
                <BookOpen size={18} /><span>Learning Materials</span>
              </button>

              <button className={`menu-btn ${isExactActive("/teacher/announcements") ? "active" : ""}`} onClick={() => navigate("/teacher/announcements")}>
                <Megaphone size={18} /><span>Announcements</span>
              </button>

              <button className={`menu-btn ${isExactActive("/teacher/feedback") ? "active" : ""}`} onClick={() => navigate("/teacher/feedback")}>
                <MessageSquare size={18} /><span>Feedback Review</span>
              </button>

              <button className={`menu-btn ${isExactActive("/teacher/attendance") ? "active" : ""}`} onClick={() => navigate("/teacher/attendance")}>
                <CalendarCheck size={18} /><span>View Attendance</span>
              </button>
            </>
          )}

          {role === "ADMIN" && (
            <>
              <p className="menu-label">Administration</p>

              <button className={`menu-btn ${isExactActive("/admin") ? "active" : ""}`} onClick={() => navigate("/admin")}>
                <LayoutDashboard size={18} /><span>Dashboard</span>
              </button>

              <button className={`menu-btn ${isExactActive("/admin/profile") ? "active" : ""}`} onClick={() => navigate("/admin/profile")}>
                <User size={18} /><span>Profile</span>
              </button>

              <button className={`menu-btn ${isExactActive("/admin/attendance") ? "active" : ""}`} onClick={() => navigate("/admin/attendance")}>
                <CalendarCheck size={18} /><span>View Attendance</span>
              </button>

              <button className={`menu-btn ${isExactActive("/admin/users") ? "active" : ""}`} onClick={() => navigate("/admin/users")}>
                <Users size={18} /><span>Manage Users</span>
              </button>

              <button className={`menu-btn ${isExactActive("/admin/feedback") ? "active" : ""}`} onClick={() => navigate("/admin/feedback")}>
                <MessageSquare size={18} /><span>Feedback Management</span>
              </button>

              <button className="menu-btn active" style={{ display: "none" }}>
                <ShieldCheck size={18} /><span>Admin</span>
              </button>
            </>
          )}
        </div>

        <div className="theme-toggle-wrap">
          <button className="menu-btn" onClick={toggleTheme}>
            {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
            <span>{theme === "dark" ? "Light Mode" : "Dark Mode"}</span>
          </button>
        </div>

        <div className="logout-section">
          <button className="menu-btn" onClick={handleLogout} style={{ color: "var(--danger)" }}>
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>
    </>
  );
}

export default Menu;