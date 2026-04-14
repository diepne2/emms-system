import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function Login() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: "",
    password: "",
  });

  const [remember, setRemember] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleLogin = async () => {
    if (!form.username || !form.password) {
      setError("Vui lòng nhập đầy đủ thông tin");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const res = await axios.post(
        "http://localhost:8080/api/v1/auth/login",
        {
          username: form.username,
          password: form.password,
        },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      const { accessToken, refreshToken } = res.data.data;

      if (!accessToken || !refreshToken) {
        setError("Không nhận được token từ server");
        return;
      }

      if (remember) {
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);
      } else {
        sessionStorage.setItem("accessToken", accessToken);
        sessionStorage.setItem("refreshToken", refreshToken);
      }

      navigate("/dashboard");
    } catch (err) {
      console.log("LOGIN ERROR:", err?.response?.data || err.message);

      setError(
        err?.response?.data?.message || "Sai tên đăng nhập hoặc mật khẩu"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.left}>
        <div style={styles.formWrapper}>
          <div style={styles.header}>
            <h1 style={styles.title}>WELCOME BACK</h1>
            <p style={styles.subtitle}>
              Truy cập hệ thống quản lý thiết bị và bảo trì
            </p>
          </div>

          {error && <div style={styles.error}>{error}</div>}

          <div style={styles.formBox}>
            <label style={styles.label}>Tên đăng nhập</label>
            <input
              name="username"
              placeholder="Nhập username"
              value={form.username}
              onChange={handleChange}
              style={styles.input}
            />

            <label style={styles.label}>Mật khẩu</label>
            <div style={styles.passwordWrapper}>
              <input
                type={showPassword ? "text" : "password"}
                name="password"
                placeholder="Nhập password"
                value={form.password}
                onChange={handleChange}
                style={styles.passwordInput}
              />
              <span
                style={styles.eyeIcon}
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </span>
            </div>

            <div style={styles.options}>
              <label style={styles.rememberLabel}>
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={() => setRemember(!remember)}
                />
                Ghi nhớ đăng nhập
              </label>
              <span style={styles.forgot}>Quên mật khẩu?</span>
            </div>

            <button onClick={handleLogin} style={styles.btn} disabled={loading}>
              {loading ? "" : "ĐĂNG NHẬP VÀO HỆ THỐNG"}
            </button>
          </div>

          <p style={styles.noAccount}>
            Chưa có tài khoản?{" "}
            <span style={styles.contactAdmin}>Liên hệ quản trị viên</span>
          </p>
        </div>
      </div>

      <div style={styles.right}>
        <div style={styles.overlay} />

        <div style={styles.content}>
          <h2 style={styles.brandTitle}>
            Quản lý thiết bị
            <br />
            và bảo trì thông minh
          </h2>
          <p style={styles.brandDesc}>
            Hệ thống CMMS giúp theo dõi thiết bị, lập kế hoạch bảo trì, giảm
            thời gian ngừng máy và tối ưu chi phí vận hành cho doanh nghiệp Việt
            Nam.
          </p>

          <div style={styles.securityBadge}>
            <span style={styles.shield}>🛡️</span>
            <div>
              <div style={styles.securityTitle}>TRẠNG THÁI BẢO MẬT</div>
              <div style={styles.securityStatus}>
                Giao thức bảo mật cao đang hoạt động
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: "flex",
    height: "100vh",
    fontFamily: "'Inter', system-ui, sans-serif",
    overflow: "hidden",
  },

  left: {
    flex: 1,
    background: "#ffffff",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    padding: "40px",
  },

  formWrapper: {
    width: "100%",
    maxWidth: "390px",
  },

  header: {
    marginBottom: "32px",
  },

  title: {
    fontSize: "28px",
    fontWeight: "700",
    color: "#111827",
    marginBottom: "8px",
  },

  subtitle: {
    fontSize: "15px",
    color: "#6b7280",
    lineHeight: "1.5",
  },

  formBox: {
    background: "#ffffff",
  },

  label: {
    fontSize: "13.5px",
    fontWeight: "500",
    color: "#374151",
    marginTop: "20px",
    marginBottom: "6px",
    display: "block",
  },

  input: {
    width: "100%",
    padding: "13px 16px",
    borderRadius: "8px",
    border: "1px solid #d1d5db",
    outline: "none",
    fontSize: "15px",
    backgroundColor: "#f9fafb",
    boxSizing: "border-box",
  },

  passwordWrapper: {
    position: "relative",
    width: "100%",
  },

  passwordInput: {
    width: "100%",
    padding: "13px 45px 13px 16px",
    borderRadius: "8px",
    border: "1px solid #d1d5db",
    outline: "none",
    fontSize: "15px",
    backgroundColor: "#f9fafb",
    boxSizing: "border-box",
  },

  eyeIcon: {
    position: "absolute",
    right: "14px",
    top: "50%",
    transform: "translateY(-50%)",
    cursor: "pointer",
    color: "#6b7280",
    fontSize: "16px",
    display: "flex",
    alignItems: "center",
  },

  options: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginTop: "16px",
    fontSize: "13.8px",
  },

  rememberLabel: {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    color: "#4b5563",
    cursor: "pointer",
  },

  forgot: {
    color: "#2563eb",
    cursor: "pointer",
    fontWeight: "500",
  },

  btn: {
    width: "100%",
    marginTop: "28px",
    padding: "14px",
    background: "linear-gradient(135deg, #1e40af, #3b82f6)",
    color: "#fff",
    border: "none",
    borderRadius: "8px",
    fontSize: "15.5px",
    fontWeight: "600",
    cursor: "pointer",
  },

  error: {
    background: "#fee2e2",
    color: "#dc2626",
    padding: "12px 14px",
    borderRadius: "8px",
    fontSize: "13.5px",
    marginBottom: "20px",
  },

  noAccount: {
    textAlign: "center",
    marginTop: "32px",
    fontSize: "13.8px",
    color: "#6b7280",
  },

  contactAdmin: {
    color: "#2563eb",
    cursor: "pointer",
    fontWeight: "500",
  },

  right: {
    flex: 1,
    position: "relative",
    background:
      "url('https://images.unsplash.com/photo-1497366216548-37526070297c') center/cover no-repeat",
    minHeight: "100vh",
  },

  overlay: {
    position: "absolute",
    inset: 0,
    background:
      "linear-gradient(135deg, rgba(30, 64, 175, 0.88), rgba(59, 130, 246, 0.85))",
  },

  content: {
    position: "relative",
    zIndex: 2,
    height: "100%",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    padding: "80px 70px",
    color: "#ffffff",
  },

  brandTitle: {
    fontSize: "38px",
    fontWeight: "700",
    lineHeight: "1.2",
    marginBottom: "24px",
  },

  brandDesc: {
    fontSize: "16px",
    lineHeight: "1.7",
    marginBottom: "48px",
    opacity: "0.95",
    maxWidth: "440px",
  },

  securityBadge: {
    display: "flex",
    alignItems: "center",
    gap: "14px",
    background: "rgba(255,255,255,0.13)",
    backdropFilter: "blur(10px)",
    padding: "16px 22px",
    borderRadius: "10px",
    width: "fit-content",
    border: "1px solid rgba(255,255,255,0.25)",
  },

  shield: {
    fontSize: "29px",
  },

  securityTitle: {
    fontSize: "12.5px",
    fontWeight: "600",
    letterSpacing: "0.6px",
    opacity: "0.9",
  },

  securityStatus: {
    fontSize: "14.2px",
    fontWeight: "600",
  },
};