import React from 'react'
import ReactDOM from 'react-dom/client'
import { GoogleOAuthProvider } from '@react-oauth/google'
import App from './App'
import './styles/index.css'

const rawGoogleClientId = (import.meta.env.VITE_GOOGLE_CLIENT_ID || '').trim()
const isPlaceholderId = rawGoogleClientId === 'your_google_oauth_web_client_id'
const googleClientId = isPlaceholderId ? '' : rawGoogleClientId

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    {googleClientId ? (
      <GoogleOAuthProvider clientId={googleClientId}>
        <App />
      </GoogleOAuthProvider>
    ) : (
      <App />
    )}
  </React.StrictMode>,
)
