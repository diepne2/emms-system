import React from 'react'
import { createRoot } from 'react-dom/client'
import { Provider } from 'react-redux'
import './assets/styles/variables.css'
import 'core-js'
import '@coreui/coreui/dist/css/coreui.min.css'
import '@coreui/icons/css/all.min.css'

import App from './App'
import store from './store'

createRoot(document.getElementById('root')).render(
  <Provider store={store}>
    <App />
  </Provider>
)