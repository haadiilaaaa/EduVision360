import React, { useEffect, useState } from "react";
import Menu from "../components/Menu";
import {
  getTeacherMessageStudents,
  sendTeacherMessage,
  getTeacherSentMessages
} from "../services/academicService";

function TeacherMessages() {
  const [students, setStudents] = useState([]);
  const [sentMessages, setSentMessages] = useState([]);
  const [form, setForm] = useState({
    receiverId: "",
    subject: "",
    body: ""
  });
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  const loadData = async () => {
    try {
      setLoading(true);
      const [studentsRes, sentRes] = await Promise.all([
        getTeacherMessageStudents(),
        getTeacherSentMessages()
      ]);

      setStudents(studentsRes.data || []);
      setSentMessages(sentRes.data || []);
    } catch (err) {
      console.error("Failed to load teacher messages data", err);
      setError("Failed to load message data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSend = async (e) => {
    e.preventDefault();
    setSuccess("");
    setError("");

    if (!form.receiverId || !form.subject.trim() || !form.body.trim()) {
      setError("Please fill all fields.");
      return;
    }

    try {
      setSending(true);
      await sendTeacherMessage({
        receiverId: form.receiverId,
        subject: form.subject,
        body: form.body
      });

      setSuccess("Message sent successfully.");
      setForm({
        receiverId: "",
        subject: "",
        body: ""
      });

      const sentRes = await getTeacherSentMessages();
      setSentMessages(sentRes.data || []);
    } catch (err) {
      console.error("Failed to send message", err);
      setError(err?.response?.data?.message || "Failed to send message.");
    } finally {
      setSending(false);
    }
  };

  const formatDateTime = (value) => {
    if (!value) return "";
    try {
      return new Date(value).toLocaleString();
    } catch {
      return value;
    }
  };

  return (
    <div style={styles.page}>
      <Menu />

      <div style={styles.content}>
        <div style={styles.headerCard}>
          <h1 style={styles.title}>Teacher Messages</h1>
          <p style={styles.subtitle}>
            Send direct messages to students and review sent communications.
          </p>
        </div>

        <div style={styles.grid}>
          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Compose Message</h2>

            {success && <div style={styles.successBox}>{success}</div>}
            {error && <div style={styles.errorBox}>{error}</div>}

            <form onSubmit={handleSend} style={styles.form}>
              <label style={styles.label}>Select Student</label>
              <select
                name="receiverId"
                value={form.receiverId}
                onChange={handleChange}
                style={styles.select}
              >
                <option value="">Choose a student</option>
                {students.map((student) => (
                  <option key={student.id} value={student.id}>
                    {student.fullName} {student.studentCode ? `(${student.studentCode})` : ""}
                  </option>
                ))}
              </select>

              <label style={styles.label}>Subject</label>
              <input
                type="text"
                name="subject"
                value={form.subject}
                onChange={handleChange}
                placeholder="Enter message subject"
                style={styles.input}
              />

              <label style={styles.label}>Message</label>
              <textarea
                name="body"
                value={form.body}
                onChange={handleChange}
                placeholder="Write your message here..."
                rows={7}
                style={styles.textarea}
              />

              <button type="submit" style={styles.primaryButton} disabled={sending}>
                {sending ? "Sending..." : "Send Message"}
              </button>
            </form>
          </div>

          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Sent Messages</h2>

            {loading ? (
              <div style={styles.emptyBox}>Loading messages...</div>
            ) : sentMessages.length === 0 ? (
              <div style={styles.emptyBox}>No sent messages yet.</div>
            ) : (
              <div style={styles.messageList}>
                {sentMessages.map((message) => (
                  <div key={message.id} style={styles.messageItem}>
                    <div style={styles.messageTop}>
                      <div>
                        <div style={styles.messageSubject}>{message.subject}</div>
                        <div style={styles.messageMeta}>
                          To: {message.receiverName || "Student"}
                        </div>
                      </div>
                      <div style={styles.messageTime}>
                        {formatDateTime(message.createdAt)}
                      </div>
                    </div>

                    <div style={styles.messageBody}>{message.body}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: {
    display: "flex",
    minHeight: "100vh",
    background: "var(--bg-main)"
  },
  content: {
    flex: 1,
    padding: "24px"
  },
  headerCard: {
    background: "var(--bg-card)",
    border: "1px solid var(--border-soft)",
    borderRadius: "20px",
    padding: "22px",
    marginBottom: "20px",
    boxShadow: "var(--shadow-md)"
  },
  title: {
    margin: 0,
    color: "var(--text-main)",
    fontSize: "28px",
    fontWeight: 800
  },
  subtitle: {
    marginTop: "8px",
    marginBottom: 0,
    color: "var(--text-muted)",
    fontSize: "14px",
    lineHeight: 1.6
  },
  grid: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "20px"
  },
  card: {
    background: "var(--bg-card)",
    border: "1px solid var(--border-soft)",
    borderRadius: "20px",
    padding: "20px",
    boxShadow: "var(--shadow-md)"
  },
  cardTitle: {
    marginTop: 0,
    marginBottom: "16px",
    color: "var(--text-main)",
    fontSize: "20px",
    fontWeight: 800
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "10px"
  },
  label: {
    color: "var(--text-main)",
    fontSize: "13px",
    fontWeight: 700
  },
  input: {
    padding: "12px 14px",
    borderRadius: "12px",
    border: "1px solid var(--border-soft)",
    background: "var(--bg-card-2)",
    color: "var(--text-main)",
    outline: "none"
  },
  select: {
    padding: "12px 14px",
    borderRadius: "12px",
    border: "1px solid var(--border-soft)",
    background: "var(--bg-card-2)",
    color: "var(--text-main)",
    outline: "none"
  },
  textarea: {
    padding: "12px 14px",
    borderRadius: "12px",
    border: "1px solid var(--border-soft)",
    background: "var(--bg-card-2)",
    color: "var(--text-main)",
    outline: "none",
    resize: "vertical"
  },
  primaryButton: {
    marginTop: "8px",
    padding: "12px 16px",
    border: "none",
    borderRadius: "12px",
    background: "linear-gradient(135deg, var(--accent), #f5c400)",
    color: "#111827",
    fontWeight: 800,
    cursor: "pointer"
  },
  successBox: {
    marginBottom: "14px",
    padding: "12px 14px",
    borderRadius: "12px",
    background: "rgba(34,197,94,0.12)",
    color: "#16a34a",
    fontWeight: 700
  },
  errorBox: {
    marginBottom: "14px",
    padding: "12px 14px",
    borderRadius: "12px",
    background: "rgba(239,68,68,0.12)",
    color: "#dc2626",
    fontWeight: 700
  },
  emptyBox: {
    padding: "16px",
    borderRadius: "12px",
    background: "var(--bg-card-2)",
    color: "var(--text-muted)"
  },
  messageList: {
    display: "flex",
    flexDirection: "column",
    gap: "12px"
  },
  messageItem: {
    padding: "14px",
    borderRadius: "14px",
    background: "var(--bg-card-2)",
    border: "1px solid var(--border-soft)"
  },
  messageTop: {
    display: "flex",
    justifyContent: "space-between",
    gap: "12px",
    marginBottom: "8px"
  },
  messageSubject: {
    color: "var(--text-main)",
    fontWeight: 800,
    fontSize: "14px"
  },
  messageMeta: {
    marginTop: "4px",
    color: "var(--text-muted)",
    fontSize: "12px"
  },
  messageTime: {
    color: "var(--text-dim)",
    fontSize: "12px",
    whiteSpace: "nowrap"
  },
  messageBody: {
    color: "var(--text-soft)",
    fontSize: "13px",
    lineHeight: 1.6,
    whiteSpace: "pre-wrap"
  }
};

export default TeacherMessages;