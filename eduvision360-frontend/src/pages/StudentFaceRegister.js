import React, { useEffect, useRef, useState } from "react";

function StudentFaceRegister() {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);

  const [message, setMessage] = useState("");
  const [samples, setSamples] = useState([]);
  const [loading, setLoading] = useState(false);

  const MIN_SAMPLES = 3;
  const MAX_SAMPLES = 5;

  const startCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
      setMessage("📷 Camera started. Capture 3 to 5 samples.");
    } catch (error) {
      console.error(error);
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

  const captureSample = () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;

    if (!video?.srcObject) {
      setMessage("⚠️ Start the camera first");
      return;
    }

    if (samples.length >= MAX_SAMPLES) {
      setMessage(`⚠️ You can capture up to ${MAX_SAMPLES} samples only`);
      return;
    }

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    const ctx = canvas.getContext("2d");
    ctx.drawImage(video, 0, 0);

    canvas.toBlob(
      (blob) => {
        if (!blob) {
          setMessage("❌ Failed to capture image");
          return;
        }

        const previewUrl = URL.createObjectURL(blob);

        setSamples((prev) => [
          ...prev,
          {
            blob,
            previewUrl,
            name: `face-${prev.length + 1}.jpg`
          }
        ]);

        setMessage(`✅ Sample ${samples.length + 1} captured`);
      },
      "image/jpeg",
      0.95
    );
  };

  const removeSample = (indexToRemove) => {
    setSamples((prev) => {
      const item = prev[indexToRemove];
      if (item?.previewUrl) {
        URL.revokeObjectURL(item.previewUrl);
      }
      return prev.filter((_, index) => index !== indexToRemove);
    });
    setMessage("🗑️ Sample removed");
  };

  const resetSamples = () => {
    samples.forEach((sample) => {
      if (sample.previewUrl) {
        URL.revokeObjectURL(sample.previewUrl);
      }
    });
    setSamples([]);
    setMessage("♻️ Samples cleared");
  };

  const registerFace = async () => {
    if (samples.length < MIN_SAMPLES) {
      setMessage(`⚠️ Capture at least ${MIN_SAMPLES} samples before registering`);
      return;
    }

    try {
      setLoading(true);
      setMessage("");

      const formData = new FormData();
      samples.forEach((sample) => {
        formData.append("images", sample.blob, sample.name);
      });

      const token = localStorage.getItem("token");

      const response = await fetch("http://localhost:8080/api/attendance/register-face", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`
        },
        body: formData
      });

      const data = await response.json();

      if (!response.ok) {
        setMessage(`❌ ${data.message || data.detail || `Registration failed (HTTP ${response.status})`}`);
        return;
      }

      if (data.success) {
        setMessage(`✅ Face registered successfully with ${samples.length} samples`);
        stopCamera();
      } else {
        setMessage("❌ Face registration failed");
      }
    } catch (error) {
      console.error(error);
      setMessage("❌ Server not reachable");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    return () => {
      stopCamera();
      samples.forEach((sample) => {
        if (sample.previewUrl) {
          URL.revokeObjectURL(sample.previewUrl);
        }
      });
    };
  }, [samples]);

  return (
    <div className="student-face-register-page">
      <style>{`
        .student-face-register-page {
          padding: 20px;
          color: var(--text-main);
        }

        .register-panel {
          background: var(--bg-card);
          border: 1px solid var(--border-soft);
          border-radius: 20px;
          padding: 24px;
          max-width: 900px;
        }

        .title {
          margin-top: 0;
          font-size: 28px;
          background: linear-gradient(to right, var(--text-main), var(--accent), var(--text-main));
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        .hint {
          color: var(--text-muted);
          margin-bottom: 18px;
          line-height: 1.6;
        }

        .video-box {
          margin-bottom: 18px;
        }

        .video-box video {
          border-radius: 16px;
          border: 1px solid var(--border-color);
          width: 100%;
          max-width: 420px;
          background: black;
        }

        .btn-row {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
          margin-top: 14px;
          margin-bottom: 18px;
        }

        .btn {
          padding: 12px 16px;
          border-radius: 14px;
          font-weight: 700;
          cursor: pointer;
          border: none;
        }

        .primary-btn {
          background: var(--accent-soft);
          border: 1px solid rgba(255,213,72,0.35);
          color: var(--accent);
        }

        .secondary-btn {
          background: var(--bg-card-2);
          border: 1px solid var(--border-color);
          color: var(--text-main);
        }

        .danger-btn {
          background: rgba(248,113,113,0.10);
          border: 1px solid rgba(248,113,113,0.25);
          color: var(--danger);
        }

        .success-btn {
          background: rgba(74,222,128,0.12);
          border: 1px solid rgba(74,222,128,0.30);
          color: var(--success);
        }

        .message {
          margin: 10px 0 16px 0;
          font-weight: 600;
          color: var(--accent);
        }

        .count-box {
          margin-bottom: 14px;
          color: var(--text-main);
          font-weight: 600;
        }

        .samples-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
          gap: 14px;
          margin-top: 14px;
        }

        .sample-card {
          background: var(--bg-card-2);
          border: 1px solid var(--border-soft);
          border-radius: 16px;
          padding: 10px;
        }

        .sample-card img {
          width: 100%;
          height: 120px;
          object-fit: cover;
          border-radius: 12px;
          display: block;
          margin-bottom: 8px;
        }

        .sample-card button {
          width: 100%;
          padding: 8px 10px;
          border-radius: 10px;
          border: 1px solid rgba(248,113,113,0.25);
          background: rgba(248,113,113,0.10);
          color: var(--danger);
          cursor: pointer;
          font-weight: 700;
        }
      `}</style>

      <div className="register-panel">
        <h2 className="title">Register Face</h2>
        <p className="hint">
          Capture <strong>3 to 5 face samples</strong> for better recognition accuracy.
          Try slightly different angles and natural expressions.
        </p>

        <div className="video-box">
          <video ref={videoRef} autoPlay playsInline />
          <canvas ref={canvasRef} style={{ display: "none" }} />
        </div>

        <div className="btn-row">
          <button className="btn primary-btn" onClick={startCamera}>
            Start Camera
          </button>

          <button className="btn secondary-btn" onClick={captureSample}>
            Capture Sample
          </button>

          <button className="btn danger-btn" onClick={stopCamera}>
            Stop Camera
          </button>

          <button className="btn danger-btn" onClick={resetSamples}>
            Clear Samples
          </button>

          <button className="btn success-btn" onClick={registerFace} disabled={loading}>
            {loading ? "Registering..." : "Register Face"}
          </button>
        </div>

        <div className="count-box">
          Captured Samples: <strong>{samples.length}</strong> / {MAX_SAMPLES}
        </div>

        {message && <p className="message">{message}</p>}

        {samples.length > 0 && (
          <div className="samples-grid">
            {samples.map((sample, index) => (
              <div key={index} className="sample-card">
                <img src={sample.previewUrl} alt={`Sample ${index + 1}`} />
                <button onClick={() => removeSample(index)}>Remove</button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default StudentFaceRegister;