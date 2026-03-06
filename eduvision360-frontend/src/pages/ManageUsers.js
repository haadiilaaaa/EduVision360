import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import Menu from "../components/Menu";
import { Search, Power, Trash2, RefreshCcw } from "lucide-react";

function ManageUsers() {
  const token = localStorage.getItem("token");

  const axiosAuth = useMemo(() => {
    return axios.create({
      baseURL: "http://localhost:8080",
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
  }, [token]);

  const [users, setUsers] = useState([]);
  const [roleFilter, setRoleFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [search, setSearch] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const loadUsers = async (overrideFilters = null) => {
    setLoading(true);
    setMessage("");

    try {
      const filters = overrideFilters || {
        roleFilter,
        statusFilter,
        search
      };

      const params = {};

      if (filters.roleFilter) params.role = filters.roleFilter;
      if (filters.statusFilter) params.status = filters.statusFilter;
      if (filters.search?.trim()) params.search = filters.search.trim();

      const res = await axiosAuth.get("/api/admin/users", { params });
      setUsers(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.log("Failed to load users", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to load users");
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) return;
    loadUsers();
    // eslint-disable-next-line
  }, [token]);

  const toggleStatus = async (user) => {
    const nextStatus = user.status === "DISABLED" ? "ACTIVE" : "DISABLED";

    try {
      await axiosAuth.patch(`/api/admin/users/${user.id}/status`, null, {
        params: { status: nextStatus }
      });

      setMessage(`User status updated to ${nextStatus}`);
      loadUsers();
    } catch (err) {
      console.log("Failed to update user status", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to update user status");
    }
  };

  const deleteUser = async (user) => {
    const confirmed = window.confirm(`Are you sure you want to delete ${user.fullName}?`);
    if (!confirmed) return;

    try {
      await axiosAuth.delete(`/api/admin/users/${user.id}`);
      setMessage("User deleted successfully");
      loadUsers();
    } catch (err) {
      console.log("Failed to delete user", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to delete user");
    }
  };

  const handleReset = () => {
    const cleared = {
      roleFilter: "",
      statusFilter: "",
      search: ""
    };

    setRoleFilter("");
    setStatusFilter("");
    setSearch("");
    setMessage("");
    loadUsers(cleared);
  };

  return (
    <div className="users-layout">
      <style>{`
        .users-layout {
          display: flex;
          min-height: 100vh;
          background: var(--bg-main);
          font-family: 'Inter', sans-serif;
        }

        .main-content {
          flex: 1;
          padding: 40px;
          color: var(--text-main);
        }

        .page-header {
          margin-bottom: 24px;
        }

        .page-header h2 {
          margin: 0 0 10px 0;
          font-size: 32px;
          background: linear-gradient(to right, var(--text-main), var(--accent), var(--text-main));
          background-size: 200% auto;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .page-header p {
          color: var(--text-muted);
          margin: 0;
        }

        .panel {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 20px;
          padding: 22px;
          backdrop-filter: blur(14px);
        }

        .filters {
          display: flex;
          gap: 12px;
          flex-wrap: wrap;
          margin-bottom: 20px;
        }

        .input, .select {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
          padding: 12px 14px;
          border-radius: 14px;
          outline: none;
          min-width: 220px;
        }

        .input::placeholder {
          color: var(--text-muted);
        }

        .primary-btn, .secondary-btn, .danger-btn {
          padding: 12px 16px;
          border-radius: 14px;
          cursor: pointer;
          font-weight: 700;
          display: flex;
          align-items: center;
          gap: 8px;
          border: none;
        }

        .primary-btn {
          background: var(--accent-soft);
          border: 1px solid rgba(255, 213, 72, 0.35);
          color: var(--accent);
        }

        .secondary-btn {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
        }

        .danger-btn {
          background: rgba(248,113,113,0.08);
          border: 1px solid rgba(248,113,113,0.25);
          color: var(--danger);
        }

        .message-text {
          color: var(--accent);
          margin-bottom: 14px;
          font-size: 14px;
          font-weight: 600;
        }

        .user-card {
          display: flex;
          justify-content: space-between;
          gap: 20px;
          align-items: center;
          padding: 16px;
          border-radius: 16px;
          border: 1px solid var(--border-soft);
          background: var(--bg-card-2);
          margin-top: 12px;
        }

        .user-left {
          flex: 1;
        }

        .user-name {
          font-weight: 800;
          font-size: 16px;
          margin-bottom: 6px;
          color: var(--text-main);
        }

        .user-meta {
          color: var(--text-muted);
          font-size: 13px;
          line-height: 1.7;
        }

        .status-badge {
          display: inline-block;
          margin-top: 8px;
          padding: 6px 10px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 700;
        }

        .status-ACTIVE {
          background: rgba(74, 222, 128, 0.1);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.3);
        }

        .status-DISABLED {
          background: rgba(248, 113, 113, 0.08);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .status-PENDING_APPROVAL, .status-PENDING_VERIFICATION {
          background: rgba(255, 213, 72, 0.12);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.35);
        }

        .actions {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .empty-state {
          color: var(--text-muted);
          padding: 20px 0;
        }

        @media (max-width: 900px) {
          .user-card {
            flex-direction: column;
            align-items: flex-start;
          }

          .actions {
            justify-content: flex-start;
          }
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <div className="page-header">
          <h2>Manage Users</h2>
          <p>View, search, filter, activate, deactivate, and delete users.</p>
        </div>

        <div className="panel">
          {message && <div className="message-text">{message}</div>}

          <div className="filters">
            <select
              className="select"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
            >
              <option value="">All Roles</option>
              <option value="ADMIN">ADMIN</option>
              <option value="TEACHER">TEACHER</option>
              <option value="STUDENT">STUDENT</option>
            </select>

            <select
              className="select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="DISABLED">DISABLED</option>
              <option value="PENDING_APPROVAL">PENDING_APPROVAL</option>
              <option value="PENDING_VERIFICATION">PENDING_VERIFICATION</option>
            </select>

            <input
              className="input"
              placeholder="Search by name, email, username, or student code"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />

            <button className="primary-btn" onClick={() => loadUsers()}>
              <Search size={16} /> Search
            </button>

            <button className="secondary-btn" onClick={handleReset}>
              <RefreshCcw size={16} /> Reset
            </button>
          </div>

          {loading ? (
            <div className="empty-state">Loading users...</div>
          ) : users.length === 0 ? (
            <div className="empty-state">No users found.</div>
          ) : (
            users.map((user) => (
              <div className="user-card" key={user.id}>
                <div className="user-left">
                  <div className="user-name">{user.fullName}</div>
                  <div className="user-meta">
                    <div><strong>Email:</strong> {user.email}</div>
                    <div><strong>Username:</strong> {user.username}</div>
                    <div><strong>Role:</strong> {user.role}</div>
                    <div><strong>Contact:</strong> {user.contactNumber || "-"}</div>
                    {user.studentCode && <div><strong>Student Code:</strong> {user.studentCode}</div>}
                  </div>

                  <div className={`status-badge status-${user.status}`}>
                    {user.status}
                  </div>
                </div>

                <div className="actions">
                  {user.role !== "ADMIN" && (
                    <>
                      <button
                        className="secondary-btn"
                        onClick={() => toggleStatus(user)}
                      >
                        <Power size={16} />
                        {user.status === "DISABLED" ? "Activate" : "Disable"}
                      </button>

                      <button
                        className="danger-btn"
                        onClick={() => deleteUser(user)}
                      >
                        <Trash2 size={16} /> Delete
                      </button>
                    </>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </main>
    </div>
  );
}

export default ManageUsers;