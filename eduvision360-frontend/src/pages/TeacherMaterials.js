import React, { useEffect, useState } from "react";
import Menu from "../components/Menu";
import {
  getMyTeacherCourses,
  getMyTeacherMaterials,
  createMaterial,
  updateTeacherMaterial,
  deleteTeacherMaterial
} from "../services/academicService";
import { PlusCircle, Trash2, FileText, Link2, Edit3, XCircle } from "lucide-react";

function TeacherMaterials() {
  const [courses, setCourses] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [message, setMessage] = useState("");
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    courseId: "",
    title: "",
    description: "",
    type: "NOTE",
    content: ""
  });

  const loadData = async () => {
    try {
      const [coursesRes, materialsRes] = await Promise.all([
        getMyTeacherCourses(),
        getMyTeacherMaterials()
      ]);

      const courseList = coursesRes.data || [];
      const materialList = materialsRes.data || [];

      setCourses(courseList);
      setMaterials(materialList);

      if (courseList.length > 0 && !form.courseId) {
        setForm((prev) => ({
          ...prev,
          courseId: courseList[0].id
        }));
      }
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to load materials page");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");

    try {
      const payload = {
        ...form,
        title: form.title.trim(),
        description: form.description.trim(),
        content: form.content.trim()
      };

      if (editingId) {
        await updateTeacherMaterial(editingId, payload);
        setMessage("✅ Material updated successfully");
      } else {
        await createMaterial(payload);
        setMessage("✅ Material uploaded successfully");
      }

      await loadData();

      setForm((prev) => ({
        ...prev,
        title: "",
        description: "",
        type: "NOTE",
        content: ""
      }));

      setEditingId(null);
    } catch (err) {
      console.log("Update/Save material error:", err);
      console.log("Status:", err?.response?.status);
      console.log("Data:", err?.response?.data);

      setMessage(
        err?.response?.data?.message ||
          err?.response?.data?.error ||
          `Failed to save material (${err?.response?.status || "no status"})`
      );
    }
  };

  const handleEdit = (material) => {
    setEditingId(material.id);
    setForm({
      courseId: material.courseId,
      title: material.title || "",
      description: material.description || "",
      type: material.type || "NOTE",
      content: material.content || ""
    });
    setMessage("✏️ Editing material");
  };

  const handleCancelEdit = () => {
    setEditingId(null);

    if (courses.length > 0) {
      setForm({
        courseId: courses[0].id,
        title: "",
        description: "",
        type: "NOTE",
        content: ""
      });
    } else {
      setForm({
        courseId: "",
        title: "",
        description: "",
        type: "NOTE",
        content: ""
      });
    }

    setMessage("Edit cancelled");
  };

  const handleDelete = async (id) => {
    const ok = window.confirm("Are you sure you want to delete this material?");
    if (!ok) return;

    try {
      await deleteTeacherMaterial(id);
      await loadData();
      setMessage("✅ Material deleted successfully");
    } catch (err) {
      console.log("Delete material error:", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Delete failed");
    }
  };

  const filteredMaterials = form.courseId
    ? materials.filter((m) => m.courseId === form.courseId)
    : materials;

  return (
    <div className="teacher-layout">
      <style>{`
        .teacher-layout {
          display: flex;
          min-height: 100vh;
          background: var(--page-bg);
          font-family: 'Inter', sans-serif;
        }

        .main-content {
          flex: 1;
          padding: 40px;
          color: var(--text-primary);
        }

        .page-header {
          margin-bottom: 24px;
        }

        .page-header h2 {
          margin: 0 0 10px 0;
          font-size: 32px;
          background: linear-gradient(to right, var(--text-primary), var(--accent), var(--text-primary));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .page-header p {
          color: var(--text-secondary);
          margin: 0;
        }

        .grid {
          display: grid;
          grid-template-columns: 1.1fr 1fr;
          gap: 20px;
        }

        .panel {
          background: var(--card-bg);
          border: 1px solid var(--border-color);
          border-radius: 22px;
          padding: 22px;
          box-shadow: var(--card-shadow);
        }

        .panel h3 {
          margin-top: 0;
          color: var(--accent);
        }

        .message {
          color: var(--accent);
          margin-bottom: 14px;
          font-weight: 600;
        }

        .field {
          display: flex;
          flex-direction: column;
          gap: 8px;
          margin-bottom: 14px;
        }

        .field label {
          font-size: 14px;
          color: var(--text-secondary);
          font-weight: 600;
        }

        .field input,
        .field select,
        .field textarea {
          background: var(--input-bg);
          border: 1px solid var(--input-border);
          color: var(--text-primary);
          padding: 12px 14px;
          border-radius: 14px;
          outline: none;
        }

        .field textarea {
          min-height: 120px;
          resize: vertical;
        }

        .field select option {
          background: var(--card-bg);
          color: var(--text-primary);
        }

        .submit-btn {
          width: 100%;
          padding: 12px 16px;
          border-radius: 14px;
          border: 1px solid var(--button-secondary-border);
          background: var(--button-secondary-bg);
          color: var(--button-secondary-text);
          font-weight: 800;
          cursor: pointer;
          display: flex;
          gap: 8px;
          justify-content: center;
          align-items: center;
        }

        .cancel-btn {
          width: 100%;
          margin-top: 10px;
          padding: 12px 16px;
          border-radius: 14px;
          border: 1px solid rgba(248,113,113,0.35);
          background: rgba(248,113,113,0.10);
          color: #f87171;
          font-weight: 800;
          cursor: pointer;
        }

        .material-list {
          display: flex;
          flex-direction: column;
          gap: 14px;
          margin-top: 14px;
        }

        .material-card {
          background: var(--input-bg);
          border: 1px solid var(--border-color);
          border-radius: 18px;
          padding: 16px;
        }

        .material-top {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 14px;
        }

        .material-title {
          font-size: 17px;
          font-weight: 800;
          color: var(--text-primary);
        }

        .material-meta {
          color: var(--text-secondary);
          font-size: 13px;
          margin-top: 6px;
          line-height: 1.6;
        }

        .type-badge {
          display: inline-block;
          padding: 5px 10px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 800;
          background: rgba(255,213,72,0.12);
          color: var(--accent);
          border: 1px solid rgba(255,213,72,0.2);
          margin-bottom: 10px;
        }

        .material-content {
          margin-top: 12px;
          color: var(--text-primary);
          white-space: pre-wrap;
          word-break: break-word;
        }

        .action-row {
          display: flex;
          gap: 8px;
        }

        .edit-btn,
        .delete-btn {
          border-radius: 12px;
          padding: 10px 12px;
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 6px;
        }

        .edit-btn {
          border: 1px solid rgba(96,165,250,0.35);
          background: rgba(96,165,250,0.10);
          color: #60a5fa;
        }

        .delete-btn {
          border: 1px solid rgba(248,113,113,0.35);
          background: rgba(248,113,113,0.10);
          color: #f87171;
        }

        .empty-state {
          color: var(--text-secondary);
          padding: 10px 0;
        }

        a.material-link {
          color: #60a5fa;
          text-decoration: none;
          font-weight: 600;
        }

        @media (max-width: 980px) {
          .grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <div className="page-header">
          <h2>Learning Materials</h2>
          <p>Create notes, links, and PDF links for your assigned courses.</p>
        </div>

        {message && <div className="message">{message}</div>}

        <div className="grid">
          <div className="panel">
            <h3>{editingId ? "Edit Material" : "Upload Material"}</h3>

            {courses.length === 0 ? (
              <div className="empty-state">
                No assigned courses found. You need a course before uploading materials.
              </div>
            ) : (
              <form onSubmit={handleSubmit}>
                <div className="field">
                  <label>Course</label>
                  <select name="courseId" value={form.courseId} onChange={handleChange}>
                    {courses.map((course) => (
                      <option key={course.id} value={course.id}>
                        {course.courseCode} - {course.title}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="field">
                  <label>Title</label>
                  <input
                    type="text"
                    name="title"
                    value={form.title}
                    onChange={handleChange}
                    placeholder="Enter material title"
                  />
                </div>

                <div className="field">
                  <label>Description</label>
                  <input
                    type="text"
                    name="description"
                    value={form.description}
                    onChange={handleChange}
                    placeholder="Short description"
                  />
                </div>

                <div className="field">
                  <label>Material Type</label>
                  <select name="type" value={form.type} onChange={handleChange}>
                    <option value="NOTE">NOTE</option>
                    <option value="LINK">LINK</option>
                    <option value="PDF_LINK">PDF LINK</option>
                  </select>
                </div>

                <div className="field">
                  <label>{form.type === "NOTE" ? "Note Content" : "URL / Link"}</label>
                  <textarea
                    name="content"
                    value={form.content}
                    onChange={handleChange}
                    placeholder={
                      form.type === "NOTE"
                        ? "Write the lesson note here..."
                        : "Paste the full URL here..."
                    }
                  />
                </div>

                <button type="submit" className="submit-btn">
                  <PlusCircle size={18} /> {editingId ? "Update Material" : "Save Material"}
                </button>

                {editingId && (
                  <button type="button" className="cancel-btn" onClick={handleCancelEdit}>
                    <XCircle size={18} style={{ marginRight: 6, verticalAlign: "middle" }} />
                    Cancel Edit
                  </button>
                )}
              </form>
            )}
          </div>

          <div className="panel">
            <h3>My Materials</h3>

            {filteredMaterials.length === 0 ? (
              <div className="empty-state">No materials found for the selected course.</div>
            ) : (
              <div className="material-list">
                {filteredMaterials.map((m) => (
                  <div key={m.id} className="material-card">
                    <div className="type-badge">{m.type}</div>

                    <div className="material-top">
                      <div>
                        <div className="material-title">{m.title}</div>
                        <div className="material-meta">
                          Course: {m.courseCode} - {m.courseTitle || "-"} <br />
                          Uploaded by: {m.teacherName || "-"}
                        </div>
                      </div>

                      <div className="action-row">
                        <button type="button" className="edit-btn" onClick={() => handleEdit(m)}>
                          <Edit3 size={16} /> Edit
                        </button>

                        <button type="button" className="delete-btn" onClick={() => handleDelete(m.id)}>
                          <Trash2 size={16} /> Delete
                        </button>
                      </div>
                    </div>

                    {m.description && (
                      <div className="material-meta" style={{ marginTop: "10px" }}>
                        {m.description}
                      </div>
                    )}

                    <div className="material-content">
                      {m.type === "NOTE" ? (
                        <>
                          <FileText size={16} style={{ marginRight: 6, verticalAlign: "middle" }} />
                          {m.content}
                        </>
                      ) : (
                        <a
                          className="material-link"
                          href={m.content}
                          target="_blank"
                          rel="noreferrer"
                        >
                          <Link2 size={16} style={{ marginRight: 6, verticalAlign: "middle" }} />
                          Open Material Link
                        </a>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default TeacherMaterials;