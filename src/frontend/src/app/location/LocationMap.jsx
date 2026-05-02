import React, { useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import { GoogleMap, LoadScript, Marker, InfoWindow } from '@react-google-maps/api'
import './location.css'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

const getToken = () =>
  localStorage.getItem('accessToken') ||
  localStorage.getItem('token') ||
  localStorage.getItem('access_token') ||
  sessionStorage.getItem('accessToken') ||
  sessionStorage.getItem('token') ||
  sessionStorage.getItem('access_token') ||
  ''

const getAuthConfig = () => {
  const token = getToken()
  return {
    headers: token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : {},
  }
}

const GOOGLE_MAPS_API_KEY = 'YOUR_GOOGLE_MAPS_API_KEY'

const defaultCenter = {
  lat: 10.9804,
  lng: 106.6519,
}

const fakeGeocode = (address = '', index = 0) => {
  const text = String(address).toLowerCase()

  if (text.includes('bình dương') || text.includes('thuận an')) {
    return { lat: 10.8797 + index * 0.002, lng: 106.7523 + index * 0.002 }
  }

  if (text.includes('nghệ an')) {
    return { lat: 18.6796 + index * 0.002, lng: 105.6813 + index * 0.002 }
  }

  if (text.includes('hà nội')) {
    return { lat: 21.0285 + index * 0.002, lng: 105.8542 + index * 0.002 }
  }

  if (text.includes('hồ chí minh') || text.includes('tp hcm')) {
    return { lat: 10.7769 + index * 0.002, lng: 106.7009 + index * 0.002 }
  }

  return { lat: defaultCenter.lat + index * 0.002, lng: defaultCenter.lng + index * 0.002 }
}

export default function LocationMap() {
  const [locations, setLocations] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState(null)

  useEffect(() => {
    const fetchLocations = async () => {
      try {
        setLoading(true)
        setError('')
        const res = await api.get('/locations', getAuthConfig())
        setLocations(Array.isArray(res.data) ? res.data : [])
      } catch (err) {
        setError(
          err?.response?.data?.message ||
            err?.response?.data?.error ||
            'Không tải được danh sách vị trí.',
        )
      } finally {
        setLoading(false)
      }
    }

    fetchLocations()
  }, [])

  const filteredLocations = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    if (!keyword) return locations

    return locations.filter((item) =>
      [item?.name, item?.address, item?.parentLocation, item?.vendors, item?.contractors].some(
        (field) => String(field || '').toLowerCase().includes(keyword),
      ),
    )
  }, [locations, search])

  const mapLocations = useMemo(() => {
    return filteredLocations.map((item, index) => {
      const position =
        item.latitude && item.longitude
          ? { lat: Number(item.latitude), lng: Number(item.longitude) }
          : fakeGeocode(item.address, index)

      return {
        ...item,
        position,
      }
    })
  }, [filteredLocations])

  const center = mapLocations.length > 0 ? mapLocations[0].position : defaultCenter

  return (
    <div className="location-page">
      <div className="card">
        <div className="assets-header">
          <div className="assets-header__top">
            <div className="assets-header__intro">
              <div className="assets-header__mini-title">Bản đồ vị trí</div>
            </div>
          </div>

          <div className="filters-panel">
            <div className="filters-panel__header">
              <div className="filters-panel__title-wrap">
                <div className="filters-panel__icon">📍</div>
                <div>
                  <div className="filters-panel__title">Google Map</div>
                </div>
              </div>
            </div>

            <div className="filters-grid filters-grid--1">
              <div className="filter-field">
                <label className="filter-label">Tìm kiếm</label>
                <div className="search-box">
                  <input
                    type="text"
                    placeholder="Tìm kiếm"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        {loading ? (
          <div className="assets-message">Đang tải bản đồ...</div>
        ) : error ? (
          <div className="assets-message assets-message--error">{error}</div>
        ) : (
          <div className="map-card">
            <div className="map-card__sidebar">
              <div className="list-section__title">
                Danh sách vị trí
                <span className="list-badge">{mapLocations.length}</span>
              </div>

              <div className="map-location-list">
                {mapLocations.length > 0 ? (
                  mapLocations.map((item) => (
                    <div
                      className="map-location-item"
                      key={item.id}
                      onClick={() => setSelected(item)}
                    >
                      <div className="map-location-item__name">{item.name || '-'}</div>
                      <div className="map-location-item__meta">{item.address || '-'}</div>
                      <div className="map-location-item__meta">
                        Parent: {item.parentLocation || '-'}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="assets-message">Không có dữ liệu vị trí.</div>
                )}
              </div>
            </div>

            <div className="map-card__content">
              <LoadScript googleMapsApiKey={GOOGLE_MAPS_API_KEY}>
                <GoogleMap
                  mapContainerClassName="location-map"
                  center={selected?.position || center}
                  zoom={11}
                  options={{
                    streetViewControl: false,
                    mapTypeControl: true,
                    fullscreenControl: true,
                  }}
                >
                  {mapLocations.map((item) => (
                    <Marker
                      key={item.id}
                      position={item.position}
                      onClick={() => setSelected(item)}
                    />
                  ))}

                  {selected && (
                    <InfoWindow
                      position={selected.position}
                      onCloseClick={() => setSelected(null)}
                    >
                      <div className="map-popup">
                        <strong>{selected.name || '-'}</strong>
                        <div>{selected.address || '-'}</div>
                        <div>Parent: {selected.parentLocation || '-'}</div>
                        <div>Vendors: {selected.vendors || '-'}</div>
                        <div>Contractors: {selected.contractors || '-'}</div>
                      </div>
                    </InfoWindow>
                  )}
                </GoogleMap>
              </LoadScript>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}