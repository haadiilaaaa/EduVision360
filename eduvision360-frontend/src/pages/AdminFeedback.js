import React, { useEffect, useState } from "react";
import {
  getAdminFeedback,
  updateAdminFeedbackStatus
} from "../services/academicService";

function AdminFeedback() {
  const [feedbackList, setFeedbackList] = useState([]);
  const [message, setMessage] = useState("");

  const loadFeedback = async () => {
    try {
      const res = await getAdminFeedback();
      setFeedbackList(res.data || []);
    } catch (err) {
      setMessage("Failed to load admin feedback");
    }
  };

  useEffect(() => {
    loadFeedback();
  }, []);

  const handleUpdate = async (id, status, responseComment) => {
    try {
      await updateAdminFeedbackStatus(id, { status, responseComment });
      setMessage("Feedback updated successfully");
      loadFeedback();
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to update feedback");
    }
  };

  return (
    <div>
      <h2 style={{ color: "var(--accent)" }}>Admin Feedback Management</h2>
      {message && <p>{message}</p>}

      {feedbackList.length === 0 ? (
        <p>No feedback found.</p>
      ) : (
        feedbackList.map((item) => (
          <AdminFeedbackCard key={item.id} item={item} onUpdate={handleUpdate} />
        ))
      )}
    </div>
  );
}

function AdminFeedbackCard({ item, onUpdate }) {
  const [status, setStatus] = useState(item.status);
  const [responseComment, setResponseComment] = useState(item.responseComment || "");

  return (
    <div
      style={{
        background: "var(--bg-card)",
        border: "1px solid var(--border-soft)",
        borderRadius: "18px",
        padding: "18px",
        marginBottom: "14px"
      }}
    >
      <h4>{item.courseCode} - {item.courseTitle}</h4>
      <p><strong>Student:</strong> {item.studentName} ({item.studentEmail})</p>
      <p><strong>Category:</strong> {item.category}</p>
      <p><strong>Rating:</strong> {item.rating}/5</p>
      <p><strong>Status:</strong> {item.status}</p>
      <p><strong>Comment:</strong> {item.comment}</p>

      <select value={status} onChange={(e) => setStatus(e.target.value)} style={{ marginRight: "10px" }}>
        <option value="NEW">NEW</option>
        <option value="REVIEWED">REVIEWED</option>
        <option value="RESOLVED">RESOLVED</option>
      </select>

      <input
        type="text"
        value={responseComment}
        onChange={(e) => setResponseComment(e.target.value)}
        placeholder="Write response"
        style={{ padding: "8px", width: "320px", marginRight: "10px" }}
      />

      <button onClick={() => onUpdate(item.id, status, responseComment)}>
        Update
      </button>
    </div>
  );
}

export default AdminFeedback;