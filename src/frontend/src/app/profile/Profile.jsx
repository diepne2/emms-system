import { useEffect, useMemo, useState } from "react"
import axios from "axios"
import "./profile.css"

const API_URL = "http://localhost:8080/api"
const SERVER_URL = "http://localhost:8080"

const Profile = () => {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    phone: "",
    jobTitle: "",
  })

  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState("")
  const [message, setMessage] = useState("")
  const [avatarPreview, setAvatarPreview] = useState("")
  const [avatarVersion, setAvatarVersion] = useState(Date.now())

  const getToken = () =>
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("jwt") ||
    localStorage.getItem("authToken") ||
    sessionStorage.getItem("token") ||
    sessionStorage.getItem("accessToken")

  const getAuthHeader = () => {
    const token = getToken()
    return token ? { Authorization: `Bearer ${token}` } : {}
  }

  const buildAvatarUrl = (avatar) => {
    if (!avatar) return ""

    if (avatar.startsWith("http")) {
      return avatar
    }

    if (avatar.startsWith("/api/")) {
      return `${SERVER_URL}${avatar}`
    }

    if (avatar.startsWith("/uploads/")) {
      const filename = avatar.split("/").pop()
      return `${SERVER_URL}/api/users/avatar/${filename}`
    }

    return `${SERVER_URL}${avatar}`
  }

  const avatarUrl = useMemo(() => {
    if (avatarPreview) return avatarPreview

    const raw = buildAvatarUrl(profile?.avatar)
    if (!raw) return ""

    return `${raw}${raw.includes("?") ? "&" : "?"}t=${avatarVersion}`
  }, [avatarPreview, profile?.avatar, avatarVersion])

  const getInitial = () => {
    return (
      profile?.firstName?.charAt(0) ||
      profile?.lastName?.charAt(0) ||
      profile?.username?.charAt(0) ||
      "U"
    ).toUpperCase()
  }

  const fetchProfile = async () => {
    setLoading(true)
    setError("")

    try {
      const res = await axios.get(`${API_URL}/users/me`, {
        headers: getAuthHeader(),
      })

      setProfile(res.data)
      setForm({
        firstName: res.data?.firstName || "",
        lastName: res.data?.lastName || "",
        phone: res.data?.phone || "",
        jobTitle: res.data?.jobTitle || "",
      })
    } catch (err) {
      console.error(err)
      setError("Không tải được thông tin người dùng. Vui lòng đăng nhập lại.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProfile()
  }, [])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSaveProfile = async (e) => {
    e.preventDefault()

    setSaving(true)
    setError("")
    setMessage("")

    try {
      const res = await axios.put(`${API_URL}/users/me`, form, {
        headers: {
          ...getAuthHeader(),
          "Content-Type": "application/json",
        },
      })

      setProfile(res.data)
      setMessage("Cập nhật thông tin thành công.")
    } catch (err) {
      console.error(err)
      setError("Cập nhật thông tin thất bại.")
    } finally {
      setSaving(false)
    }
  }

  const handleAvatarChange = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    setUploading(true)
    setError("")
    setMessage("")

    const previewUrl = URL.createObjectURL(file)
    setAvatarPreview(previewUrl)

    try {
      const formData = new FormData()
      formData.append("file", file)

      const res = await axios.post(`${API_URL}/users/me/avatar`, formData, {
        headers: {
          ...getAuthHeader(),
        },
      })

      setProfile(res.data)
      setAvatarVersion(Date.now())

      localStorage.setItem("user_profile", JSON.stringify(res.data))
      window.dispatchEvent(new Event("user-profile-updated"))



      setMessage("Upload avatar thành công.")
    } catch (err) {
      console.error(err)
      setError("Upload avatar thất bại.")
    } finally {
      setAvatarPreview("")
      setUploading(false)
      e.target.value = ""
      URL.revokeObjectURL(previewUrl)
    }
  }

  if (loading) {
    return (
      <div className="profile-page">
        <div className="profile-shell">
          <div className="profile-loading-card">Đang tải thông tin...</div>
        </div>
      </div>
    )
  }

  return (
    <div className="profile-page">
      <div className="profile-shell">
        <div className="profile-topbar">
          <div>
            <h1>Thông tin người dùng</h1>
          </div>
        </div>

        {error && <div className="profile-alert profile-alert-error">{error}</div>}
        {message && <div className="profile-alert profile-alert-success">{message}</div>}

        <div className="profile-grid">
          <aside className="profile-card profile-summary-card">
            <div className="profile-summary-top">
              <div className="profile-avatar-wrap">
                {avatarUrl ? (
                  <img
                    key={avatarUrl}
                    src={avatarUrl}
                    alt="Avatar"
                    className="avatar-img"
                    onError={() => {
                      console.log("Avatar load failed:", avatarUrl)
                    }}
                  />
                ) : (
                  <div className="profile-avatar">{getInitial()}</div>
                )}
              </div>

              <div className="profile-identity">
                <h2>
                  {`${profile?.firstName || ""} ${profile?.lastName || ""}`.trim() ||
                    profile?.username ||
                    "Người dùng"}
                </h2>
                <p>{profile?.email || "Chưa có email"}</p>
              </div>
            </div>

            <div className="profile-section-block">
              <div className="profile-section-title">Ảnh đại diện</div>

              <label className="profile-upload-box">
                <span>{uploading ? "Đang upload..." : "Chọn ảnh mới"}</span>
                <small>Hỗ trợ JPG, PNG, WEBP. Tối đa 5MB.</small>
                <input
                  type="file"
                  accept="image/png,image/jpeg,image/jpg,image/webp"
                  onChange={handleAvatarChange}
                  disabled={uploading}
                />
              </label>
            </div>

            <div className="profile-section-block">
              <div className="profile-section-title">Tài khoản</div>

              <div className="profile-info-list">
                <div className="profile-info-item">
                  <span className="profile-info-label">Username</span>
                  <strong>{profile?.username || "—"}</strong>
                </div>

                <div className="profile-info-item">
                  <span className="profile-info-label">Email</span>
                  <strong>{profile?.email || "—"}</strong>
                </div>

                <div className="profile-info-item">
                  <span className="profile-info-label">Vai trò</span>
                  <strong>{profile?.roleName || profile?.roleCode || "—"}</strong>
                </div>
              </div>
            </div>
          </aside>

          <main className="profile-main-stack">
            <form className="profile-card profile-form-card" onSubmit={handleSaveProfile}>
              <div className="profile-card-header">
                <div>
                  <h3>Hồ sơ cá nhân</h3>
                </div>
              </div>

              <div className="profile-form-grid">
                <div className="profile-form-group">
                  <label>Họ</label>
                  <input
                    name="firstName"
                    value={form.firstName}
                    onChange={handleChange}
                    placeholder="Nhập họ"
                  />
                </div>

                <div className="profile-form-group">
                  <label>Tên</label>
                  <input
                    name="lastName"
                    value={form.lastName}
                    onChange={handleChange}
                    placeholder="Nhập tên"
                  />
                </div>

                <div className="profile-form-group">
                  <label>Số điện thoại</label>
                  <input
                    name="phone"
                    value={form.phone}
                    onChange={handleChange}
                    placeholder="Nhập số điện thoại"
                  />
                </div>

                <div className="profile-form-group">
                  <label>Chức danh</label>
                  <input
                    name="jobTitle"
                    value={form.jobTitle}
                    onChange={handleChange}
                    placeholder="Nhập chức danh"
                  />
                </div>

                <div className="profile-form-group">
                  <label>Email</label>
                  <input value={profile?.email || ""} disabled readOnly />
                </div>

                <div className="profile-form-group">
                  <label>Tên đăng nhập</label>
                  <input value={profile?.username || ""} disabled readOnly />
                </div>
              </div>

              <div className="profile-form-footer">
                <button
                  type="submit"
                  className="profile-btn profile-btn-primary"
                  disabled={saving}
                >
                  {saving ? "Đang lưu..." : "Lưu thay đổi"}
                </button>
              </div>
            </form>
          </main>
        </div>
      </div>
    </div>
  )
}

export default Profile