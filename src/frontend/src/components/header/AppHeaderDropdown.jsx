import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
  CDropdown,
  CDropdownDivider,
  CDropdownHeader,
  CDropdownItem,
  CDropdownMenu,
  CDropdownToggle,
} from '@coreui/react'
import { cilAccountLogout, cilUser, cilSettings } from '@coreui/icons'
import CIcon from '@coreui/icons-react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import ChangePassword from '../../app/change-password/ChangePassword'

const API_BASE = 'https://emms-system-production-4239.up.railway.app'
const fallbackLogo = '/logo.jpg'

const avatarStyle = {
  width: '42px',
  height: '42px',
  borderRadius: '12px',
  objectFit: 'cover',
  display: 'block',
  border: '2px solid #fff',
  boxShadow: '0 6px 14px rgba(0,0,0,0.12)',
}

const avatarTextStyle = {
  ...avatarStyle,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: 'linear-gradient(135deg,#2563eb,#315efb)',
  color: '#fff',
  fontWeight: 700,
  fontSize: '14px',
}

const api = axios.create({
  baseURL: API_BASE,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken') || ''
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const getStoredProfile = () => {
  try {
    return JSON.parse(localStorage.getItem('user_profile') || '{}')
  } catch {
    return {}
  }
}

const buildAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  if (avatar.startsWith('/')) return `${API_BASE}${avatar}`
  return `${API_BASE}/${avatar}`
}

const getInitials = (user) => {
  const first = user?.firstName?.charAt(0) || ''
  const last = user?.lastName?.charAt(0) || ''
  const username = user?.username?.charAt(0) || ''
  return `${first}${last}`.trim() || username.toUpperCase() || 'U'
}

const AppHeaderDropdown = () => {
  const navigate = useNavigate()

  const [user, setUser] = useState(getStoredProfile())
  const [imgError, setImgError] = useState(false)
  const [fallbackError, setFallbackError] = useState(false)
  const [openChangePassword, setOpenChangePassword] = useState(false)

  const avatarSrc = useMemo(() => {
    const raw = buildAvatarUrl(user?.avatar)
    if (!raw) return ''
    return `${raw}${raw.includes('?') ? '&' : '?'}t=${Date.now()}`
  }, [user?.avatar])

  const syncProfile = useCallback(async () => {
    try {
      const res = await api.get('/api/users/me')
      const fresh = res.data || {}

      setUser(fresh)
      localStorage.setItem('user_profile', JSON.stringify(fresh))
    } catch {
      setUser(getStoredProfile())
    }
  }, [])

  useEffect(() => {
    syncProfile()

    const updateProfile = () => {
      setUser(getStoredProfile())
      setImgError(false)
      setFallbackError(false)
    }

    window.addEventListener('user-profile-updated', updateProfile)
    window.addEventListener('storage', updateProfile)

    return () => {
      window.removeEventListener('user-profile-updated', updateProfile)
      window.removeEventListener('storage', updateProfile)
    }
  }, [syncProfile])

  const handleLogout = async () => {
    try {

    } catch (error) {
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user_profile')
      sessionStorage.clear()
      navigate('/login')
    }
  }

  const renderAvatar = () => {
    if (!imgError && avatarSrc) {
      return (
        <img
          src={avatarSrc}
          alt="avatar"
          style={avatarStyle}
          onError={() => setImgError(true)}
        />
      )
    }

    if (!fallbackError) {
      return (
        <img
          src={fallbackLogo}
          alt="avatar fallback"
          style={avatarStyle}
          onError={() => setFallbackError(true)}
        />
      )
    }

    return <div style={avatarTextStyle}>{getInitials(user)}</div>
  }

  return (
    <>
      <CDropdown variant="nav-item">
        <CDropdownToggle
          placement="bottom-end"
          className="py-0 pe-0 border-0 bg-transparent shadow-none"
          caret={false}
          style={{ padding: 0 }}
        >
          {renderAvatar()}
        </CDropdownToggle>

        <CDropdownMenu
          className="pt-0 shadow-lg border-0"
          placement="bottom-end"
        >
          <CDropdownHeader className="bg-body-secondary fw-semibold mb-2">
            Tài khoản
          </CDropdownHeader>

          <CDropdownItem onClick={() => navigate('/profile')}>
            <CIcon icon={cilUser} className="me-2" />
            Hồ sơ cá nhân
          </CDropdownItem>

          <CDropdownItem onClick={() => setOpenChangePassword(true)}>
            <CIcon icon={cilSettings} className="me-2" />
            Thay đổi mật khẩu
          </CDropdownItem>

          <CDropdownDivider />

          <CDropdownItem onClick={handleLogout}>
            <CIcon icon={cilAccountLogout} className="me-2" />
            Đăng xuất
          </CDropdownItem>
        </CDropdownMenu>
      </CDropdown>

      <ChangePassword
        open={openChangePassword}
        onClose={() => setOpenChangePassword(false)}
      />
    </>
  )
}

export default AppHeaderDropdown