import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Mail, ArrowLeft, Send, ShieldCheck, CheckCircle, AlertCircle } from "lucide-react";

function ForgotPassword() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const res = await fetch(
        "http://localhost:8080/api/auth/password/request",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          body: new URLSearchParams({ email }),
        }
      );

      const data = await res.json();

      if (res.ok) {
        setMessage("Password reset link sent to your email");
        setIsError(false);
      } else {
        setMessage(data.message || "Failed to send link");
        setIsError(true);
      }
    } catch {
      setMessage("Failed to connect to the server");
      setIsError(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap');

        body {
          background: radial-gradient(circle at top right, #1a3a5a, #07121f);
          margin: 0;
          font-family: 'Inter', sans-serif;
          color: white;
          overflow: hidden;
        }

        .header {
          width: 100%;
          padding: 15px 50px;
          background: rgba(7, 18, 31, 0.8);
          backdrop-filter: blur(12px);
          display: flex;
          align-items: center;
          justify-content: space-between;
          border-bottom: 1px solid rgba(255, 213, 72, 0.2);
          position: fixed;
          top: 0;
          z-index: 100;
        }

        .header-title {
          font-size: 24px;
          font-weight: 700;
          color: #ffd548;
          text-transform: uppercase;
          letter-spacing: 2px;
        }

        .forgot-wrapper {
          min-height: 100vh;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 20px;
        }

        .forgot-card {
          width: 100%;
          max-width: 400px;
          padding: 40px;
          background: rgba(255, 255, 255, 0.03);
          border-radius: 24px;
          border: 1px solid rgba(255, 255, 255, 0.1);
          box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
          backdrop-filter: blur(20px);
          animation: fadeIn 0.6s ease-out;
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }

        .title-section {
          text-align: center;
          margin-bottom: 30px;
        }

        .title-section h2 {
          font-size: 28px;
          margin-bottom: 10px;
          background: linear-gradient(to right, #fff, #ffd548);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
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
          color: rgba(255, 213, 72, 0.7);
        }

        .input-box {
          width: 100%;
          padding: 15px 15px 15px 45px;
          box-sizing: border-box;
          background: rgba(255, 255, 255, 0.05);
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: 12px;
          font-size: 15px;
          color: white;
          transition: all 0.3s ease;
        }

        .input-box:focus {
          outline: none;
          background: rgba(255, 255, 255, 0.1);
          border-color: #ffd548;
          box-shadow: 0 0 15px rgba(255, 213, 72, 0.2);
        }

        .btn {
          width: 100%;
          padding: 15px;
          background: #ffd548;
          border: none;
          border-radius: 12px;
          color: #000;
          font-weight: 700;
          font-size: 16px;
          cursor: pointer;
          transition: 0.3s;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 10px;
          margin-top: 10px;
        }

        .btn:hover:not(:disabled) {
          background: #ffc400;
          transform: translateY(-2px);
          box-shadow: 0 10px 20px rgba(255, 213, 72, 0.3);
        }

        .btn:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .status-message {
          margin-top: 20px;
          padding: 12px;
          border-radius: 8px;
          font-size: 14px;
          display: flex;
          align-items: center;
          gap: 10px;
          background: rgba(255, 255, 255, 0.05);
        }

        .status-error { border: 1px solid #ff4d4d; color: #ff4d4d; }
        .status-success { border: 1px solid #4ade80; color: #4ade80; }

        .back-link {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 5px;
          margin-top: 25px;
          font-size: 14px;
          color: #aaa;
          cursor: pointer;
          transition: 0.3s;
        }

        .back-link:hover { color: #ffd548; }

        .footer {
          width: 100%;
          text-align: center;
          padding: 20px;
          background: rgba(7, 18, 31, 0.9);
          position: fixed;
          bottom: 0;
          font-size: 12px;
          color: #888;
        }
      `}</style>

      <div className="header">
        <div className="header-title">EduVision 360</div>
        <div style={{fontSize: '12px', opacity: 0.7, display: 'flex', alignItems: 'center', gap: '5px'}}>
          <ShieldCheck size={14} color="#ffd548" /> Security Center
        </div>
      </div>

      <div className="forgot-wrapper">
        <div className="forgot-card">
          <div className="title-section">
            <h2>Forgot Password?</h2>
            <p style={{color: '#aaa', fontSize: '14px'}}>No worries, we'll send you reset instructions.</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="input-group">
              <Mail size={18} className="input-icon" />
              <input
                className="input-box"
                type="email"
                placeholder="Registered Email Address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <button className="btn" type="submit" disabled={loading}>
              {loading ? "Sending Link..." : <><Send size={18} /> Send Reset Link</>}
            </button>
          </form>

          {message && (
            <div className={`status-message ${isError ? 'status-error' : 'status-success'}`}>
              {isError ? <AlertCircle size={16} /> : <CheckCircle size={16} />}
              {message}
            </div>
          )}

          <div className="back-link" onClick={() => navigate("/login")}>
            <ArrowLeft size={16} /> Back to Sign In
          </div>
        </div>
      </div>

      <div className="footer">
        EduVision 360 — Intelligence in Education | © 2024
      </div>
    </>
  );
}

export default ForgotPassword;