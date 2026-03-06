import React, { useEffect, useState } from "react";
import Menu from "../components/Menu";
import {
  getMyTeacherCourses,
  getMyTeacherAnnouncements,
  createAnnouncement,
  updateTeacherAnnouncement,
  deleteTeacherAnnouncement
} from "../services/academicService";
import { Megaphone, Edit3, Trash2, XCircle } from "lucide-react";

function TeacherAnnouncements() {
  const [courses, setCourses] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [message, setMessage] = useState("");
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    courseId: "",
    title: "",
    message: ""
  });

  const loadData = async () => {
    try {
      const [coursesRes, announcementsRes] = await Promise.all([
        getMyTeacherCourses(),
        getMyTeacherAnnouncements()
      ]);

      const teacherCourses = coursesRes.data || [];
      const teacherAnnouncements = announcementsRes.data || [];

      setCourses(teacherCourses);
      setAnnouncements(teacherAnnouncements);

      setForm((prev) => {
        if (!prev.courseId && teacherCourses.length > 0) {
          return { ...prev, courseId: teacherCourses[0].id };
        }
        return prev;
      });
    } catch (err) {
      console.log("Load announcements error:", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to load announcements");
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
        message: form.message.trim()
      };

      if (editingId) {
        await updateTeacherAnnouncement(editingId, payload);
        setMessage("✅ Announcement updated successfully");
      } else {
        await createAnnouncement(payload);
        setMessage("✅ Announcement posted successfully");
      }

      await loadData();

      setForm((prev) => ({
        ...prev,
        title: "",
        message: ""
      }));
      setEditingId(null);
    } catch (err) {
      console.log("Save announcement error:", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Failed to save announcement");
    }
  };

  const handleEdit = (announcement) => {
    setEditingId(announcement.id);
    setForm({
      courseId: announcement.courseId,
      title: announcement.title || "",
      message: announcement.message || ""
    });
    setMessage("✏️ Editing announcement");
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setForm({
      courseId: courses.length > 0 ? courses[0].id : "",
      title: "",
      message: ""
    });
    setMessage("Edit cancelled");
  };

  const handleDelete = async (id) => {
    const ok = window.confirm("Are you sure you want to delete this announcement?");
    if (!ok) return;

    try {
      await deleteTeacherAnnouncement(id);
      await loadData();
      setMessage("✅ Announcement deleted successfully");
    } catch (err) {
      console.log("Delete announcement error:", err?.response?.status, err?.response?.data);
      setMessage(err?.response?.data?.message || "Delete failed");
    }
  };

  const filteredAnnouncements = form.courseId
    ? announcements.filter((a) => a.courseId === form.courseId)
    : announcements;

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

        .message-line {
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

        .field select option {
          background: var(--card-bg);
          color: var(--text-primary);
        }

        .field textarea {
          min-height: 150px;
          resize: vertical;
        }

        .submit-btn,
        .cancel-btn {
          width: 100%;
          padding: 12px 16px;
          border-radius: 14px;
          font-weight: 800;
          cursor: pointer;
          display: flex;
          gap: 8px;
          justify-content: center;
          align-items: center;
        }

        .submit-btn {
          border: 1px solid var(--button-secondary-border);
          background: var(--button-secondary-bg);
          color: var(--button-secondary-text);
        }

        .cancel-btn {
          margin-top: 10px;
          border: 1px solid rgba(248,113,113,0.35);
          background: rgba(248,113,113,0.10);
          color: #f87171;
        }

        .announcement-list {
          display: flex;
          flex-direction: column;
          gap: 14px;
          margin-top: 14px;
        }

        .announcement-card {
          background: var(--input-bg);
          border: 1px solid var(--border-color);
          border-radius: 18px;
          padding: 16px;
        }

        .announcement-top {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 14px;
        }

        .announcement-title {
          font-size: 17px;
          font-weight: 800;
          color: var(--text-primary);
        }

        .announcement-meta {
          color: var(--text-secondary);
          font-size: 13px;
          margin-top: 6px;
          line-height: 1.6;
        }

        .announcement-content {
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

        @media (max-width: 980px) {
          .grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <div className="page-header">
          <h2>Course Announcements</h2>
          <p>Post updates and important notices for your assigned courses.</p>
        </div>

        {message && <div className="message-line">{message}</div>}

        <div className="grid">
          <div className="panel">
            <h3>{editingId ? "Edit Announcement" : "Post Announcement"}</h3>

            {courses.length === 0 ? (
              <div className="empty-state">No assigned courses found.</div>
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
                    placeholder="Enter announcement title"
                  />
                </div>

                <div className="field">
                  <label>Message</label>
                  <textarea
                    name="message"
                    value={form.message}
                    onChange={handleChange}
                    placeholder="Write the announcement here..."
                  />
                </div>

                <button type="submit" className="submit-btn">
                  <Megaphone size={18} />
                  {editingId ? "Update Announcement" : "Post Announcement"}
                </button>

                {editingId && (
                  <button type="button" className="cancel-btn" onClick={handleCancelEdit}>
                    <XCircle size={18} />
                    Cancel Edit
                  </button>
                )}
              </form>
            )}
          </div>

          <div className="panel">
            <h3>My Announcements</h3>

            {filteredAnnouncements.length === 0 ? (
              <div className="empty-state">No announcements found for the selected course.</div>
            ) : (
              <div className="announcement-list">
                {filteredAnnouncements.map((a) => (
                  <div key={a.id} className="announcement-card">
                    <div className="announcement-top">
                      <div>
                        <div className="announcement-title">{a.title}</div>
                        <div className="announcement-meta">
                          Course: {a.courseCode} - {a.courseTitle || "-"} <br />
                          Posted by: {a.teacherName || "-"}
                        </div>
                      </div>

                      <div className="action-row">
                        <button type="button" className="edit-btn" onClick={() => handleEdit(a)}>
                          <Edit3 size={16} /> Edit
                        </button>

                        <button type="button" className="delete-btn" onClick={() => handleDelete(a.id)}>
                          <Trash2 size={16} /> Delete
                        </button>
                      </div>
                    </div>

                    <div className="announcement-content">{a.message}</div>

                    {(a.updatedAt || a.createdAt) && (
                      <div className="announcement-meta" style={{ marginTop: "10px" }}>
                        {a.updatedAt ? "Updated: " : "Posted: "}
                        {new Date(a.updatedAt || a.createdAt).toLocaleString()}
                      </div>
                    )}
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

export default TeacherAnnouncements;