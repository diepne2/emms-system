import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'

export default function Logout() {
  const navigate = useNavigate()

  useEffect(() => {
    const doLogout = async () => {
      const accessToken = localStorage.getItem('accessToken')
      const refreshToken = localStorage.getItem('refreshToken')

      try {
        if (refreshToken) {
          await axios.post(
            'https://emms-system-production-4239.up.railway.app/api/v1/auth/logout',
            { refreshToken },
            {
              headers: {
                'Content-Type': 'application/json',
                ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
              },
            },
          )
        }
      } catch (error) {
        console.error('Logout API error:', error)
      } finally {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        sessionStorage.clear()
        navigate('/login', { replace: true })
      }
    }

    doLogout()
  }, [navigate])

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        fontSize: '20px',
        fontWeight: '600',
      }}
    >
      Đang đăng xuất...
    </div>
  )
}