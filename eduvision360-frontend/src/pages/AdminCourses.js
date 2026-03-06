import React, { useEffect, useState } from "react";
import {
  createCourse,
  deleteCourse,
  getCourses,
  getDepartments,
  updateCourse,
} from "../services/academicService";

function AdminCourses() {
  const [courses, setCourses] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [message, setMessage] = useState("");
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    courseCode: "",
    title: "",
    description: "",
    departmentId: "",
    teacherId: "",
    creditValue: 3,
    semester: 1,
    academicYear: "",
    status: "ACTIVE",
  });

  const loadData = async () => {
    try {
      const [courseRes, deptRes] = await Promise.all([
        getCourses(),
        getDepartments(),
      ]);

      setCourses(courseRes.data || []);
      setDepartments(deptRes.data || []);
    } catch (err) {
      setMessage("Failed to load course data");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const resetForm = () => {
    setEditingId(null);
    setForm({
      courseCode: "",
      title: "",
      description: "",
      departmentId: "",
      teacherId: "",
      creditValue: 3,
      semester: 1,
      academicYear: "",
      status: "ACTIVE",
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");

    try {
      const payload = {
        ...form,
        creditValue: Number(form.creditValue),
        semester: Number(form.semester),
      };

      if (editingId) {
        await updateCourse(editingId, payload);
        setMessage("Course updated successfully");
      } else {
        await createCourse(payload);
        setMessage("Course created successfully");
      }

      resetForm();
      loadData();
    } catch (err) {
      setMessage(err.response?.data?.message || "Operation failed");
    }
  };

  const handleEdit = (course) => {
    setEditingId(course.id);
    setForm({
      courseCode: course.courseCode || "",
      title: course.title || "",
      description: course.description || "",
      departmentId: course.departmentId || "",
      teacherId: course.teacherId || "",
      creditValue: course.creditValue || 3,
      semester: course.semester || 1,
      academicYear: course.academicYear || "",
      status: course.status || "ACTIVE",
    });
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this course?")) return;

    try {
      await deleteCourse(id);
      setMessage("Course deleted successfully");
      loadData();
    } catch (err) {
      setMessage(err.response?.data?.message || "Delete failed");
    }
  };

  return (
    <div style={{ padding: "20px", color: "white" }}>
      <h2>Manage Courses</h2>

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
        <h3>{editingId ? "Edit Course" : "Create Course"}</h3>

        <input
          type="text"
          placeholder="Course Code"
          value={form.courseCode}
          onChange={(e) => setForm({ ...form, courseCode: e.target.value })}
          required
          style={inputStyle}
        />

        <input
          type="text"
          placeholder="Course Title"
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
          required
          style={inputStyle}
        />

        <textarea
          placeholder="Description"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
          style={{ ...inputStyle, minHeight: "100px" }}
        />

        <select
          value={form.departmentId}
          onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
          required
          style={inputStyle}
        >
          <option value="">Select Department</option>
          {departments.map((dept) => (
            <option key={dept.id} value={dept.id}>
              {dept.name} ({dept.code})
            </option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Teacher ID (optional for now)"
          value={form.teacherId}
          onChange={(e) => setForm({ ...form, teacherId: e.target.value })}
          style={inputStyle}
        />

        <input
          type="number"
          placeholder="Credit Value"
          value={form.creditValue}
          onChange={(e) => setForm({ ...form, creditValue: e.target.value })}
          required
          style={inputStyle}
        />

        <input
          type="number"
          placeholder="Semester"
          value={form.semester}
          onChange={(e) => setForm({ ...form, semester: e.target.value })}
          required
          style={inputStyle}
        />

        <input
          type="text"
          placeholder="Academic Year (e.g. 2025/2026)"
          value={form.academicYear}
          onChange={(e) => setForm({ ...form, academicYear: e.target.value })}
          required
          style={inputStyle}
        />

        {editingId && (
          <select
            value={form.status}
            onChange={(e) => setForm({ ...form, status: e.target.value })}
            style={inputStyle}
          >
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        )}

        <div style={{ marginTop: "10px" }}>
          <button type="submit" style={buttonStyle}>
            {editingId ? "Update Course" : "Create Course"}
          </button>

          {editingId && (
            <button
              type="button"
              style={{ ...buttonStyle, marginLeft: "10px", background: "#666" }}
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
        <h3>Course List</h3>

        {courses.length === 0 ? (
          <p>No courses found.</p>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr>
                <th style={thStyle}>Code</th>
                <th style={thStyle}>Title</th>
                <th style={thStyle}>Department</th>
                <th style={thStyle}>Teacher</th>
                <th style={thStyle}>Semester</th>
                <th style={thStyle}>Status</th>
                <th style={thStyle}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {courses.map((course) => (
                <tr key={course.id}>
                  <td style={tdStyle}>{course.courseCode}</td>
                  <td style={tdStyle}>{course.title}</td>
                  <td style={tdStyle}>{course.departmentName || "-"}</td>
                  <td style={tdStyle}>{course.teacherName || course.teacherId || "-"}</td>
                  <td style={tdStyle}>{course.semester}</td>
                  <td style={tdStyle}>{course.status}</td>
                  <td style={tdStyle}>
                    <button
                      onClick={() => handleEdit(course)}
                      style={smallButtonStyle}
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(course.id)}
                      style={{ ...smallButtonStyle, marginLeft: "8px", background: "#b91c1c" }}
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

export default AdminCourses;