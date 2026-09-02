import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './styles.css';
import { TenantProvider } from './context/TenantContext';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <TenantProvider>
      <App />
    </TenantProvider>
  </React.StrictMode>
);
