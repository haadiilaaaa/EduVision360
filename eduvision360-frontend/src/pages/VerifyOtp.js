import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { verifyOtp, resendOtp } from "../services/authService";
import { ShieldCheck, RefreshCw, CheckCircle, MailOpen } from "lucide-react";

function VerifyOtp() {
  const location = useLocation();
  const navigate = useNavigate();

  const email = location.state?.email || "";
  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);

  const handleVerify = async () => {
    if (!otp) return alert("Please enter the OTP");
    setLoading(true);
    try {
      await verifyOtp({ email, otp });
      alert("OTP verified successfully");
      navigate("/login");
    } catch (err) {
      alert(err.response?.data?.message || "Invalid OTP");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    try {
      await resendOtp(email);
      alert("A new OTP has been sent to your email.");
    } catch (err) {
      alert(err.response?.data?.message || "Failed to resend OTP");
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

        .verify-wrapper {
          min-height: 100vh;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 20px;
        }

        .verify-card {
          width: 100%;
          max-width: 400px;
          padding: 40px;
          background: rgba(255, 255, 255, 0.03);
          border-radius: 24px;
          border: 1px solid rgba(255, 255, 255, 0.1);
          box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
          backdrop-filter: blur(20px);
          text-align: center;
          animation: slideIn 0.5s ease-out;
        }

        @keyframes slideIn {
          from { opacity: 0; transform: scale(0.95); }
          to { opacity: 1; transform: scale(1); }
        }

        .icon-circle {
          width: 70px;
          height: 70px;
          background: rgba(255, 213, 72, 0.1);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0 auto 20px;
          border: 1px solid rgba(255, 213, 72, 0.3);
        }

        .email-display {
          color: #ffd548;
          font-weight: 600;
          background: rgba(255, 213, 72, 0.1);
          padding: 4px 12px;
          border-radius: 20px;
          font-size: 14px;
          display: inline-block;
          margin-bottom: 25px;
        }

        .otp-input {
          width: 100%;
          padding: 15px;
          background: rgba(255, 255, 255, 0.05);
          border: 2px solid rgba(255, 255, 255, 0.1);
          border-radius: 12px;
          font-size: 24px;
          color: white;
          text-align: center;
          letter-spacing: 8px;
          font-weight: 700;
          transition: all 0.3s ease;
          margin-bottom: 20px;
        }

        .otp-input:focus {
          outline: none;
          border-color: #ffd548;
          background: rgba(255, 255, 255, 0.1);
          box-shadow: 0 0 20px rgba(255, 213, 72, 0.2);
        }

        .btn-primary {
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
        }

        .btn-primary:hover:not(:disabled) {
          background: #ffc400;
          transform: translateY(-2px);
        }

        .btn-secondary {
          background: transparent;
          border: 1px solid rgba(255, 255, 255, 0.2);
          color: white;
          width: 100%;
          padding: 12px;
          border-radius: 12px;
          margin-top: 15px;
          cursor: pointer;
          font-size: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          transition: 0.3s;
        }

        .btn-secondary:hover {
          background: rgba(255, 255, 255, 0.05);
          border-color: white;
        }

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
          <ShieldCheck size={14} color="#ffd548" /> Verification Phase
        </div>
      </div>

      <div className="verify-wrapper">
        <div className="verify-card">
          <div className="icon-circle">
            <MailOpen size={32} color="#ffd548" />
          </div>
          <h2 style={{margin: '0 0 10px 0'}}>Verify Identity</h2>
          <p style={{color: '#aaa', fontSize: '14px', margin: '0 0 15px 0'}}>We've sent a 6-digit code to</p>
          <div className="email-display">{email || "your email"}</div>

          <input 
            className="otp-input" 
            placeholder="······" 
            maxLength={6}
            onChange={(e) => setOtp(e.target.value)} 
          />

          <button className="btn-primary" onClick={handleVerify} disabled={loading}>
            {loading ? "Verifying..." : <><CheckCircle size={18} /> Verify Code</>}
          </button>

          <button className="btn-secondary" onClick={handleResend}>
            <RefreshCw size={16} /> Resend OTP
          </button>
        </div>
      </div>

      <div className="footer">
        EduVision 360 — Intelligence in Education | © 2024
      </div>
    </>
  );
}

export default VerifyOtp;