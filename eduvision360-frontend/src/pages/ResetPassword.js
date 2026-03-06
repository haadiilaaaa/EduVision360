import { useSearchParams, useNavigate } from "react-router-dom";
import { useState } from "react";
import { Lock, CheckCircle, AlertCircle } from "lucide-react";

function ResetPassword() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  const token = params.get("token");

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);

  if (!token) {
    return (
      <>
        <style>{styles}</style>
        <div className="login-wrapper">
          <div className="login-card center">
            <AlertCircle size={48} className="error-icon" />
            <h2>Invalid Link</h2>
            <p>This password reset link is invalid or has expired.</p>
            <button className="btn" onClick={() => navigate("/login")}>
              Back to Login
            </button>
          </div>
        </div>
      </>
    );
  }

  const handleReset = async (e) => {
    e.preventDefault();

    if (newPassword !== confirmPassword) {
      setIsError(true);
      setMessage("Passwords do not match");
      return;
    }

    try {
      const res = await fetch(
        "http://localhost:8080/api/auth/password/reset",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: new URLSearchParams({
            token,
            newPassword,
          }),
        }
      );

      const data = await res.json();

      if (res.ok) {
        setIsError(false);
        setMessage("Password reset successful. Redirecting...");
        setTimeout(() => navigate("/login"), 2000);
      } else {
        setIsError(true);
        setMessage(data.message || "Reset failed");
      }
    } catch {
      setIsError(true);
      setMessage("Server error. Please try again.");
    }
  };

  return (
    <>
      <style>{styles}</style>

      <div className="login-wrapper">
        <div className="login-card">
          <h2 className="title">Reset Password</h2>

          <form onSubmit={handleReset}>
            <div className="input-group">
              <Lock size={18} className="input-icon" />
              <input
                type="password"
                className="input-box"
                placeholder="New Password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
            </div>

            <div className="input-group">
              <Lock size={18} className="input-icon" />
              <input
                type="password"
                className="input-box"
                placeholder="Confirm Password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="btn">
              Update Password
            </button>
          </form>

          {message && (
            <div
              className={`status-message ${
                isError ? "status-error" : "status-success"
              }`}
            >
              {isError ? <AlertCircle size={16} /> : <CheckCircle size={16} />}
              {message}
            </div>
          )}
        </div>
      </div>
    </>
  );
}

const styles = `
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap');

body {
  margin: 0;
  font-family: 'Inter', sans-serif;
  background: radial-gradient(circle at top right, #1a3a5a, #07121f);
  color: white;
}

.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow:
    0 30px 60px rgba(0, 0, 0, 0.6),
    0 0 40px rgba(255, 213, 72, 0.08);
}

.center {
  text-align: center;
}

.title {
  text-align: center;
  font-size: 26px;
  margin-bottom: 30px;
}

.input-group {
  position: relative;
  margin-bottom: 20px;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 213, 72, 0.8);
}

.input-box {
  width: 80%;
  padding: 15px 15px 15px 45px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: white;
  font-size: 15px;
  line-height: 1.4;
}

.input-box::placeholder {
  color: rgba(255, 255, 255, 0.55);
}

.input-box:focus {
  outline: none;
  border-color: #ffd548;
  box-shadow: 0 0 14px rgba(255, 213, 72, 0.35);
}

.btn {
  width: 100%;
  padding: 15px;
  margin-top: 10px;
  background: #ffd548;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.25s ease;
}

.btn:hover {
  background: #ffc400;
  transform: translateY(-2px);
}

.status-message {
  margin-top: 20px;
  padding: 12px;
  border-radius: 10px;
  display: flex;
  gap: 10px;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
}

.status-error {
  border: 1px solid #ff4d4d;
  color: #ff4d4d;
}

.status-success {
  border: 1px solid #4ade80;
  color: #4ade80;
}

.error-icon {
  color: #ff4d4d;
  margin-bottom: 15px;
}
`;

export default ResetPassword;
