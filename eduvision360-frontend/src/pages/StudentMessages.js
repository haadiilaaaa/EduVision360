import React, { useEffect, useState } from "react";
import {
  getStudentInboxMessages,
  markStudentMessageAsRead
} from "../services/academicService";

function StudentMessages() {
  const [messages, setMessages] = useState([]);
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadMessages = async () => {
    try {
      setLoading(true);
      const res = await getStudentInboxMessages();
      const data = res.data || [];
      setMessages(data);

      if (data.length > 0 && !selectedMessage) {
        setSelectedMessage(data[0]);
      }
    } catch (err) {
      console.error("Failed to load student inbox", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMessages();
  }, []);

  const formatDateTime = (value) => {
    if (!value) return "";
    try {
      return new Date(value).toLocaleString();
    } catch {
      return value;
    }
  };

  const openMessage = async (message) => {
    setSelectedMessage(message);

    if (!message.read) {
      try {
        const res = await markStudentMessageAsRead(message.id);
        const updated = res.data;

        setMessages((prev) =>
          prev.map((m) => (m.id === message.id ? updated : m))
        );
        setSelectedMessage(updated);
      } catch (err) {
        console.error("Failed to mark message as read", err);
      }
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.content}>
        <div style={styles.headerCard}>
          <h1 style={styles.title}>Student Messages</h1>
          <p style={styles.subtitle}>
            View messages sent by your teachers and track unread communication.
          </p>
        </div>

        <div style={styles.layout}>
          <div style={styles.leftCard}>
            <h2 style={styles.cardTitle}>Inbox</h2>

            {loading ? (
              <div style={styles.emptyBox}>Loading inbox...</div>
            ) : messages.length === 0 ? (
              <div style={styles.emptyBox}>No messages received yet.</div>
            ) : (
              <div style={styles.list}>
                {messages.map((message) => (
                  <button
                    key={message.id}
                    onClick={() => openMessage(message)}
                    style={{
                      ...styles.listItem,
                      ...(selectedMessage?.id === message.id ? styles.listItemActive : {}),
                      ...(!message.read ? styles.listItemUnread : {})
                    }}
                  >
                    <div style={styles.listTop}>
                      <span style={styles.senderName}>
                        {message.senderName || "Teacher"}
                      </span>
                      {!message.read && <span style={styles.unreadBadge}>Unread</span>}
                    </div>

                    <div style={styles.subject}>{message.subject}</div>
                    <div style={styles.time}>{formatDateTime(message.createdAt)}</div>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div style={styles.rightCard}>
            <h2 style={styles.cardTitle}>Message Details</h2>

            {!selectedMessage ? (
              <div style={styles.emptyBox}>Select a message to view details.</div>
            ) : (
              <div style={styles.detailBox}>
                <div style={styles.detailHeader}>
                  <div style={styles.detailSubject}>{selectedMessage.subject}</div>
                  <div style={styles.detailTime}>
                    {formatDateTime(selectedMessage.createdAt)}
                  </div>
                </div>

                <div style={styles.detailMeta}>
                  From: {selectedMessage.senderName || "Teacher"}
                </div>

                <div style={styles.readStatus}>
                  Status: {selectedMessage.read ? "Read" : "Unread"}
                </div>

                <div style={styles.detailBody}>{selectedMessage.body}</div>
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
    minHeight: "100vh",
    background: "var(--bg-main)"
  },
  content: {
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
  layout: {
    display: "grid",
    gridTemplateColumns: "360px 1fr",
    gap: "20px"
  },
  leftCard: {
    background: "var(--bg-card)",
    border: "1px solid var(--border-soft)",
    borderRadius: "20px",
    padding: "20px",
    boxShadow: "var(--shadow-md)"
  },
  rightCard: {
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
  emptyBox: {
    padding: "16px",
    borderRadius: "12px",
    background: "var(--bg-card-2)",
    color: "var(--text-muted)"
  },
  list: {
    display: "flex",
    flexDirection: "column",
    gap: "10px"
  },
  listItem: {
    width: "100%",
    textAlign: "left",
    padding: "14px",
    borderRadius: "14px",
    border: "1px solid var(--border-soft)",
    background: "var(--bg-card-2)",
    cursor: "pointer"
  },
  listItemActive: {
    border: "1px solid rgba(255,213,72,0.35)",
    background: "rgba(255,213,72,0.08)"
  },
  listItemUnread: {
    boxShadow: "inset 3px 0 0 var(--accent)"
  },
  listTop: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "10px",
    marginBottom: "6px"
  },
  senderName: {
    color: "var(--text-main)",
    fontWeight: 800,
    fontSize: "13px"
  },
  unreadBadge: {
    padding: "4px 8px",
    borderRadius: "999px",
    background: "rgba(239,68,68,0.12)",
    color: "#dc2626",
    fontSize: "11px",
    fontWeight: 800
  },
  subject: {
    color: "var(--text-main)",
    fontSize: "14px",
    fontWeight: 700,
    marginBottom: "6px"
  },
  time: {
    color: "var(--text-dim)",
    fontSize: "12px"
  },
  detailBox: {
    padding: "4px 0"
  },
  detailHeader: {
    display: "flex",
    justifyContent: "space-between",
    gap: "12px",
    marginBottom: "10px"
  },
  detailSubject: {
    color: "var(--text-main)",
    fontSize: "20px",
    fontWeight: 800
  },
  detailTime: {
    color: "var(--text-dim)",
    fontSize: "12px",
    whiteSpace: "nowrap"
  },
  detailMeta: {
    color: "var(--text-muted)",
    fontSize: "13px",
    marginBottom: "8px"
  },
  readStatus: {
    color: "var(--text-soft)",
    fontSize: "13px",
    marginBottom: "16px",
    fontWeight: 700
  },
  detailBody: {
    padding: "16px",
    borderRadius: "14px",
    background: "var(--bg-card-2)",
    border: "1px solid var(--border-soft)",
    color: "var(--text-soft)",
    fontSize: "14px",
    lineHeight: 1.7,
    whiteSpace: "pre-wrap"
  }
};

export default StudentMessages;