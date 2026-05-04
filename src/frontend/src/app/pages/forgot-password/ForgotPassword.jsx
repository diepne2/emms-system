import { useState } from "react";
import { Link } from "react-router-dom";
import "./ForgotPassword.css";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [status, setStatus] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!email.trim()) {
      setStatus("error");
      setMessage("Vui lòng nhập địa chỉ email.");
      return;
    }

    try {
      setLoading(true);
      setMessage("");
      setStatus("");

      const res = await fetch(
        `https://emms-system-production-4239.up.railway.app/api/users/forgot-password?email=${encodeURIComponent(email)}`,
        { method: "POST" }
      );

      const text = await res.text();

      if (!res.ok) {
        setStatus("error");
        setMessage(text || "Không thể gửi liên kết đặt lại mật khẩu.");
        return;
      }

      setStatus("success");
      setMessage("Liên kết đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email hoặc thư rác.");
    } catch (error) {
      console.error(error);
      setStatus("error");
      setMessage("Không thể kết nối đến máy chủ. Vui lòng thử lại sau.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="forgot-page">
      <div className="forgot-card">
        <div className="forgot-header">
          <div className="forgot-icon">🔐</div>
          <h1>Quên mật khẩu?</h1>
          <p>
            Nhập email đã đăng ký. Chúng tôi sẽ gửi liên kết để bạn đặt lại mật khẩu.
          </p>
        </div>

        {message && (
          <div className={`forgot-alert ${status}`}>
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="forgot-form">
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              placeholder="Nhập email"
              value={email}
              disabled={loading}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <button type="submit" disabled={loading}>
            {loading ? "Đang gửi..." : "Gửi liên kết đặt lại mật khẩu"}
          </button>
        </form>

        <div className="forgot-footer">
          <Link to="/login">← Quay lại đăng nhập</Link>
        </div>
      </div>
    </div>
  );
}