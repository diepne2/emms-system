import { useEffect, useMemo, useState } from 'react'
import { MapContainer, Marker, Popup, TileLayer } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './location.css'

const API_BASE =
  import.meta.env.VITE_API_BASE_URL ||
  'https://emms-system-production-4239.up.railway.app'

const LOCATIONS_API = `${API_BASE}/api/locations`

delete L.Icon.Default.prototype._getIconUrl

L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl:
    'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl:
    'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

const getToken = () =>
  localStorage.getItem('token') ||
  localStorage.getItem('accessToken') ||
  localStorage.getItem('access_token') ||
  localStorage.getItem('jwt') ||
  ''

const getAuthHeaders = () => {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const getLat = (item) =>
  Number(item?.latitude || item?.lat || item?.mapLat || 21.0285)

const getLng = (item) =>
  Number(item?.longitude || item?.lng || item?.mapLng || 105.8542)

export default function LocationMap() {
  const [locations, setLocations] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [keyword, setKeyword] = useState('')

  const loadLocations = async () => {
    try {
      setLoading(true)
      setError('')

      const res = await fetch(LOCATIONS_API, {
        headers: getAuthHeaders(),
      })

      const data = await res.json()

      if (!res.ok) {
        throw new Error(data?.message || 'Không tải được danh sách vị trí.')
      }

      setLocations(Array.isArray(data) ? data : data?.content || data?.data || [])
    } catch (err) {
      setError(err.message || 'Không tải được danh sách vị trí.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadLocations()
  }, [])

  const filteredLocations = useMemo(() => {
    const q = keyword.trim().toLowerCase()
    if (!q) return locations

    return locations.filter((item) =>
      [
        item.name,
        item.address,
        item.description,
        item.parentLocation,
        item.vendors,
        item.contractors,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(q),
    )
  }, [locations, keyword])

  const handleSearch = () => {
    setKeyword(searchInput)
  }

  const handleReset = () => {
    setSearchInput('')
    setKeyword('')
  }

  return (
    <div className="location-map-page">
      <div className="location-map-card">
        <h2>Bản đồ vị trí</h2>

        <div className="location-map-filter">
          <div className="location-map-filter-header">
            <div className="location-map-icon">📍</div>
            <div>
              <h3>OpenStreetMap</h3>
            </div>
          </div>

          <label>Tìm kiếm</label>
          <div className="location-map-search-row">
            <input
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="Tìm kiếm"
            />

            <button
              className="location-map-search-btn"
              type="button"
              onClick={handleSearch}
            >
              Tìm kiếm
            </button>

            <button
              className="location-map-reset-btn"
              type="button"
              onClick={handleReset}
            >
              Làm mới
            </button>
          </div>
        </div>

        {loading && <div className="location-map-message">Đang tải dữ liệu...</div>}

        {error && <div className="location-map-error">{error}</div>}

        {!loading && !error && (
          <>
            <div className="location-map-summary">
              Đang hiển thị <strong>{filteredLocations.length}</strong> vị trí
            </div>

            <div className="location-map-box">
              <MapContainer
                center={[21.0285, 105.8542]}
                zoom={12}
                scrollWheelZoom
                style={{ height: '520px', width: '100%' }}
              >
                <TileLayer
                  attribution="&copy; OpenStreetMap contributors"
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                {filteredLocations.map((item) => {
                  const id = item.id || item.locationId || item.name
                  const lat = getLat(item)
                  const lng = getLng(item)

                  return (
                    <Marker key={id} position={[lat, lng]}>
                      <Popup>
                        <strong>{item.name || 'Vị trí'}</strong>
                        <br />
                        {item.address || 'Chưa có địa chỉ'}
                        {item.description && (
                          <>
                            <br />
                            {item.description}
                          </>
                        )}
                      </Popup>
                    </Marker>
                  )
                })}
              </MapContainer>
            </div>
          </>
        )}
      </div>
    </div>
  )
}