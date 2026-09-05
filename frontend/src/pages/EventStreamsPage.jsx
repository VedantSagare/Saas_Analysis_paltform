import { ChevronDown, CirclePause, CirclePlay, Moon, Sun, Wifi } from 'lucide-react';
import { useState } from 'react';
import Sidebar from '../components/Sidebar';
import { useTenant } from '../context/TenantContext';

const events = [
  { type: 'page_view', source: 'web', user: 'usr_9m42', time: 'Just now' },
  { type: 'checkout_started', source: 'web', user: 'usr_7k18', time: '18 sec ago' },
  { type: 'purchase_completed', source: 'api', user: 'usr_2d55', time: '46 sec ago' },
  { type: 'signup', source: 'web', user: 'usr_4p03', time: '1 min ago' },
];

export default function EventStreamsPage({ dark, setDark, onNavigate }) {
  const { tenant } = useTenant();
  const [paused, setPaused] = useState(false);
  const [query, setQuery] = useState('');
  const visibleEvents = events.filter((event) => event.type.includes(query.toLowerCase()) || event.user.includes(query.toLowerCase()));

  return <div className={dark ? 'app-shell dark' : 'app-shell'}>
    <Sidebar activePage="events" onNavigate={onNavigate} />
    <main className="main-content">
      <header className="topbar"><div className="breadcrumb">Workspace <ChevronDown size={14} /> <strong>Event streams</strong></div><button type="button" className="icon-button" aria-label="Toggle dark mode" onClick={() => setDark((value) => !value)}>{dark ? <Sun size={18} /> : <Moon size={18} />}</button></header>
      <section className="page-heading"><div><div className="eyebrow">Live ingestion</div><h1>Event streams</h1><p>Inspect incoming product events for {tenant.name}.</p></div><button type="button" className="stream-toggle" onClick={() => setPaused((value) => !value)}>{paused ? <CirclePlay size={16} /> : <CirclePause size={16} />}{paused ? 'Resume stream' : 'Pause stream'}</button></section>
      <section className="stream-summary"><div><Wifi size={19} /><span><strong>{paused ? 'Stream paused' : 'Stream connected'}</strong><small>{paused ? 'New events are temporarily hidden.' : 'Listening to the events topic.'}</small></span></div><b>{paused ? '—' : '24 events/min'}</b></section>
      <section className="panel stream-panel"><div className="table-tools"><div><h2>Recent events</h2><p>Newest events appear first.</p></div><div className="search-box"><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search event or user" /></div></div>{paused ? <div className="empty-state">The stream is paused. Resume it to inspect incoming events.</div> : <div className="table-scroll"><table><thead><tr><th>Event</th><th>Source</th><th>User</th><th>Received</th></tr></thead><tbody>{visibleEvents.map((event) => <tr key={`${event.type}-${event.user}`}><td><span className="event-dot" />{event.type}</td><td>{event.source}</td><td>{event.user}</td><td>{event.time}</td></tr>)}</tbody></table></div>}</section>
    </main>
  </div>;
}
