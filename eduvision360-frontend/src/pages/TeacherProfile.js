import React, { useEffect, useState } from "react";
import Menu from "../components/Menu";
import { User, Save, RefreshCcw } from "lucide-react";
import { getMyProfile, updateMyProfile } from "../services/academicService";

function TeacherProfile() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({
    fullName: "",
    contactNumber: ""
  });
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const loadProfile = async () => {
    try {
      setLoading(true);
      setMessage("");
      const res = await getMyProfile();
      const data = res.data;

      setProfile(data);
      setForm({
        fullName: data.fullName || "",
        contactNumber: data.contactNumber || ""
      });
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to load profile");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value
    });
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setMessage("");

      const payload = {
        fullName: form.fullName.trim(),
        contactNumber: form.contactNumber.trim()
      };

      const res = await updateMyProfile(payload);
      setProfile(res.data);
      setForm({
        fullName: res.data.fullName || "",
        contactNumber: res.data.contactNumber || ""
      });

      setMessage("✅ Profile updated successfully");
    } catch (err) {
      setMessage(err?.response?.data?.message || "Failed to update profile");
    } finally {
      setSaving(false);
    }
  };

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

        .profile-title {
          margin-top: 0;
          margin-bottom: 20px;
          display: flex;
          align-items: center;
          gap: 10px;
          color: var(--accent);
        }

        .profile-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
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

        .field input {
          background: var(--input-bg);
          border: 1px solid var(--input-border);
          color: var(--text-primary);
          padding: 12px 14px;
          border-radius: 14px;
          outline: none;
        }

        .readonly-box {
          background: var(--input-bg);
          border: 1px solid var(--border-color);
          color: var(--text-primary);
          padding: 12px 14px;
          border-radius: 14px;
        }

        .message {
          color: var(--accent);
          margin-bottom: 14px;
          font-weight: 600;
        }

        .loading-text,
        .empty-text {
          color: var(--text-secondary);
        }

        .save-btn,
        .refresh-btn {
          padding: 12px 16px;
          border-radius: 14px;
          cursor: pointer;
          font-weight: 700;
          display: inline-flex;
          align-items: center;
          gap: 8px;
          border: none;
          margin-right: 10px;
        }

        .save-btn {
          background: var(--button-secondary-bg);
          border: 1px solid var(--button-secondary-border);
          color: var(--button-secondary-text);
        }

        .refresh-btn {
          background: var(--input-bg);
          border: 1px solid var(--input-border);
          color: var(--text-primary);
        }

        @media (max-width: 900px) {
          .profile-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>

      <Menu />

      <main className="main-content">
        <h2 className="profile-title">
          <User size={24} />
          Teacher Profile
        </h2>

        {message && <div className="message">{message}</div>}

        {loading ? (
          <div className="loading-text">Loading profile...</div>
        ) : !profile ? (
          <div className="empty-text">No profile data found.</div>
        ) : (
          <div className="profile-grid">
            <div className="panel">
              <h3>Edit Basic Details</h3>

              <div className="field">
                <label>Full Name</label>
                <input
                  type="text"
                  name="fullName"
                  value={form.fullName}
                  onChange={handleChange}
                />
              </div>

              <div className="field">
                <label>Contact Number</label>
                <input
                  type="text"
                  name="contactNumber"
                  value={form.contactNumber}
                  onChange={handleChange}
                />
              </div>

              <button className="save-btn" onClick={handleSave} disabled={saving}>
                <Save size={16} />
                {saving ? "Saving..." : "Save Changes"}
              </button>

              <button className="refresh-btn" onClick={loadProfile}>
                <RefreshCcw size={16} />
                Refresh
              </button>
            </div>

            <div className="panel">
              <h3>Account Information</h3>

              <div className="field">
                <label>Username</label>
                <div className="readonly-box">{profile.username || "-"}</div>
              </div>

              <div className="field">
                <label>Email</label>
                <div className="readonly-box">{profile.email || "-"}</div>
              </div>

              <div className="field">
                <label>Role</label>
                <div className="readonly-box">{profile.role || "-"}</div>
              </div>

              <div className="field">
                <label>Status</label>
                <div className="readonly-box">{profile.status || "-"}</div>
              </div>

              <div className="field">
                <label>Created At</label>
                <div className="readonly-box">
                  {profile.createdAt ? new Date(profile.createdAt).toLocaleString() : "-"}
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default TeacherProfile;