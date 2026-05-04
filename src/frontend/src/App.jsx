import React, { Suspense, useEffect } from 'react'
import { HashRouter, Route, Routes, Navigate } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { CSpinner, useColorModes } from '@coreui/react'

import './scss/style.scss'
import './scss/examples.scss'

const DefaultLayout = React.lazy(() => import('./layout/DefaultLayout'))


const Login = React.lazy(() => import('./app/pages/login/Login'))
const ForgotPassword = React.lazy(() => import('./app/pages/forgot-password/ForgotPassword'))
const ResetPassword = React.lazy(() => import('./app/pages/reset-password/ResetPassword'))
const Page404 = React.lazy(() => import('./app/pages/page404/Page404'))
const Logout = React.lazy(() => import('./app/pages/logout/Logout'))

function PrivateRoute({ children }) {
  const token = getAccessToken()

  if (!token || isTokenExpired(token)) {
    clearAuth()
    return <Navigate to="/login" replace />
  }

  return children
}

function PublicRoute({ children }) {
  const token = getAccessToken()

  if (token && !isTokenExpired(token)) {
    return <Navigate to="/dashboard1" replace />
  }

  return children
}

const App = () => {
  const { isColorModeSet, setColorMode } =
    useColorModes('coreui-free-react-admin-template-theme')

  const storedTheme = useSelector((state) => state.theme)

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search)

    const theme =
      urlParams.get('theme') &&
      urlParams.get('theme').match(/^[A-Za-z0-9\s]+/)[0]

    if (theme) {
      setColorMode(theme)
    }

    if (!isColorModeSet()) {
      setColorMode(storedTheme)
    }
  }, [storedTheme, isColorModeSet, setColorMode])

  return (
    <HashRouter>
      <Suspense
        fallback={
          <div className="pt-3 text-center">
            <CSpinner color="primary" />
          </div>
        }
      >
        <Routes>
          {/* Root redirect */}
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* PUBLIC ROUTES */}
          <Route path="/login" element={<Login />} />
          <Route path="/logout" element={<Logout />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
  

          {/* ERROR ROUTES */}
          <Route path="/404" element={<Page404 />} />


          {/* MAIN APP */}
          <Route path="/*" element={<DefaultLayout />} />

          {/* UNKNOWN ROUTE */}
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Routes>
      </Suspense>
    </HashRouter>
  )
}

export default App