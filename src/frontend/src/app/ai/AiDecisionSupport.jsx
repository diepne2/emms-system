import { useState } from 'react'
import axios from 'axios'
import './AiDecisionSupport.css'

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  "https://emms-system-production-4239.up.railway.app";

const suggestions = [
  'Thiết bị nào cần ưu tiên bảo trì?',
  'Thiết bị nào có rủi ro cao nhất?',
  'Tóm tắt tình hình bảo trì hiện tại',
  'Đề xuất hướng bảo trì cho các thiết bị rủi ro cao',
  'Có thiết bị nào cần kiểm tra ngay không?',
]

export default function AiDecisionSupport() {
  const [question, setQuestion] = useState('')
  const [answer, setAnswer] = useState('')
  const [loading, setLoading] = useState(false)

  const getToken = () => {
    return (
      localStorage.getItem('token') ||
      localStorage.getItem('accessToken') ||
      localStorage.getItem('access_token') ||
      localStorage.getItem('jwt')
    )
  }

  const askAi = async (customQuestion) => {
    const q = customQuestion || question

    if (!q.trim()) {
      alert('Vui lòng nhập câu hỏi')
      return
    }

    setLoading(true)
    setAnswer('')
    setQuestion(q)

    try {
      const token = getToken()

      const res = await axios.post(
        `${API_BASE}/api/ai-decision/ask`,
        {
          question: q,
        },
        {
          headers: {
            Authorization: token ? `Bearer ${token}` : undefined,
          },
        },
      )

      setAnswer(res.data.answer)
    } catch (error) {
      setAnswer('Không thể gọi Gemini AI. Vui lòng kiểm tra API key, backend hoặc quyền truy cập.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="ai-decision-page">
      <div className="ai-decision-hero">
        <span className="ai-badge">EMMS AI</span>

        <h2>AI hỗ trợ ra quyết định bảo trì</h2>

        <p>
          Phân tích dữ liệu vận hành thực tế, đánh giá rủi ro thiết bị.
        </p>
      </div>

      <div className="ai-suggestion-grid">
        {suggestions.map((item) => (
          <button
            key={item}
            type="button"
            onClick={() => askAi(item)}
            disabled={loading}
          >
            {item}
          </button>
        ))}
      </div>

      <div className="ai-panel">
        <label>Câu hỏi cho AI</label>

        <textarea
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="Nhập câu hỏi."
          rows={5}
        />

        <button
          type="button"
          className="ai-primary-btn"
          onClick={() => askAi()}
          disabled={loading}
        >
          {loading ? 'AI đang phân tích...' : 'Phân tích'}
        </button>
      </div>

      {answer && (
        <div className="ai-result">
          <h4>Kết quả phân tích</h4>
          <div>{answer}</div>
        </div>
      )}
    </div>
  )
}