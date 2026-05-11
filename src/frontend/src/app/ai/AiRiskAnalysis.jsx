import { useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './AiRiskAnalysis.css'

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

  const loadRiskData = async () => {
    setLoading(true)
    setError('')

    try {
      const token = getToken()

      const res = await axios.get('/api/ai-risk/assets', {
        headers: {
          Authorization: token ? `Bearer ${token}` : undefined,
        },
      })

      setData(normalizeRiskData(res.data))
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

    return { total, high, medium, low }
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
        <p>
          Phân tích mức độ rủi ro thiết bị dựa trên Work Order, downtime.
        </p>
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

        <div className="risk-card low">
          <span>Rủi ro thấp</span>
          <strong>{summary.low}</strong>
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
                <th>Khuyến nghị AI</th>
              </tr>
            </thead>

            <tbody>
              {riskList.map((item) => (
                <tr key={item.assetId}>
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

                  <td className="recommendation">
                    {item.recommendation}
                  </td>
                </tr>
              ))}

              {!loading && riskList.length === 0 && (
                <tr>
                  <td colSpan={7} className="empty-row">
                    Chưa có dữ liệu phân tích rủi ro.
                  </td>
                </tr>
              )}

              {loading && (
                <tr>
                  <td colSpan={7} className="empty-row">
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