import { useEffect, useMemo, useState } from 'react'
import { notification } from 'antd'
import axios from 'axios'
import './AiRiskAnalysis.css'

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  'https://emms-system-production-4239.up.railway.app'

export default function AiRiskAnalysis() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const getToken = () => {
    return (
      localStorage.getItem('token') ||
      localStorage.getItem('accessToken') ||
      localStorage.getItem('access_token') ||
      localStorage.getItem('jwt')
    )
  }

  const normalizeRiskData = (raw) => {
    if (Array.isArray(raw)) return raw
    if (Array.isArray(raw?.data)) return raw.data
    if (Array.isArray(raw?.content)) return raw.content
    if (Array.isArray(raw?.items)) return raw.items
    return []
  }

  const showAiWarningNotification = (list) => {
    const warningAssets = list.filter((item) => item.earlyWarning)

    if (warningAssets.length === 0) return

    const highRiskAssets = warningAssets.filter((item) => item.riskLevel === 'HIGH')
    const topAssets = warningAssets
      .slice(0, 3)
      .map((item) => item.assetName)
      .join(', ')

    notification.warning({
      message: 'AI cảnh báo thiết bị',
      description: `Có ${warningAssets.length} thiết bị có nguy cơ hỏng hóc. ${
        highRiskAssets.length > 0 ? `${highRiskAssets.length} thiết bị rủi ro cao. ` : ''
      }Cần ưu tiên kiểm tra: ${topAssets}.`,
      placement: 'topRight',
      duration: 8,
    })
  }

  const loadRiskData = async () => {
    setLoading(true)
    setError('')

    try {
      const token = getToken()
      const headers = token ? { Authorization: `Bearer ${token}` } : {}

      const res = await axios.get(`${API_BASE}/api/ai-risk/assets`, { headers })

      const normalized = normalizeRiskData(res.data)
      setData(normalized)
      showAiWarningNotification(normalized)
    } catch (err) {
      console.error('AI Risk Analysis error:', err)
      setData([])
      setError('Không thể tải dữ liệu AI Risk Analysis. Vui lòng kiểm tra backend, token hoặc CORS.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRiskData()
  }, [])

  const riskList = Array.isArray(data) ? data : []

  const summary = useMemo(() => {
    const total = riskList.length
    const high = riskList.filter((item) => item.riskLevel === 'HIGH').length
    const medium = riskList.filter((item) => item.riskLevel === 'MEDIUM').length
    const low = riskList.filter((item) => item.riskLevel === 'LOW').length
    const warning = riskList.filter((item) => item.earlyWarning).length

    return { total, high, medium, low, warning }
  }, [riskList])

  const getRiskClass = (level) => {
    if (level === 'HIGH') return 'risk-high'
    if (level === 'MEDIUM') return 'risk-medium'
    return 'risk-low'
  }

  return (
    <div className="ai-risk-page">
      <div className="ai-risk-hero">
        <span className="ai-risk-badge">EMMS AI</span>
        <h2>AI Risk Analysis</h2>
        <p>Phân tích mức độ rủi ro thiết bị dựa trên Work Order, downtime và cảnh báo sớm nguy cơ hỏng hóc.</p>
      </div>

      <div className="ai-risk-summary">
        <div className="risk-card">
          <span>Tổng thiết bị</span>
          <strong>{summary.total}</strong>
        </div>

        <div className="risk-card high">
          <span>Rủi ro cao</span>
          <strong>{summary.high}</strong>
        </div>

        <div className="risk-card medium">
          <span>Rủi ro trung bình</span>
          <strong>{summary.medium}</strong>
        </div>

        <div className="risk-card warning">
          <span>Cảnh báo AI</span>
          <strong>{summary.warning}</strong>
        </div>
      </div>

      <div className="ai-risk-actions">
        <button type="button" onClick={loadRiskData} disabled={loading}>
          {loading ? 'Đang phân tích...' : 'Tải lại phân tích'}
        </button>
      </div>

      {error && <div className="ai-risk-error">{error}</div>}

      <div className="ai-risk-table-card">
        <h4>Danh sách rủi ro thiết bị</h4>

        <div className="ai-risk-table-wrap">
          <table className="ai-risk-table">
            <thead>
              <tr>
                <th>Thiết bị</th>
                <th>Trạng thái</th>
                <th>Work Orders</th>
                <th>Downtime</th>
                <th>Risk Score</th>
                <th>Risk Level</th>
                <th>Cảnh báo AI</th>
                <th>Khuyến nghị AI</th>
              </tr>
            </thead>

            <tbody>
              {riskList.map((item) => (
                <tr key={item.assetId} className={item.earlyWarning ? 'warning-row' : ''}>
                  <td>
                    <strong>{item.assetName}</strong>
                    <div className="asset-id">ID: {item.assetId}</div>
                  </td>

                  <td>{item.status}</td>
                  <td>{item.totalWorkOrders}</td>
                  <td>{item.totalDowntimes}</td>

                  <td>
                    <div className="risk-score">
                      <span>{item.riskScore}/100</span>
                      <div className="risk-bar">
                        <div
                          className={`risk-bar-fill ${getRiskClass(item.riskLevel)}`}
                          style={{ width: `${item.riskScore || 0}%` }}
                        />
                      </div>
                    </div>
                  </td>

                  <td>
                    <span className={`risk-badge ${getRiskClass(item.riskLevel)}`}>
                      {item.riskLevel}
                    </span>
                  </td>

                  <td className="ai-warning-cell">
                    {item.earlyWarning ? (
                      <div className="ai-warning-box">
                        <strong>Cảnh báo</strong>
                        <span>{item.warningMessage}</span>
                      </div>
                    ) : (
                      <span className="ai-safe-text">Ổn định</span>
                    )}
                  </td>

                  <td className="recommendation">{item.recommendation}</td>
                </tr>
              ))}

              {!loading && riskList.length === 0 && (
                <tr>
                  <td colSpan={8} className="empty-row">
                    Chưa có dữ liệu phân tích rủi ro.
                  </td>
                </tr>
              )}

              {loading && (
                <tr>
                  <td colSpan={8} className="empty-row">
                    AI đang phân tích dữ liệu...
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}