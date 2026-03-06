import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../services/authService";
import { User, Mail, Lock, Phone, UserCircle } from "lucide-react";

function Register() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: "",
    username: "",
    email: "",
    password: "",
    contactNumber: "",
    role: "STUDENT",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;

    // ✅ keep contact number digits-only + max 10
    if (name === "contactNumber") {
      const digits = value.replace(/\D/g, "").slice(0, 10);
      setForm((prev) => ({ ...prev, [name]: digits }));
      return;
    }

    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      ...form,
      fullName: form.fullName.trim(),
      username: form.username.trim(),
      email: form.email.trim(),
      contactNumber: form.contactNumber.trim(),
    };

    // quick frontend validation to match backend regex ^[0-9]{10}$
    if (!/^[0-9]{10}$/.test(payload.contactNumber)) {
      alert("Contact number must be exactly 10 digits");
      return;
    }

    try {
      const res = await registerUser(payload);

      // backend returns ApiResponse { message: ... } (or error)
      alert(res?.data?.message || "OTP sent to email");
      navigate("/verify-otp", { state: { email: payload.email } });
    } catch (err) {
      alert(err.response?.data?.message || "Registration failed");
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
          overflow-x: hidden;
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

        .register-wrapper {
          min-height: 100vh;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 120px 20px 80px 20px;
        }

        .register-card {
          width: 100%;
          max-width: 420px;
          padding: 40px;
          background: rgba(255, 255, 255, 0.03);
          border-radius: 24px;
          border: 1px solid rgba(255, 255, 255, 0.1);
          box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
          backdrop-filter: blur(20px);
        }

        .title-section {
          text-align: center;
          margin-bottom: 30px;
        }

        .title-section h2 {
          font-size: 28px;
          margin-bottom: 8px;
          background: linear-gradient(to right, #fff, #ffd548);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .input-group {
          position: relative;
          margin-bottom: 18px;
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
          padding: 14px 14px 14px 45px;
          box-sizing: border-box;
          background: rgba(255, 255, 255, 0.05);
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: 12px;
          font-size: 14px;
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
          margin-top: 10px;
        }

        .btn:hover {
          background: #ffc400;
          transform: translateY(-2px);
          box-shadow: 0 10px 20px rgba(255, 213, 72, 0.3);
        }

        .helper-text {
          text-align: center;
          font-size: 14px;
          color: #ccc;
          margin-top: 15px;
        }

        .accent-link {
          color: #ffd548;
          cursor: pointer;
          font-weight: 600;
          text-decoration: none;
        }

        .accent-link:hover {
          text-decoration: underline;
        }

        .divider {
          height: 1px;
          background: rgba(255, 255, 255, 0.1);
          margin: 20px 0;
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
        <div>
          <div className="header-title">EduVision 360</div>
        </div>
        <div style={{ fontSize: "12px", opacity: 0.7 }}>v1.0 Powered by AI</div>
      </div>

      <div className="register-wrapper">
        <div className="register-card">
          <div className="title-section">
            <h2>Get Started</h2>
            <p style={{ color: "#aaa", fontSize: "14px" }}>
              Join our intelligent learning ecosystem
            </p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="input-group">
              <User size={18} className="input-icon" />
              <input
                className="input-box"
                name="fullName"
                placeholder="Full Name"
                onChange={handleChange}
                required
              />
            </div>

            <div className="input-group">
              <UserCircle size={18} className="input-icon" />
              <input
                className="input-box"
                name="username"
                placeholder="Username"
                onChange={handleChange}
                required
              />
            </div>

            <div className="input-group">
              <Mail size={18} className="input-icon" />
              <input
                className="input-box"
                type="email"
                name="email"
                placeholder="Email Address"
                onChange={handleChange}
                required
              />
            </div>

            <div className="input-group">
              <Lock size={18} className="input-icon" />
              <input
                className="input-box"
                type="password"
                name="password"
                placeholder="Password"
                onChange={handleChange}
                required
                minLength={8}
              />
            </div>

            <div className="input-group">
              <Phone size={18} className="input-icon" />
              <input
                className="input-box"
                name="contactNumber"
                placeholder="Contact Number"
                onChange={handleChange}
                required
                inputMode="numeric"
                pattern="^[0-9]{10}$"
                title="Contact number must be exactly 10 digits"
              />
            </div>

            <button className="btn" type="submit">
              Create Account
            </button>
          </form>

          <div className="helper-text">
            Already a member?{" "}
            <span className="accent-link" onClick={() => navigate("/login")}>
              Sign In
            </span>
          </div>

          <div className="divider"></div>

          <div className="helper-text">
            Are you a teacher?{" "}
            <Link to="/apply-teacher" className="accent-link">
              Apply here
            </Link>
          </div>
        </div>
      </div>

      <div className="footer">EduVision 360 — Intelligence in Education | © 2024</div>
    </>
  );
}

export default Register;