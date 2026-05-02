import React, { useCallback, useEffect, useState } from 'react'
import {
  CDropdown,
  CDropdownHeader,
  CDropdownItem,
  CDropdownMenu,
  CDropdownToggle,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilBell } from '@coreui/icons'
import './noti.css'

const API = 'https://emms-system-production-4239.up.railway.app/api/notifications'

function getToken() {
  const raw =
    localStorage.getItem('accessToken') ||
    localStorage.getItem('token') ||
    localStorage.getItem('jwt')

  if (!raw) return null

  try {
    const parsed = JSON.parse(raw)
    return parsed.accessToken || parsed.token || parsed.jwt || null
  } catch {
    return raw
  }
}

function authHeader() {
  const token = getToken()

  if (!token || token.split('.').length !== 3) {
    return {}
  }

  return {
    Authorization: `Bearer ${token}`,
  }
}

async function request(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeader(),
      ...(options.headers || {}),
    },
  })

  if (res.status === 401) {
    console.error('Unauthorized: token sai hoặc hết hạn')
    return null
  }

  if (!res.ok) {
    throw new Error(await res.text())
  }

  if (res.status === 204) return null

  return res.json()
}

export default function NotificationDropdown() {
  const [items, setItems] = useState([])
  const [unread, setUnread] = useState(0)

  const loadNotifications = useCallback(async () => {
    try {
      const data = await request(API)
      setItems(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error('Không tải được thông báo:', error)
    }
  }, [])

  const loadUnread = useCallback(async () => {
    try {
      const data = await request(`${API}/unread-count`)
      setUnread(Number(data?.unreadCount || 0))
    } catch (error) {
      console.error('Không tải được số thông báo chưa đọc:', error)
    }
  }, [])

  const reload = useCallback(() => {
    loadNotifications()
    loadUnread()
  }, [loadNotifications, loadUnread])

  useEffect(() => {
    reload()
    const interval = setInterval(reload, 5000)
    return () => clearInterval(interval)
  }, [reload])

  const markRead = async (id) => {
    if (!id) return

    try {
      await request(`${API}/${id}/read`, { method: 'PATCH' })
      reload()
    } catch (error) {
      console.error('Không đánh dấu đã đọc được:', error)
    }
  }

  const markAllRead = async () => {
    try {
      await request(`${API}/read-all`, { method: 'PATCH' })
      reload()
    } catch (error) {
      console.error('Không đọc tất cả được:', error)
    }
  }

  return (
    <CDropdown variant="nav-item" alignment="end">
      <CDropdownToggle caret={false} className="notification-toggle">
        <span className="notification-icon">
          <CIcon icon={cilBell} size="lg" />

          {unread > 0 && (
            <span className="notification-badge">
              {unread > 99 ? '99+' : unread}
            </span>
          )}
        </span>
      </CDropdownToggle>

      <CDropdownMenu className="notification-menu">
        <CDropdownHeader className="notification-header">
          <span>Thông báo</span>

          {unread > 0 && (
            <button
              type="button"
              className="notification-read-all"
              onClick={markAllRead}
            >
              Đọc tất cả
            </button>
          )}
        </CDropdownHeader>

        {items.length === 0 ? (
          <CDropdownItem disabled className="notification-empty">
            Không có thông báo
          </CDropdownItem>
        ) : (
          items.map((n) => {
            const id = n.id || n.notificationId
            const isRead = n.read ?? n.isRead ?? false

            return (
              <CDropdownItem
                key={id}
                onClick={() => markRead(id)}
                className={`notification-item ${!isRead ? 'unread' : ''}`}
              >
                <div className="notification-title">
                  {n.title || 'Thông báo'}
                </div>
                <div className="notification-message">
                  {n.message || n.content || ''}
                </div>
              </CDropdownItem>
            )
          })
        )}
      </CDropdownMenu>
    </CDropdown>
  )
}