import React, { useEffect, useState } from 'react'
import axios from 'axios'
import './changepassword.css'

const API_BASE = 'http://localhost:8080'

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

const EMPTY_FORM = {
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
}

const ChangePassword = ({ open, onClose, onSuccess }) => {
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [showCurrentPassword, setShowCurrentPassword] = useState(false)
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  useEffect(() => {
    if (!open) {
      setForm(EMPTY_FORM)
      setError('')
      setSuccess('')
      setSubmitting(false)
      setShowCurrentPassword(false)
      setShowNewPassword(false)
      setShowConfirmPassword(false)
    }
  }, [open])

  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && open && !submitting) {
        onClose?.()
      }
    }

    document.addEventListener('keydown', handleEsc)
    return () => document.removeEventListener('keydown', handleEsc)
  }, [open, submitting, onClose])

  useEffect(() => {
    if (open) {
      document.body.classList.add('cp-modal-open')
    } else {
      document.body.classList.remove('cp-modal-open')
    }

    return () => document.body.classList.remove('cp-modal-open')
  }, [open])

  if (!open) return null

  const handleChange = (field) => (e) => {
    setForm((prev) => ({
      ...prev,
      [field]: e.target.value,
    }))
  }

  const validateForm = () => {
    if (!form.currentPassword.trim()) {
      return 'Vui lòng nhập mật khẩu hiện tại.'
    }

    if (!form.newPassword.trim()) {
      return 'Vui lòng nhập mật khẩu mới.'
    }

    if (form.newPassword.length < 6) {
      return 'Mật khẩu mới phải có ít nhất 6 ký tự.'
    }

    if (!form.confirmPassword.trim()) {
      return 'Vui lòng nhập xác nhận mật khẩu.'
    }

    if (form.newPassword !== form.confirmPassword) {
      return 'Xác nhận mật khẩu không khớp.'
    }

    if (form.currentPassword === form.newPassword) {
      return 'Mật khẩu mới phải khác mật khẩu hiện tại.'
    }

    return ''
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    const validationError = validateForm()
    if (validationError) {
      setError(validationError)
      return
    }

    try {
      setSubmitting(true)

      await api.put('/api/users/me/change-password', {
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword,
      })

      setSuccess('Đổi mật khẩu thành công.')

      if (typeof onSuccess === 'function') {
        onSuccess()
      }

      setTimeout(() => {
        onClose?.()
      }, 900)
    } catch (err) {
      const message =
        err?.response?.data?.message ||
        (typeof err?.response?.data === 'string' ? err.response.data : '') ||
        'Đổi mật khẩu thất bại.'

      setError(message)
    } finally {
      setSubmitting(false)
    }
  }

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget && !submitting) {
      onClose?.()
    }
  }

  return (
    <div className="cp-modal-overlay" onClick={handleOverlayClick}>
      <div className="cp-modal">
        <div className="cp-modal-header">
          <div>
            <h3>Đổi mật khẩu</h3>
            <p>Cập nhật mật khẩu để tăng bảo mật cho tài khoản của bạn.</p>
          </div>

          <button
            type="button"
            className="cp-close-btn"
            onClick={onClose}
            disabled={submitting}
            aria-label="Đóng"
          >
            ×
          </button>
        </div>

        {error && <div className="cp-alert cp-alert-error">{error}</div>}
        {success && <div className="cp-alert cp-alert-success">{success}</div>}

        <form onSubmit={handleSubmit} className="cp-form">
          <div className="cp-form-group">
            <label htmlFor="currentPassword">Mật khẩu hiện tại</label>
            <div className="cp-password-wrap">
              <input
                id="currentPassword"
                type={showCurrentPassword ? 'text' : 'password'}
                value={form.currentPassword}
                onChange={handleChange('currentPassword')}
                placeholder="Nhập mật khẩu hiện tại"
                autoComplete="current-password"
                disabled={submitting}
              />
              <button
                type="button"
                className="cp-toggle-btn"
                onClick={() => setShowCurrentPassword((prev) => !prev)}
                disabled={submitting}
              >
                {showCurrentPassword ? 'Ẩn' : 'Hiện'}
              </button>
            </div>
          </div>

          <div className="cp-form-group">
            <label htmlFor="newPassword">Mật khẩu mới</label>
            <div className="cp-password-wrap">
              <input
                id="newPassword"
                type={showNewPassword ? 'text' : 'password'}
                value={form.newPassword}
                onChange={handleChange('newPassword')}
                placeholder="Nhập mật khẩu mới"
                autoComplete="new-password"
                disabled={submitting}
              />
              <button
                type="button"
                className="cp-toggle-btn"
                onClick={() => setShowNewPassword((prev) => !prev)}
                disabled={submitting}
              >
                {showNewPassword ? 'Ẩn' : 'Hiện'}
              </button>
            </div>
          </div>

          <div className="cp-form-group">
            <label htmlFor="confirmPassword">Xác nhận mật khẩu mới</label>
            <div className="cp-password-wrap">
              <input
                id="confirmPassword"
                type={showConfirmPassword ? 'text' : 'password'}
                value={form.confirmPassword}
                onChange={handleChange('confirmPassword')}
                placeholder="Nhập lại mật khẩu mới"
                autoComplete="new-password"
                disabled={submitting}
              />
              <button
                type="button"
                className="cp-toggle-btn"
                onClick={() => setShowConfirmPassword((prev) => !prev)}
                disabled={submitting}
              >
                {showConfirmPassword ? 'Ẩn' : 'Hiện'}
              </button>
            </div>
          </div>

          <div className="cp-password-hint">
            Mật khẩu mới nên có ít nhất 6 ký tự và khác mật khẩu hiện tại.
          </div>

          <div className="cp-modal-footer">
            <button
              type="button"
              className="cp-btn cp-btn-light"
              onClick={onClose}
              disabled={submitting}
            >
              Hủy
            </button>

            <button
              type="submit"
              className="cp-btn cp-btn-primary"
              disabled={submitting}
            >
              {submitting ? 'Đang cập nhật...' : 'Lưu mật khẩu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ChangePassword