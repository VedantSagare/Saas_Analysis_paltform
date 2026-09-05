import { useState } from 'react';
import DashboardPage from './pages/DashboardPage';
import EventStreamsPage from './pages/EventStreamsPage';
import ReportsPage from './pages/ReportsPage';

export default function App() {
  const [page, setPage] = useState('overview');
  const [dark, setDark] = useState(true);
  const pageProps = { dark, setDark, onNavigate: setPage };

  if (page === 'reports') return <ReportsPage {...pageProps} />;
  if (page === 'events') return <EventStreamsPage {...pageProps} />;
  return <DashboardPage {...pageProps} />;
}
