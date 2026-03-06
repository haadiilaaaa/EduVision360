import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { login } from "../services/authService";
import { getUserRole } from "../utils/auth";
import { Mail, Lock, LogIn, ShieldCheck, Camera } from "lucide-react";

function Login() {
  const navigate = useNavigate();

  const [form, setForm] = useState({ identifier: "", password: "" });

  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);

  const [cameraOn, setCameraOn] = useState(false);
  const [loadingFace, setLoadingFace] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await login(form);
      localStorage.setItem("token", res.data.token);
      alert("Welcome back!");

      const role = getUserRole();
      if (role === "ADMIN") navigate("/admin");
      else if (role === "TEACHER") navigate("/teacher");
      else navigate("/student");
    } catch (err) {
      alert(err.response?.data?.message || "Login failed");
    }
  };

  // ✅ When cameraOn becomes true, attach the stream to the video element
  useEffect(() => {
    if (cameraOn && videoRef.current && streamRef.current) {
      videoRef.current.srcObject = streamRef.current;
    }
  }, [cameraOn]);

  const startCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: false,
      });

      streamRef.current = stream;
      setCameraOn(true); // render <video> first, then useEffect attaches stream
    } catch (err) {
      console.error("Camera error:", err?.name, err?.message, err);
      alert(`${err?.name || "CameraError"}: ${err?.message || "Unable to access camera"}`);
    }
  };

  const stopCamera = () => {
    streamRef.current?.getTracks()?.forEach((t) => t.stop());
    streamRef.current = null;

    if (videoRef.current) videoRef.current.srcObject = null;

    setCameraOn(false);
    setLoadingFace(false);
  };

  const captureBase64 = () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;

    if (!video || !canvas) throw new Error("Camera not initialized");

    // ✅ If stream not ready yet, prevent blank capture
    if (video.videoWidth === 0 || video.videoHeight === 0) {
      throw new Error("Camera not ready yet. Wait 1–2 seconds and try again.");
    }

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0);

    return canvas.toDataURL("image/jpeg"); // includes data:image/... prefix
  };

  const faceLogin = async () => {
    setLoadingFace(true);
    try {
      const imageBase64 = captureBase64();

      const res = await axios.post("http://localhost:8080/api/auth/face-login", {
        imageBase64,
      });

      localStorage.setItem("token", res.data.token);

      const role = getUserRole();
      if (role === "ADMIN") navigate("/admin");
      else if (role === "TEACHER") navigate("/teacher");
      else navigate("/student");
    } catch (err) {
      console.error("Face login error:", err?.response?.data || err);
      alert(err.response?.data?.message || err.message || "Face login failed");
    } finally {
      stopCamera();
    }
  };

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap');
        body { background: radial-gradient(circle at top right, #1a3a5a, #07121f); margin: 0; font-family: 'Inter', sans-serif; color: white; overflow: hidden; }
        .header { width: 100%; padding: 15px 50px; background: rgba(7, 18, 31, 0.8); backdrop-filter: blur(12px); display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid rgba(255, 213, 72, 0.2); position: fixed; top: 0; z-index: 100; }
        .header-title { font-size: 24px; font-weight: 700; color: #ffd548; text-transform: uppercase; letter-spacing: 2px; }
        .login-wrapper { min-height: 100vh; display: flex; justify-content: center; align-items: center; padding: 20px; }
        .login-card { width: 100%; max-width: 400px; padding: 40px; background: rgba(255, 255, 255, 0.03); border-radius: 24px; border: 1px solid rgba(255, 255, 255, 0.1); box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5); backdrop-filter: blur(20px); animation: slideUp 0.6s ease-out; }
        @keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
        .title-section { text-align: center; margin-bottom: 35px; }
        .title-section h2 { font-size: 32px; margin-bottom: 10px; background: linear-gradient(to right, #fff, #ffd548); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
        .input-group { position: relative; margin-bottom: 20px; }
        .input-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: rgba(255, 213, 72, 0.7); }
        .input-box { width: 100%; padding: 15px 15px 15px 45px; box-sizing: border-box; background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 12px; font-size: 15px; color: white; transition: all 0.3s ease; }
        .input-box:focus { outline: none; background: rgba(255, 255, 255, 0.1); border-color: #ffd548; box-shadow: 0 0 15px rgba(255, 213, 72, 0.2); }
        .btn { width: 100%; padding: 15px; background: #ffd548; border: none; border-radius: 12px; color: #000; font-weight: 700; font-size: 16px; cursor: pointer; transition: 0.3s; display: flex; align-items: center; justify-content: center; gap: 10px; margin-top: 10px; }
        .btn:hover { background: #ffc400; transform: translateY(-2px); box-shadow: 0 10px 20px rgba(255, 213, 72, 0.3); }
        .btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; box-shadow: none; }
        .btn-secondary { width: 100%; padding: 15px; border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 10px; margin-top: 10px;
          background: rgba(255,255,255,0.06); color: white; border: 1px solid rgba(255,255,255,0.12);
        }
        .footer { width: 100%; text-align: center; padding: 20px; background: rgba(7, 18, 31, 0.9); position: fixed; bottom: 0; font-size: 12px; color: #888; }
      `}</style>

      <div className="header">
        <div className="header-title">EduVision 360</div>
        <div style={{ fontSize: "12px", opacity: 0.7, display: "flex", alignItems: "center", gap: "5px" }}>
          <ShieldCheck size={14} color="#ffd548" /> Secure Portal
        </div>
      </div>

      <div className="login-wrapper">
        <div className="login-card">
          <div className="title-section">
            <h2>Welcome Back</h2>
            <p style={{ color: "#aaa", fontSize: "14px" }}>Enter your credentials to continue</p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="input-group">
              <Mail size={18} className="input-icon" />
              <input
                className="input-box"
                name="identifier"
                placeholder="Email or Username"
                value={form.identifier}
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
                value={form.password}
                onChange={handleChange}
                required
              />
            </div>

            <button className="btn" type="submit">
              <LogIn size={18} /> Sign In
            </button>
          </form>

          {/* ✅ Face login section */}
          <div style={{ marginTop: 14 }}>
            {!cameraOn ? (
              <button className="btn-secondary" type="button" onClick={startCamera}>
                <Camera size={18} /> Login with Face
              </button>
            ) : (
              <>
                <video
                  ref={videoRef}
                  autoPlay
                  playsInline
                  muted
                  style={{ width: "100%", borderRadius: 14, marginTop: 10 }}
                />
                <canvas ref={canvasRef} style={{ display: "none" }} />

                <div style={{ display: "flex", gap: 10, marginTop: 10 }}>
                  <button className="btn" type="button" onClick={faceLogin} disabled={loadingFace}>
                    <LogIn size={18} /> {loadingFace ? "Processing..." : "Capture & Login"}
                  </button>
                  <button className="btn-secondary" type="button" onClick={stopCamera} disabled={loadingFace}>
                    Close
                  </button>
                </div>

                <p style={{ marginTop: 10, fontSize: 12, opacity: 0.75 }}>
                  Tip: If the preview is black, wait 1–2 seconds, then click Capture.
                </p>
              </>
            )}
          </div>

          <p style={{ textAlign: "center", marginTop: "25px", fontSize: "14px", color: "#ccc" }}>
            Don't have an account?{" "}
            <span style={{ color: "#ffd548", cursor: "pointer", fontWeight: 600 }} onClick={() => navigate("/")}>
              Create one
            </span>
          </p>

          <p
            style={{ textAlign: "right", marginTop: "10px", fontSize: "13px", color: "#ffd548", cursor: "pointer", fontWeight: 500 }}
            onClick={() => navigate("/forgot-password")}
          >
            Forgot password?
          </p>
        </div>
      </div>

      <div className="footer">EduVision 360 — Intelligence in Education | © 2024</div>
    </>
  );
}

export default Login;