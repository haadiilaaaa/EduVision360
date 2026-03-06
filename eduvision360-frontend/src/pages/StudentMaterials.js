import React, { useEffect, useMemo, useState } from "react";
import {
  getMyEnrollments,
  getStudentMaterials,
  trackMaterialView
} from "../services/academicService";
import { BookOpen, FileText, Link2, X } from "lucide-react";

function StudentMaterials() {
  const [enrollments, setEnrollments] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [message, setMessage] = useState("");
  const [selectedNote, setSelectedNote] = useState(null);
  const [actionLoadingId, setActionLoadingId] = useState("");

  const loadData = async () => {
    try {
      setMessage("");

      const [enrollmentRes, materialRes] = await Promise.all([
        getMyEnrollments(),
        getStudentMaterials()
      ]);

      const enrolledCourses = enrollmentRes.data || [];
      const materialList = materialRes.data || [];

      setEnrollments(enrolledCourses);
      setMaterials(materialList);

      if (enrolledCourses.length > 0) {
        setSelectedCourseId((prev) => prev || enrolledCourses[0].courseId);
      } else {
        setSelectedCourseId("");
      }
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to load materials");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filteredMaterials = useMemo(() => {
    if (!selectedCourseId) return materials;
    return materials.filter((m) => m.courseId === selectedCourseId);
  }, [materials, selectedCourseId]);

  const handleViewNote = async (material) => {
    try {
      setMessage("");
      setActionLoadingId(material.id);

      await trackMaterialView(material.id);
      setSelectedNote(material);
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to track note view");
    } finally {
      setActionLoadingId("");
    }
  };

  const handleOpenLink = async (material) => {
    const popup = window.open("", "_blank");

    try {
      setMessage("");
      setActionLoadingId(material.id);

      await trackMaterialView(material.id);

      if (popup) {
        popup.opener = null;
        popup.location.href = material.content;
      } else {
        window.open(material.content, "_blank", "noopener,noreferrer");
      }
    } catch (err) {
      if (popup) popup.close();
      setMessage(err?.response?.data?.message || "Failed to track material view");
    } finally {
      setActionLoadingId("");
    }
  };

  return (
    <div className="student-materials-page">
      <style>{`
        .student-materials-page {
          color: var(--text-main);
        }

        .page-title {
          margin-top: 0;
          display: flex;
          gap: 10px;
          align-items: center;
        }

        .message-box {
          color: var(--accent);
          margin-bottom: 12px;
          font-weight: 600;
        }

        .muted-text {
          color: var(--text-muted);
        }

        .select-label {
          display: block;
          margin-bottom: 8px;
          color: var(--text-main);
          font-weight: 600;
        }

        .course-select {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
          padding: 12px 14px;
          border-radius: 14px;
          min-width: 300px;
          outline: none;
        }

        .materials-grid {
          display: grid;
          gap: 14px;
        }

        .material-card {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 18px;
          padding: 16px;
        }

        .material-type {
          display: inline-block;
          padding: 5px 10px;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 800;
          background: var(--accent-soft);
          color: var(--accent);
          border: 1px solid rgba(255,213,72,0.2);
          margin-bottom: 10px;
        }

        .material-title {
          font-weight: 800;
          font-size: 17px;
          color: var(--text-main);
        }

        .material-meta {
          color: var(--text-muted);
          font-size: 13px;
          margin-top: 6px;
          line-height: 1.6;
        }

        .material-description {
          color: var(--text-main);
          margin-top: 10px;
        }

        .material-actions {
          margin-top: 14px;
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .action-btn {
          border: none;
          border-radius: 12px;
          padding: 10px 14px;
          font-weight: 700;
          cursor: pointer;
          display: inline-flex;
          align-items: center;
          gap: 8px;
        }

        .primary-btn {
          background: var(--accent);
          color: #111827;
        }

        .primary-btn:disabled {
          opacity: 0.7;
          cursor: not-allowed;
        }

        .note-preview {
          margin-top: 10px;
          color: var(--text-muted);
          font-size: 14px;
          line-height: 1.6;
        }

        .modal-backdrop {
          position: fixed;
          inset: 0;
          background: rgba(0,0,0,0.6);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 999;
          padding: 20px;
        }

        .modal-card {
          width: min(800px, 100%);
          max-height: 85vh;
          overflow-y: auto;
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 22px;
          padding: 20px;
          box-shadow: 0 20px 60px rgba(0,0,0,0.35);
        }

        .modal-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 10px;
          margin-bottom: 16px;
        }

        .modal-title {
          font-size: 20px;
          font-weight: 800;
          color: var(--text-main);
        }

        .close-btn {
          border: none;
          background: transparent;
          color: var(--text-main);
          cursor: pointer;
        }

        .note-content {
          color: var(--text-main);
          white-space: pre-wrap;
          word-break: break-word;
          line-height: 1.8;
        }
      `}</style>

      <h2 className="page-title">
        <BookOpen /> Learning Materials
      </h2>

      {message && <div className="message-box">{message}</div>}

      {enrollments.length === 0 ? (
        <div className="muted-text">You are not enrolled in any courses yet.</div>
      ) : (
        <>
          <div style={{ marginBottom: 18 }}>
            <label className="select-label">Select Course</label>
            <select
              className="course-select"
              value={selectedCourseId}
              onChange={(e) => setSelectedCourseId(e.target.value)}
            >
              {enrollments.map((c) => (
                <option key={c.courseId} value={c.courseId}>
                  {c.courseCode} - {c.title}
                </option>
              ))}
            </select>
          </div>

          {filteredMaterials.length === 0 ? (
            <div className="muted-text">No materials uploaded for this course yet.</div>
          ) : (
            <div className="materials-grid">
              {filteredMaterials.map((m) => (
                <div key={m.id} className="material-card">
                  <div className="material-type">{m.type}</div>

                  <div className="material-title">{m.title}</div>

                  <div className="material-meta">
                    Course: {m.courseCode} - {m.courseTitle || "-"} <br />
                    Teacher: {m.teacherName || "-"}
                  </div>

                  {m.description && (
                    <div className="material-description">{m.description}</div>
                  )}

                  {m.type === "NOTE" ? (
                    <>
                      <div className="note-preview">
                        Click below to view this note. The view will be tracked for learning analytics.
                      </div>

                      <div className="material-actions">
                        <button
                          className="action-btn primary-btn"
                          onClick={() => handleViewNote(m)}
                          disabled={actionLoadingId === m.id}
                        >
                          <FileText size={16} />
                          {actionLoadingId === m.id ? "Opening..." : "View Note"}
                        </button>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="note-preview">
                        Open this material link to access the learning resource.
                      </div>

                      <div className="material-actions">
                        <button
                          className="action-btn primary-btn"
                          onClick={() => handleOpenLink(m)}
                          disabled={actionLoadingId === m.id}
                        >
                          <Link2 size={16} />
                          {actionLoadingId === m.id ? "Opening..." : "Open Material"}
                        </button>
                      </div>
                    </>
                  )}
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {selectedNote && (
        <div className="modal-backdrop" onClick={() => setSelectedNote(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <div className="modal-title">{selectedNote.title}</div>
                <div className="material-meta">
                  Course: {selectedNote.courseCode} - {selectedNote.courseTitle || "-"} <br />
                  Teacher: {selectedNote.teacherName || "-"}
                </div>
              </div>

              <button className="close-btn" onClick={() => setSelectedNote(null)}>
                <X size={22} />
              </button>
            </div>

            {selectedNote.description && (
              <div className="material-description" style={{ marginBottom: 14 }}>
                {selectedNote.description}
              </div>
            )}

            <div className="note-content">{selectedNote.content}</div>
          </div>
        </div>
      )}
    </div>
  );
}

export default StudentMaterials;