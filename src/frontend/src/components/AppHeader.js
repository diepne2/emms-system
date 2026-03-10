import React, { useEffect, useRef, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
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
  cilBell,
  cilEnvelopeOpen,
  cilMoon,
  cilSun,
  cilContrast,
} from '@coreui/icons'

import { Breadcrumb } from './header/index'
import { HeaderDropdown } from './header'

export default function AppHeader() {
  const headerRef = useRef()
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const sidebarShow = useSelector((state) => state.sidebarShow)
  const { colorMode, setColorMode } = useColorModes('coreui-free-react-admin-template-theme')

  const [notiCount] = useState(3)
  const [msgCount] = useState(2)

  useEffect(() => {
    const handleScroll = () => {
      headerRef.current &&
        headerRef.current.classList.toggle('shadow-sm', document.documentElement.scrollTop > 0)
    }
    document.addEventListener('scroll', handleScroll)
    return () => document.removeEventListener('scroll', handleScroll)
  }, [])

  return (
    <CHeader position="sticky" className="mb-4 p-0" ref={headerRef}>
      <CContainer fluid className="border-bottom px-4">
        {/* Sidebar toggle */}
        <CHeaderToggler
          onClick={() => dispatch({ type: 'set', sidebarShow: !sidebarShow })}
          style={{ marginInlineStart: '-14px' }}
        >
          <CIcon icon={cilMenu} size="lg" />
        </CHeaderToggler>

        {/* Left */}
        <CHeaderNav className="d-none d-md-flex">
          <CNavItem>
            <CNavLink to="/dashboard" as={NavLink}>
              Dashboard
            </CNavLink>
          </CNavItem>
        </CHeaderNav>

        {/* Right icons */}
        <CHeaderNav className="ms-auto align-items-center gap-3">
          {/* 🔔 Notifications */}
          <CDropdown variant="nav-item" placement="bottom-end">
            <CDropdownToggle caret={false} className="position-relative">
              <CIcon icon={cilBell} size="lg" />
              {notiCount > 0 && (
                <CBadge color="danger" shape="rounded-pill" className="position-absolute top-0 start-100 translate-middle">
                  {notiCount}
                </CBadge>
              )}
            </CDropdownToggle>

            <CDropdownMenu style={{ width: 360 }}>
              <div className="fw-bold text-center py-2 border-bottom">Thông báo</div>

              <CDropdownItem onClick={() => navigate('/notifications/1')}>
                <div className="fw-semibold">Vật tư sắp hết</div>
                <small className="text-muted">Kho A chỉ còn 5 sản phẩm</small>
                <div className="text-muted small">2 phút trước</div>
              </CDropdownItem>

              <CDropdownItem onClick={() => navigate('/notifications/2')}>
                <div className="fw-semibold">Hoàn thành sửa chữa</div>
                <small className="text-muted">Thiết bị TB-001</small>
                <div className="text-muted small">10 phút trước</div>
              </CDropdownItem>

              <CDropdownItem onClick={() => navigate('/notifications/3')}>
                <div className="fw-semibold">Yêu cầu mới</div>
                <small className="text-muted">Có yêu cầu bảo trì mới</small>
                <div className="text-muted small">1 giờ trước</div>
              </CDropdownItem>

              <div className="text-center border-top">
                <CDropdownItem onClick={() => navigate('/notifications')} className="text-primary fw-semibold">
                  Xem tất cả
                </CDropdownItem>
              </div>
            </CDropdownMenu>
          </CDropdown>

          {/* ✉️ Messages */}
          <CDropdown variant="nav-item" placement="bottom-end">
            <CDropdownToggle caret={false} className="position-relative">
              <CIcon icon={cilEnvelopeOpen} size="lg" />
              {msgCount > 0 && (
                <CBadge color="danger" shape="rounded-pill" className="position-absolute top-0 start-100 translate-middle">
                  {msgCount}
                </CBadge>
              )}
            </CDropdownToggle>

            <CDropdownMenu style={{ width: 360 }}>
              <div className="fw-bold text-center py-2 border-bottom">Tin nhắn</div>

              <CDropdownItem>
                <div className="fw-semibold">Trần Thị B</div>
                <small className="text-muted">Anh xem giúp em case này</small>
                <div className="text-muted small">Vừa xong</div>
              </CDropdownItem>

              <CDropdownItem>
                <div className="fw-semibold">Nguyễn Văn A</div>
                <small className="text-muted">Thiết bị đã xử lý xong</small>
                <div className="text-muted small">5 phút trước</div>
              </CDropdownItem>

              <div className="text-center border-top">
                <CDropdownItem onClick={() => navigate('/chat')} className="text-primary fw-semibold">
                  Mở chat
                </CDropdownItem>
              </div>
            </CDropdownMenu>
          </CDropdown>

          {/* Theme */}
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

          <HeaderDropdown />
        </CHeaderNav>
      </CContainer>

      <CContainer fluid className="px-4">
        <Breadcrumb />
      </CContainer>
    </CHeader>
  )
}
