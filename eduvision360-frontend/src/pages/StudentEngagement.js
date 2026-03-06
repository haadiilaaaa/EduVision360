import React, { useEffect, useRef, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import {
  Camera,
  Square,
  Activity,
  Eye,
  AlertCircle,
  Brain,
  CheckCircle2
} from "lucide-react";

function StudentEngagement() {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const intervalRef = useRef(null);

  const { sessionId } = useParams();
  const location = useLocation();

  const sessionName = location.state?.sessionName || "Selected Session";
  const courseId = location.state?.courseId || "";

  const [message, setMessage] = useState("");
  const [cameraOn, setCameraOn] = useState(false);
  const [isMonitoring, setIsMonitoring] = useState(false);
  const [latest, setLatest] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    return () => {
      stopMonitoring();
    };
    // eslint-disable-next-line
  }, []);

  const startCamera = async () => {
    try {
      if (videoRef.current?.srcObject) return;

      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
      setCameraOn(true);
      setMessage("");
    } catch (err) {
      setMessage("❌ Unable to access camera");
    }
  };

  const stopCameraOnly = () => {
    const stream = videoRef.current?.srcObject;
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      videoRef.current.srcObject = null;
    }
    setCameraOn(false);
  };

  const stopMonitoring = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    setIsMonitoring(false);
    stopCameraOnly();
  };

  const sendFrame = async () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;

    if (!sessionId) {
      setMessage("⚠️ No session selected");
      return;
    }

    if (!video?.srcObject) {
      setMessage("⚠️ Camera not started");
      return;
    }

    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;

    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    canvas.toBlob(async (blob) => {
      if (!blob) {
        setMessage("❌ Failed to capture image");
        return;
      }

      const formData = new FormData();
      formData.append("image", blob, "engagement.jpg");
      formData.append("sessionId", sessionId);
      if (courseId) formData.append("courseId", courseId);

      const token = localStorage.getItem("token");

      try {
        const response = await fetch("http://localhost:8080/api/engagement/analyze", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        });

        const data = await response.json();

        if (!response.ok) {
          setMessage(`❌ ${data.message || data.detail || "Engagement analysis failed"}`);
          return;
        }

        setLatest(data);
        setHistory((prev) => [data, ...prev].slice(0, 6));
        setMessage(`✅ ${data.message}`);
      } catch (err) {
        console.error(err);
        setMessage("❌ Engagement analysis request failed");
      }
    }, "image/jpeg");
  };

  const startMonitoring = async () => {
    await startCamera();

    setIsMonitoring(true);
    setMessage("✅ Engagement monitoring started");

    await new Promise((resolve) => setTimeout(resolve, 800));
    sendFrame();

    if (intervalRef.current) clearInterval(intervalRef.current);
    intervalRef.current = setInterval(sendFrame, 8000);
  };

  const getMessageClass = () => {
    if (message.startsWith("✅")) return "success-message";
    if (message.startsWith("⚠️")) return "warning-message";
    if (message.startsWith("❌")) return "error-message";
    return "";
  };

  const getLabelClass = (label) => {
    if (label === "ATTENTIVE") return "label-attentive";
    if (label === "NEUTRAL") return "label-neutral";
    return "label-distracted";
  };

  return (
    <div className="engagement-page">
      <style>{`
        .engagement-page {
          animation: fadeIn 0.4s ease;
          color: var(--text-main);
        }

        .engagement-header {
          margin-bottom: 24px;
        }

        .engagement-header h2 {
          margin: 0 0 10px 0;
          font-size: 28px;
          display: flex;
          align-items: center;
          gap: 10px;
          background: linear-gradient(to right, var(--text-main), var(--accent));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .engagement-header p {
          margin: 0;
          color: var(--text-muted);
          font-size: 15px;
        }

        .engagement-grid {
          display: grid;
          grid-template-columns: 1.4fr 1fr;
          gap: 20px;
        }

        .engagement-card {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-top: 1px solid var(--border-strong);
          border-left: 1px solid var(--border-mid);
          border-radius: 24px;
          padding: 24px;
          box-shadow: var(--shadow-lg);
          backdrop-filter: blur(20px);
        }

        .session-chip {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 18px;
          padding: 8px 14px;
          border-radius: 999px;
          font-size: 13px;
          font-weight: 700;
          background: rgba(255, 213, 72, 0.08);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.22);
        }

        .video-wrapper {
          display: flex;
          justify-content: center;
          align-items: center;
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          border-radius: 22px;
          padding: 18px;
          min-height: 340px;
          overflow: hidden;
          margin-bottom: 22px;
          position: relative;
        }

        .video-frame {
          width: 100%;
          max-width: 520px;
          border-radius: 18px;
          border: 1px solid var(--border-color);
          background: #000;
        }

        .camera-placeholder {
          text-align: center;
          color: var(--text-dim);
          padding: 30px 20px;
        }

        .action-row {
          display: flex;
          gap: 12px;
          flex-wrap: wrap;
        }

        .action-btn {
          border: none;
          border-radius: 14px;
          padding: 12px 18px;
          font-weight: 700;
          cursor: pointer;
          display: inline-flex;
          align-items: center;
          gap: 8px;
          transition: 0.25s ease;
        }

        .start-btn {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.28);
        }

        .analyze-btn {
          background: var(--accent);
          color: #111827;
          border: 1px solid rgba(255, 213, 72, 0.4);
        }

        .stop-btn {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .action-btn:hover {
          transform: translateY(-2px);
          box-shadow: var(--shadow-md);
        }

        .message-box {
          margin-top: 18px;
          padding: 14px 16px;
          border-radius: 14px;
          font-size: 14px;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .success-message {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.28);
        }

        .warning-message {
          background: rgba(255, 213, 72, 0.10);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.25);
        }

        .error-message {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .stat-box {
          border: 1px solid var(--border-color);
          border-radius: 18px;
          padding: 16px;
          background: rgba(255,255,255,0.02);
          margin-bottom: 14px;
        }

        .stat-title {
          color: var(--text-muted);
          font-size: 13px;
          margin-bottom: 8px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: 800;
        }

        .label-chip {
          display: inline-flex;
          align-items: center;
          gap: 8px;
          padding: 8px 12px;
          border-radius: 999px;
          font-size: 13px;
          font-weight: 800;
        }

        .label-attentive {
          background: rgba(74, 222, 128, 0.12);
          color: var(--success);
          border: 1px solid rgba(74, 222, 128, 0.28);
        }

        .label-neutral {
          background: rgba(255, 213, 72, 0.10);
          color: var(--accent);
          border: 1px solid rgba(255, 213, 72, 0.25);
        }

        .label-distracted {
          background: rgba(248, 113, 113, 0.10);
          color: var(--danger);
          border: 1px solid rgba(248, 113, 113, 0.25);
        }

        .mini-list {
          display: flex;
          flex-direction: column;
          gap: 10px;
          margin-top: 14px;
        }

        .mini-item {
          border: 1px solid var(--border-color);
          border-radius: 14px;
          padding: 12px;
          background: rgba(255,255,255,0.02);
        }

        .mini-top {
          display: flex;
          justify-content: space-between;
          gap: 10px;
          margin-bottom: 8px;
          font-size: 13px;
          color: var(--text-muted);
        }

        .mini-main {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 10px;
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(12px); }
          to { opacity: 1; transform: translateY(0); }
        }

        @media (max-width: 900px) {
          .engagement-grid {
            grid-template-columns: 1fr;
          }
        }

        @media (max-width: 768px) {
          .action-row {
            flex-direction: column;
          }

          .action-btn {
            width: 100%;
            justify-content: center;
          }
        }
      `}</style>

      <div className="engagement-header">
        <h2>
          <Brain size={26} />
          Student Engagement Monitoring
        </h2>
        <p>Start the camera to analyze live engagement cues during the selected session.</p>
      </div>

      <div className="engagement-grid">
        <div className="engagement-card">
          <div className="session-chip">
            <CheckCircle2 size={14} />
            Session: {sessionName}
          </div>

          <div className="video-wrapper">
            <video ref={videoRef} autoPlay className="video-frame" />
            {!cameraOn && (
              <div className="camera-placeholder">
                <Camera size={42} />
                <div>Camera preview will appear here after you start monitoring.</div>
              </div>
            )}
            <canvas ref={canvasRef} style={{ display: "none" }} />
          </div>

          <div className="action-row">
            <button onClick={startMonitoring} className="action-btn start-btn">
              <Camera size={18} />
              Start Monitoring
            </button>

            <button onClick={sendFrame} className="action-btn analyze-btn">
              <Activity size={18} />
              Analyze Now
            </button>

            <button onClick={stopMonitoring} className="action-btn stop-btn">
              <Square size={18} />
              Stop Monitoring
            </button>
          </div>

          {message && (
            <div className={`message-box ${getMessageClass()}`}>
              <AlertCircle size={18} />
              <span>{message}</span>
            </div>
          )}
        </div>

        <div className="engagement-card">
          <div className="stat-box">
            <div className="stat-title">Monitoring Status</div>
            <div className="stat-value">{isMonitoring ? "ACTIVE" : "IDLE"}</div>
          </div>

          <div className="stat-box">
            <div className="stat-title">Latest Engagement Score</div>
            <div className="stat-value">
              {latest?.engagementScore != null ? latest.engagementScore : "--"}
            </div>
          </div>

          <div className="stat-box">
            <div className="stat-title">Latest Label</div>
            {latest?.label ? (
              <div className={`label-chip ${getLabelClass(latest.label)}`}>
                <Eye size={15} />
                {latest.label}
              </div>
            ) : (
              <div className="stat-value">--</div>
            )}
          </div>

          <div className="stat-box">
            <div className="stat-title">Signals</div>
            <div style={{ display: "grid", gap: "8px", fontSize: "14px" }}>
              <div>Face detected: {latest?.faceDetected ? "Yes" : "No"}</div>
              <div>Eyes detected: {latest?.eyesDetected ? "Yes" : "No"}</div>
              <div>Looking away: {latest?.lookingAway ? "Yes" : "No"}</div>
              <div>Confidence: {latest?.confidence != null ? Number(latest.confidence).toFixed(2) : "--"}</div>

              <div>Dominant emotion: {latest?.dominantEmotion || "--"}</div>
              <div>Emotion confidence: {latest?.emotionConfidence != null ? Number(latest.emotionConfidence).toFixed(2) : "--"}</div>
            </div>
          </div>

          <div className="stat-box">
            <div className="stat-title">Recent Samples</div>
            <div className="mini-list">
              {history.length === 0 && <div style={{ color: "var(--text-muted)" }}>No samples yet.</div>}
              {history.map((item, idx) => (
                <div className="mini-item" key={idx}>
                  <div className="mini-top">
                    <span>{item.capturedAt ? new Date(item.capturedAt).toLocaleTimeString() : "Now"}</span>
                    <span>
                      {item.dominantEmotion ? `Emo ${item.dominantEmotion}` : ""}
                      {item.confidence != null ? ` | Conf ${Number(item.confidence).toFixed(2)}` : ""}
                    </span>
                  </div>
                  <div className="mini-main">
                    <div className={`label-chip ${getLabelClass(item.label)}`}>{item.label}</div>
                    <strong>{item.engagementScore}</strong>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default StudentEngagement;