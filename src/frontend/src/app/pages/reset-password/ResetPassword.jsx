import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import "./ResetPassword.css";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      setStatus("error");
      setMessage("Liên kết đặt lại mật khẩu không hợp lệ.");
      return;
    }

    if (!password || !confirmPassword) {
      setStatus("error");
      setMessage("Vui lòng nhập đầy đủ mật khẩu.");
      return;
    }

    if (password.length < 6) {
      setStatus("error");
      setMessage("Mật khẩu phải có ít nhất 6 ký tự.");
      return;
    }

    if (password !== confirmPassword) {
      setStatus("error");
      setMessage("Mật khẩu xác nhận không khớp.");
      return;
    }

    try {
      setLoading(true);
      setMessage("");
      setStatus("");

      const res = await fetch(
        `http://localhost:8080/api/users/reset-password?token=${encodeURIComponent(
          token
        )}&newPassword=${encodeURIComponent(password)}`,
        { method: "POST" }
      );

      const text = await res.text();

      if (!res.ok) {
        setStatus("error");
        setMessage(text || "Liên kết không hợp lệ hoặc đã hết hạn.");
        return;
      }

      setStatus("success");
      setMessage("Đổi mật khẩu thành công. Đang chuyển về trang đăng nhập...");

      setTimeout(() => {
        navigate("/login");
      }, 2000);
    } catch (error) {
      console.error(error);
      setStatus("error");
      setMessage("Không thể kết nối đến máy chủ. Vui lòng thử lại sau.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="reset-page">
      <div className="reset-card">
        <div className="reset-header">
          <div className="reset-icon">🔑</div>
          <h1>Đặt lại mật khẩu</h1>
          <p>Nhập mật khẩu mới để tiếp tục sử dụng tài khoản của bạn.</p>
        </div>

        {message && <div className={`reset-alert ${status}`}>{message}</div>}

        <form onSubmit={handleSubmit} className="reset-form">
          <div className="form-group">
            <label>Mật khẩu mới</label>
            <input
              type="password"
              placeholder="Nhập mật khẩu mới"
              value={password}
              disabled={loading}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Xác nhận mật khẩu</label>
            <input
              type="password"
              placeholder="Nhập lại mật khẩu mới"
              value={confirmPassword}
              disabled={loading}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </div>

          <button type="submit" disabled={loading}>
            {loading ? "Đang xử lý..." : "Cập nhật mật khẩu"}
          </button>
        </form>

        <div className="reset-footer">
          <Link to="/login">← Quay lại đăng nhập</Link>
        </div>
      </div>
    </div>
  );
}