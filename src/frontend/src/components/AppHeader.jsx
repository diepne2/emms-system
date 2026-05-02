import React, { useCallback, useEffect, useRef, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import NotificationDropdown from '../app/notifications/NotificationDropdown'

import {
  CContainer,
  CDropdown,
  CDropdownItem,
  CDropdownMenu,
  CDropdownToggle,
  CHeader,
  CHeaderNav,
  CHeaderToggler,
  CNavLink,
  CNavItem,
  CBadge,
  useColorModes,
} from '@coreui/react'

import CIcon from '@coreui/icons-react'
import {
  cilMenu,
  cilEnvelopeOpen,
  cilMoon,
  cilSun,
  cilContrast,
} from '@coreui/icons'

import { AppBreadcrumb } from './index'
import { AppHeaderDropdown } from './header'

const API_BASE = 'https://emms-system-production-4239.up.railway.app/api/chat'

function getToken() {
  return (
    localStorage.getItem('token') ||
    localStorage.getItem('accessToken') ||
    localStorage.getItem('jwt')
  )
}

function getUserId(user) {
  return user?.userId ?? user?.id ?? user?.user_id ?? null
}

function getUserName(user) {
  return user?.fullName || user?.username || 'Unknown'
}

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)

  if (Number.isNaN(d.getTime())) return ''

  return d.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function apiRequest(path) {
  const token = getToken()

  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })

  if (!res.ok) {
    throw new Error(await res.text())
  }

  return res.json()
}

export default function AppHeader() {
  const headerRef = useRef(null)
  const navigate = useNavigate()
  const dispatch = useDispatch()

  const sidebarShow = useSelector((state) => state.sidebarShow)
  const { colorMode, setColorMode } = useColorModes('coreui-free-react-admin-template-theme')

  const [chatUsers, setChatUsers] = useState([])

  const chatNotifications = chatUsers.filter((user) => Number(user.unreadCount || 0) > 0)

  const msgCount = chatNotifications.reduce((total, user) => {
    return total + Number(user.unreadCount || 0)
  }, 0)

  const loadChatNotifications = useCallback(async () => {
    try {
      const data = await apiRequest('/users')
      const list = Array.isArray(data) ? data : []

      setChatUsers(list.filter((user) => getUserId(user) != null))
    } catch (error) {
      console.error('Không tải được thông báo chat:', error)
    }
  }, [])

  const openChat = (user) => {
    const userId = getUserId(user)

    if (!userId) return

    navigate(`/chat?userId=${userId}`)
  }

  useEffect(() => {
    loadChatNotifications()

    const interval = setInterval(loadChatNotifications, 3000)

    return () => clearInterval(interval)
  }, [loadChatNotifications])

  useEffect(() => {
    const handleScroll = () => {
      if (!headerRef.current) return

      headerRef.current.classList.toggle('shadow-sm', document.documentElement.scrollTop > 0)
    }

    document.addEventListener('scroll', handleScroll)
    return () => document.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <CHeader position="sticky" className="mb-2 p-0" ref={headerRef}>
      <CContainer fluid className="border-bottom px-4 py-2">
        <CHeaderToggler
          onClick={() => dispatch({ type: 'set', sidebarShow: !sidebarShow })}
          style={{ marginInlineStart: '-14px' }}
        >
          <CIcon icon={cilMenu} size="lg" />
        </CHeaderToggler>

        <CHeaderNav className="d-none d-md-flex">
          <CNavItem>
            <CNavLink to="/dashboard" as={NavLink}>
              Dashboard
            </CNavLink>
          </CNavItem>
        </CHeaderNav>

        <CHeaderNav className="ms-auto align-items-center gap-3">
          {/* THÔNG BÁO HỆ THỐNG THẬT */}
          <NotificationDropdown />

          {/* TIN NHẮN CHAT */}
          <CDropdown variant="nav-item" placement="bottom-end">
            <CDropdownToggle caret={false} className="position-relative">
              <CIcon icon={cilEnvelopeOpen} size="lg" />

              {msgCount > 0 && (
                <CBadge
                  color="danger"
                  shape="rounded-pill"
                  className="position-absolute top-0 start-100 translate-middle"
                >
                  {msgCount}
                </CBadge>
              )}
            </CDropdownToggle>

            <CDropdownMenu style={{ width: 360 }}>
              <div className="fw-bold text-center py-2 border-bottom">Tin nhắn</div>

              {chatNotifications.length > 0 ? (
                chatNotifications.map((user) => (
                  <CDropdownItem key={getUserId(user)} onClick={() => openChat(user)}>
                    <div className="d-flex justify-content-between gap-2">
                      <div className="fw-semibold">{getUserName(user)}</div>

                      <CBadge color="primary" shape="rounded-pill">
                        {Number(user.unreadCount || 0)}
                      </CBadge>
                    </div>

                    <small className="text-muted d-block text-truncate">
                      {user.lastMessage || `@${user.username || ''}`}
                    </small>

                    <div className="text-muted small">{formatTime(user.lastMessageAt)}</div>
                  </CDropdownItem>
                ))
              ) : (
                <div className="text-center text-muted py-3">Không có tin nhắn mới</div>
              )}

              <div className="text-center border-top">
                <CDropdownItem onClick={() => navigate('/chat')} className="text-primary fw-semibold">
                  Mở chat
                </CDropdownItem>
              </div>
            </CDropdownMenu>
          </CDropdown>

          {/* THEME */}
          <CDropdown variant="nav-item">
            <CDropdownToggle caret={false}>
              {colorMode === 'dark' ? (
                <CIcon icon={cilMoon} />
              ) : colorMode === 'auto' ? (
                <CIcon icon={cilContrast} />
              ) : (
                <CIcon icon={cilSun} />
              )}
            </CDropdownToggle>

            <CDropdownMenu>
              <CDropdownItem onClick={() => setColorMode('light')}>Light</CDropdownItem>
              <CDropdownItem onClick={() => setColorMode('dark')}>Dark</CDropdownItem>
              <CDropdownItem onClick={() => setColorMode('auto')}>Auto</CDropdownItem>
            </CDropdownMenu>
          </CDropdown>

          <AppHeaderDropdown />
        </CHeaderNav>
      </CContainer>

      <CContainer fluid className="px-4 py-1 border-bottom" style={{ marginTop: '-2px' }}>
        <AppBreadcrumb />
      </CContainer>
    </CHeader>
  )
}