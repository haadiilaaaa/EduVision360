import React, { useRef, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import { Camera, ScanFace, Square, AlertCircle, CheckCircle2 } from "lucide-react";

function StudentAttendance() {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [message, setMessage] = useState("");

  const { sessionId } = useParams();
  const location = useLocation();

  const sessionName = location.state?.sessionName || "Selected Session";

  const startCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      videoRef.current.srcObject = stream;
      setMessage("");
    } catch (err) {
      setMessage("❌ Unable to access camera");
    }
  };

  const stopCamera = () => {
    const stream = videoRef.current?.srcObject;
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      videoRef.current.srcObject = null;
    }
  };

  const captureAndSend = async () => {
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

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0);

    canvas.toBlob(async (blob) => {
      if (!blob) {
        setMessage("❌ Failed to capture image");
        return;
      }

      const formData = new FormData();
      formData.append("image", blob, "face.jpg");
      formData.append("sessionId", sessionId);

      const token = localStorage.getItem("token");

      try {
        const response = await fetch("http://localhost:8080/api/attendance/mark", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        });

        if (!response.ok) {
          const error = await response.json();
          setMessage(`ℹ️ ${error.message || "Attendance request failed"}`);
          return;
        }

        const data = await response.json();

        if (data.success && data.status === "PRESENT") {
          setMessage("✅ Attendance marked successfully");
          stopCamera();
        } else if (data.status === "UNCERTAIN") {
          setMessage(`⚠️ ${data.message} (${data.confidence ?? "N/A"}%)`);
        } else {
          setMessage(`❌ ${data.message || "Face not recognized"}`);
        }
      } catch (err) {
        console.error(err);
        setMessage("❌ Attendance request failed");
      }
    }, "image/jpeg");
  };

  const getMessageClass = () => {
    if (message.startsWith("✅")) return "success-message";
    if (message.startsWith("⚠️") || message.startsWith("ℹ️")) return "warning-message";
    if (message.startsWith("❌")) return "error-message";
    return "";
  };

  return (
    <div className="attendance-page">
      <style>{`
        .attendance-page {
          animation: fadeIn 0.4s ease;
          color: var(--text-main);
        }

        .attendance-header {
          margin-bottom: 24px;
        }

        .attendance-header h2 {
          margin: 0 0 10px 0;
          font-size: 28px;
          display: flex;
          align-items: center;
          gap: 10px;
          background: linear-gradient(to right, var(--text-main), var(--accent));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .attendance-header p {
          margin: 0;
          color: var(--text-muted);
          font-size: 15px;
        }

        .attendance-card {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-top: 1px solid var(--border-strong);
          border-left: 1px solid var(--border-mid);
          border-radius: 24px;
          padding: 26px;
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
          box-shadow: 0 0 0 4px rgba(255, 213, 72, 0.04);
        }

        .camera-placeholder {
          text-align: center;
          color: var(--text-dim);
          padding: 30px 20px;
        }

        .camera-placeholder svg {
          margin-bottom: 12px;
          opacity: 0.5;
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

        .mark-btn {
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

        .help-text {
          margin-top: 20px;
          color: var(--text-muted);
          font-size: 14px;
          line-height: 1.7;
          background: rgba(255,255,255,0.02);
          border: 1px dashed var(--border-color);
          border-radius: 16px;
          padding: 16px;
        }

        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(12px); }
          to { opacity: 1; transform: translateY(0); }
        }

        @media (max-width: 768px) {
          .attendance-card {
            padding: 18px;
          }

          .attendance-header h2 {
            font-size: 24px;
          }

          .action-row {
            flex-direction: column;
          }

          .action-btn {
            width: 100%;
            justify-content: center;
          }

          .video-wrapper {
            min-height: 260px;
          }
        }
      `}</style>

      <div className="attendance-header">
        <h2>
          <ScanFace size={26} />
          Student Face Attendance
        </h2>
        <p>Start the camera, verify your face, and mark attendance for the selected session.</p>
      </div>

      <div className="attendance-card">
        <div className="session-chip">
          <CheckCircle2 size={14} />
          Session: {sessionName}
        </div>

        <div className="video-wrapper">
          <video ref={videoRef} autoPlay className="video-frame" />
          {!videoRef.current?.srcObject && (
            <div className="camera-placeholder">
              <Camera size={42} />
              <div>Camera preview will appear here after you start the camera.</div>
            </div>
          )}
          <canvas ref={canvasRef} style={{ display: "none" }} />
        </div>

        <div className="action-row">
          <button onClick={startCamera} className="action-btn start-btn">
            <Camera size={18} />
            Start Camera
          </button>

          <button onClick={captureAndSend} className="action-btn mark-btn">
            <ScanFace size={18} />
            Mark Attendance
          </button>

          <button onClick={stopCamera} className="action-btn stop-btn">
            <Square size={18} />
            Stop Camera
          </button>
        </div>

        {message && (
          <div className={`message-box ${getMessageClass()}`}>
            <AlertCircle size={18} />
            <span>{message}</span>
          </div>
        )}

        <div className="help-text">
          Make sure your face is clearly visible, the camera is stable, and the session is currently open before marking attendance.
        </div>
      </div>
    </div>
  );
}

export default StudentAttendance;