import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { FaEye, FaEyeSlash } from 'react-icons/fa'

const API_LOGIN_URL = 'http://localhost:8080/api/auth/login'

export default function Login() {
  const navigate = useNavigate()

  const [form, setForm] = useState({
    usernameOrEmail: '',
    password: '',
  })

  const [remember, setRemember] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const clearStoredAuth = () => {
    ;[
      'accessToken',
      'refreshToken',
      'token',
      'access_token',
      'user',
      'roles',
      'authorities',
      'permissions',
      'role',
    ].forEach((key) => {
      localStorage.removeItem(key)
      sessionStorage.removeItem(key)
    })
  }

  const saveAuth = ({
    accessToken,
    refreshToken,
    user,
    roles,
    authorities,
    permissions,
    role,
  }) => {
    const storage = remember ? localStorage : sessionStorage

    if (accessToken) storage.setItem('accessToken', accessToken)
    if (refreshToken) storage.setItem('refreshToken', refreshToken)

    storage.setItem('user', JSON.stringify(user || {}))
    storage.setItem('roles', JSON.stringify(roles || []))
    storage.setItem('authorities', JSON.stringify(authorities || []))
    storage.setItem('permissions', JSON.stringify(permissions || []))
    if (role) storage.setItem('role', role)

    // đồng bộ sang localStorage để các page khác đang đọc localStorage vẫn hoạt động
    if (accessToken) localStorage.setItem('accessToken', accessToken)
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('user', JSON.stringify(user || {}))
    localStorage.setItem('roles', JSON.stringify(roles || []))
    localStorage.setItem('authorities', JSON.stringify(authorities || []))
    localStorage.setItem('permissions', JSON.stringify(permissions || []))
    if (role) localStorage.setItem('role', role)
  }

  const extractErrorMessage = (err) => {
    const responseData = err?.response?.data

    if (typeof responseData === 'string' && responseData.trim()) {
      return responseData
    }

    if (responseData?.message) {
      return responseData.message
    }

    if (responseData?.error) {
      return responseData.error
    }

    if (err?.message) {
      return err.message
    }

    return 'Sai tài khoản hoặc mật khẩu'
  }

  const handleLogin = async () => {
    if (!form.usernameOrEmail.trim() || !form.password.trim()) {
      setError('Vui lòng nhập đầy đủ thông tin')
      return
    }

    try {
      setLoading(true)
      setError('')
      clearStoredAuth()

      const res = await axios.post(
        API_LOGIN_URL,
        {
          usernameOrEmail: form.usernameOrEmail.trim(),
          password: form.password,
        },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        },
      )

      const data = res?.data || {}
      console.log('LOGIN RESPONSE =', data)

      const accessToken =
        data.accessToken ||
        data.token ||
        data.jwt ||
        data.access_token ||
        ''

      const refreshToken =
        data.refreshToken ||
        data.refresh_token ||
        ''

      const user = data.user || {}

      const roles =
        data.roles ||
        user.roles ||
        []

      const authorities =
        data.authorities ||
        user.authorities ||
        []

      const permissions =
        data.permissions ||
        user.permissions ||
        []

      const role =
        data.role ||
        user.role ||
        ''

      if (!accessToken) {
        setError('Không nhận được access token từ server')
        return
      }

      saveAuth({
        accessToken,
        refreshToken,
        user,
        roles,
        authorities,
        permissions,
        role,
      })

      navigate('/assets/list', { replace: true })
    } catch (err) {
      console.error('LOGIN ERROR:', err?.response?.data || err.message)
      setError(extractErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !loading) {
      handleLogin()
    }
  }

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
            <label style={styles.label}>Tên đăng nhập hoặc email</label>
            <input
              name="usernameOrEmail"
              placeholder="Nhập username hoặc email"
              value={form.usernameOrEmail}
              onChange={handleChange}
              onKeyDown={handleKeyDown}
              style={styles.input}
              autoComplete="username"
              disabled={loading}
            />

            <label style={styles.label}>Mật khẩu</label>
            <div style={styles.passwordWrapper}>
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                placeholder="Nhập password"
                value={form.password}
                onChange={handleChange}
                onKeyDown={handleKeyDown}
                style={styles.passwordInput}
                autoComplete="current-password"
                disabled={loading}
              />
              <span
                style={styles.eyeIcon}
                onClick={() => setShowPassword((prev) => !prev)}
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </span>
            </div>

            <div style={styles.options}>
              <label style={styles.rememberLabel}>
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={() => setRemember((prev) => !prev)}
                  disabled={loading}
                />
                Ghi nhớ đăng nhập
              </label>

              <span
                style={styles.forgot}
                onClick={() => navigate('/forgot-password')}
              >
                Quên mật khẩu?
              </span>
            </div>

            <button
              onClick={handleLogin}
              style={{
                ...styles.btn,
                opacity: loading ? 0.75 : 1,
                cursor: loading ? 'not-allowed' : 'pointer',
              }}
              disabled={loading}
            >
              {loading ? 'ĐANG ĐĂNG NHẬP...' : 'ĐĂNG NHẬP VÀO HỆ THỐNG'}
            </button>
          </div>

          <p style={styles.noAccount}>
            Chưa có tài khoản?{' '}
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
  )
}

const styles = {
  container: {
    display: 'flex',
    height: '100vh',
    fontFamily: "'Inter', system-ui, sans-serif",
    overflow: 'hidden',
  },

  left: {
    flex: 1,
    background: '#ffffff',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    padding: '40px',
  },

  formWrapper: {
    width: '100%',
    maxWidth: '390px',
  },

  header: {
    marginBottom: '32px',
  },

  title: {
    fontSize: '28px',
    fontWeight: '700',
    color: '#111827',
    marginBottom: '8px',
  },

  subtitle: {
    fontSize: '15px',
    color: '#6b7280',
    lineHeight: '1.5',
  },

  formBox: {
    background: '#ffffff',
  },

  label: {
    fontSize: '13.5px',
    fontWeight: '500',
    color: '#374151',
    marginTop: '20px',
    marginBottom: '6px',
    display: 'block',
  },

  input: {
    width: '100%',
    padding: '13px 16px',
    borderRadius: '8px',
    border: '1px solid #d1d5db',
    outline: 'none',
    fontSize: '15px',
    backgroundColor: '#f9fafb',
    boxSizing: 'border-box',
  },

  passwordWrapper: {
    position: 'relative',
    width: '100%',
  },

  passwordInput: {
    width: '100%',
    padding: '13px 45px 13px 16px',
    borderRadius: '8px',
    border: '1px solid #d1d5db',
    outline: 'none',
    fontSize: '15px',
    backgroundColor: '#f9fafb',
    boxSizing: 'border-box',
  },

  eyeIcon: {
    position: 'absolute',
    right: '14px',
    top: '50%',
    transform: 'translateY(-50%)',
    cursor: 'pointer',
    color: '#6b7280',
    fontSize: '16px',
    display: 'flex',
    alignItems: 'center',
  },

  options: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '16px',
    fontSize: '13.8px',
  },

  rememberLabel: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    color: '#4b5563',
    cursor: 'pointer',
  },

  forgot: {
    color: '#2563eb',
    cursor: 'pointer',
    fontWeight: '500',
  },

  btn: {
    width: '100%',
    marginTop: '28px',
    padding: '14px',
    background: 'linear-gradient(135deg, #1e40af, #3b82f6)',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '15.5px',
    fontWeight: '600',
  },

  error: {
    background: '#fee2e2',
    color: '#dc2626',
    padding: '12px 14px',
    borderRadius: '8px',
    fontSize: '13.5px',
    marginBottom: '20px',
  },

  noAccount: {
    textAlign: 'center',
    marginTop: '32px',
    fontSize: '13.8px',
    color: '#6b7280',
  },

  contactAdmin: {
    color: '#2563eb',
    cursor: 'pointer',
    fontWeight: '500',
  },

  right: {
    flex: 1,
    position: 'relative',
    background:
      "url('https://images.unsplash.com/photo-1497366216548-37526070297c') center/cover no-repeat",
    minHeight: '100vh',
  },

  overlay: {
    position: 'absolute',
    inset: 0,
    background:
      'linear-gradient(135deg, rgba(30, 64, 175, 0.88), rgba(59, 130, 246, 0.85))',
  },

  content: {
    position: 'relative',
    zIndex: 2,
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',
    padding: '80px 70px',
    color: '#ffffff',
  },

  brandTitle: {
    fontSize: '38px',
    fontWeight: '700',
    lineHeight: '1.2',
    marginBottom: '24px',
  },

  brandDesc: {
    fontSize: '16px',
    lineHeight: '1.7',
    marginBottom: '48px',
    opacity: '0.95',
    maxWidth: '440px',
  },

  securityBadge: {
    display: 'flex',
    alignItems: 'center',
    gap: '14px',
    background: 'rgba(255,255,255,0.13)',
    backdropFilter: 'blur(10px)',
    padding: '16px 22px',
    borderRadius: '10px',
    width: 'fit-content',
    border: '1px solid rgba(255,255,255,0.25)',
  },

  shield: {
    fontSize: '29px',
  },

  securityTitle: {
    fontSize: '12.5px',
    fontWeight: '600',
    letterSpacing: '0.6px',
    opacity: '0.9',
  },

  securityStatus: {
    fontSize: '14.2px',
    fontWeight: '600',
  },
}