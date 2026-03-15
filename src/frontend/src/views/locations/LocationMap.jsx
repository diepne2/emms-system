import React from "react"
import { GoogleMap, LoadScript, Marker } from "@react-google-maps/api"

const styles = {

  container: {
    padding: "20px"
  },

  title: {
    fontSize: "22px",
    fontWeight: "600",
    marginBottom: "16px"
  },

  mapBox: {
    width: "100%",
    height: "600px",
    borderRadius: "12px",
    overflow: "hidden",
    boxShadow: "0 4px 12px rgba(0,0,0,0.1)"
  }

}

const center = {
  lat: 21.0285,
  lng: 105.8542
}

const locations = [
  {
    id: 1,
    name: "Kho A",
    position: { lat: 21.0285, lng: 105.8542 }
  },
  {
    id: 2,
    name: "Kho B",
    position: { lat: 21.0300, lng: 105.8500 }
  }
]

const LocationMap = () => {

  return (

    <div style={styles.container}>

      <h4 style={styles.title}>
        Bản đồ 
      </h4>

      <div style={styles.mapBox}>

        <LoadScript googleMapsApiKey="YOUR_GOOGLE_MAP_API_KEY">

          <GoogleMap
            mapContainerStyle={{
              width: "100%",
              height: "100%"
            }}
            center={center}
            zoom={14}
          >

            {locations.map((loc) => (

              <Marker
                key={loc.id}
                position={loc.position}
                title={loc.name}
              />

            ))}

          </GoogleMap>

        </LoadScript>

      </div>

    </div>

  )

}

export default LocationMap