import React, { useEffect, useState } from "react";
import {
  createDepartment,
  deleteDepartment,
  getDepartments,
  updateDepartment,
} from "../services/academicService";

function AdminDepartments() {
  const [departments, setDepartments] = useState([]);
  const [message, setMessage] = useState("");
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    code: "",
    name: "",
    description: "",
  });

  const loadDepartments = async () => {
    try {
      const res = await getDepartments();
      setDepartments(res.data || []);
    } catch (err) {
      console.log("Failed to load departments", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to load departments");
    }
  };

  useEffect(() => {
    loadDepartments();
  }, []);

  const resetForm = () => {
    setForm({
      code: "",
      name: "",
      description: "",
    });
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");

    if (!form.code.trim() || !form.name.trim()) {
      setMessage("Department code and name are required");
      return;
    }

    const payload = {
      code: form.code.trim().toUpperCase(),
      name: form.name.trim(),
      description: form.description.trim(),
    };

    try {
      if (editingId) {
        await updateDepartment(editingId, payload);
        setMessage("Department updated successfully");
      } else {
        await createDepartment(payload);
        setMessage("Department created successfully");
      }

      resetForm();
      loadDepartments();
    } catch (err) {
      console.log("Department operation failed", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Operation failed");
    }
  };

  const handleEdit = (dept) => {
    setEditingId(dept.id);
    setForm({
      code: dept.code || "",
      name: dept.name || "",
      description: dept.description || "",
    });
    setMessage("");
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this department?")) return;

    try {
      await deleteDepartment(id);
      setMessage("Department deleted successfully");

      if (editingId === id) {
        resetForm();
      }

      loadDepartments();
    } catch (err) {
      console.log("Delete failed", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Delete failed");
    }
  };

  return (
    <div style={{ padding: "20px", color: "white" }}>
      <h2>Manage Departments</h2>

      {message && (
        <p style={{ marginBottom: "15px", color: "#ffd548" }}>{message}</p>
      )}

      <form
        onSubmit={handleSubmit}
        style={{
          background: "rgba(255,255,255,0.04)",
          padding: "20px",
          borderRadius: "16px",
          marginBottom: "25px",
        }}
      >
        <h3>{editingId ? "Edit Department" : "Create Department"}</h3>

        <input
          type="text"
          placeholder="Department Code"
          value={form.code}
          onChange={(e) => setForm({ ...form, code: e.target.value })}
          required
          style={inputStyle}
        />

        <input
          type="text"
          placeholder="Department Name"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          required
          style={inputStyle}
        />

        <textarea
          placeholder="Description"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          style={{ ...inputStyle, minHeight: "100px" }}
        />

        <div style={{ marginTop: "10px" }}>
          <button type="submit" style={buttonStyle}>
            {editingId ? "Update Department" : "Create Department"}
          </button>

          {editingId && (
            <button
              type="button"
              style={{ ...buttonStyle, marginLeft: "10px", background: "#666", color: "#fff" }}
              onClick={resetForm}
            >
              Cancel
            </button>
          )}
        </div>
      </form>

      <div
        style={{
          background: "rgba(255,255,255,0.04)",
          padding: "20px",
          borderRadius: "16px",
        }}
      >
        <h3>Department List</h3>

        {departments.length === 0 ? (
          <p>No departments found.</p>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr>
                <th style={thStyle}>Code</th>
                <th style={thStyle}>Name</th>
                <th style={thStyle}>Description</th>
                <th style={thStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {departments.map((dept) => (
                <tr key={dept.id}>
                  <td style={tdStyle}>{dept.code}</td>
                  <td style={tdStyle}>{dept.name}</td>
                  <td style={tdStyle}>{dept.description || "-"}</td>
                  <td style={tdStyle}>
                    <button
                      type="button"
                      onClick={() => handleEdit(dept)}
                      style={smallButtonStyle}
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(dept.id)}
                      style={{
                        ...smallButtonStyle,
                        marginLeft: "8px",
                        background: "#b91c1c",
                      }}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

const inputStyle = {
  width: "100%",
  marginBottom: "12px",
  padding: "12px",
  borderRadius: "10px",
  border: "1px solid rgba(255,255,255,0.15)",
  background: "rgba(255,255,255,0.06)",
  color: "white",
  boxSizing: "border-box",
};

const buttonStyle = {
  padding: "12px 16px",
  border: "none",
  borderRadius: "10px",
  background: "#ffd548",
  color: "#000",
  fontWeight: "bold",
  cursor: "pointer",
};

const smallButtonStyle = {
  padding: "8px 12px",
  border: "none",
  borderRadius: "8px",
  background: "#2563eb",
  color: "white",
  cursor: "pointer",
};

const thStyle = {
  textAlign: "left",
  padding: "12px",
  borderBottom: "1px solid rgba(255,255,255,0.1)",
};

const tdStyle = {
  padding: "12px",
  borderBottom: "1px solid rgba(255,255,255,0.05)",
};

export default AdminDepartments;