import { useEffect, useMemo, useRef, useState } from 'react';
import { Activity, ArrowUpRight, Bell, ChevronDown, ChevronLeft, ChevronRight, Download, FileText, Moon, Search, Sun, Users, Zap } from 'lucide-react';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getAnalyticsActivity, getAnalyticsSummary } from '../services/api';
import { useTenant } from '../context/TenantContext';
import Sidebar from '../components/Sidebar';

const fallbackData = {
  dailyActiveUsers: 2847,
  eventCounts: { page_view: 18240, checkout_started: 6420, purchase_completed: 3891, search: 2190, signup: 1480 },
  topEvents: { page_view: 18240, checkout_started: 6420, purchase_completed: 3891, search: 2190, signup: 1480 },
  funnels: [{ stage: 1, eventType: 'Landing page', count: 10000 }, { stage: 2, eventType: 'Product viewed', count: 7420 }, { stage: 3, eventType: 'Checkout started', count: 4210 }, { stage: 4, eventType: 'Purchase completed', count: 2730 }],
  retention: [{ day: 1, users: 2847 }, { day: 7, users: 1810 }, { day: 14, users: 1240 }, { day: 30, users: 834 }],
  latency: { p95Ms: 182, p99Ms: 426 },
};
const fallbackActivity = [{ day: 'Aug 27', users: 1810, events: 4980 }, { day: 'Aug 28', users: 2140, events: 6210 }, { day: 'Aug 29', users: 1980, events: 5720 }, { day: 'Aug 30', users: 2470, events: 7440 }, { day: 'Aug 31', users: 2310, events: 6810 }, { day: 'Sep 01', users: 2690, events: 8420 }, { day: 'Sep 02', users: 2847, events: 9230 }];
const eventLabels = { page_view: 'Page view', checkout_started: 'Checkout started', purchase_completed: 'Purchase completed', search: 'Search', signup: 'Sign up' };
const number = (value) => new Intl.NumberFormat('en-US').format(value || 0);
const csvValue = (value) => `"${String(value).replaceAll('"', '""')}"`;

function downloadFile(content, name) {
  const link = document.createElement('a');
  link.href = URL.createObjectURL(new Blob([content], { type: 'text/csv' }));
  link.download = name;
  link.click();
  URL.revokeObjectURL(link.href);
}

function MetricCard({ label, value, change, icon: Icon, accent }) {
  return <article className="metric-card"><div className={`metric-icon ${accent}`}><Icon size={19} /></div><div className="metric-label">{label}</div><div className="metric-value">{value}</div><div className="metric-change"><ArrowUpRight size={14} /> {change} <span>vs last period</span></div></article>;
}

export default function DashboardPage({ dark, setDark, onNavigate }) {
  const { tenant } = useTenant();
  const [data, setData] = useState(fallbackData);
  const [activity, setActivity] = useState(fallbackActivity);
  const [activityLoaded, setActivityLoaded] = useState(false);
  const [range, setRange] = useState('Last 7 days');
  const [chartMetric, setChartMetric] = useState('both');
  const [page, setPage] = useState(1);
  const [query, setQuery] = useState('');
  const [eventType, setEventType] = useState('all');
  const [loading, setLoading] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [retentionDetailOpen, setRetentionDetailOpen] = useState(false);
  const [funnelDetailOpen, setFunnelDetailOpen] = useState(false);
  const searchInput = useRef(null);

  useEffect(() => {
    const to = new Date();
    const from = new Date(to);
    from.setDate(to.getDate() - (range === 'Last 30 days' ? 30 : range === 'Last 24 hours' ? 1 : 7));
    let current = true;
    setLoading(true);
    Promise.all([getAnalyticsSummary(tenant.id, from, to), getAnalyticsActivity(tenant.id, from, to)])
      .then(([summary, points]) => { if (current) { setData(summary); setActivity(points); setActivityLoaded(true); } })
      .catch(() => { if (current) { setData(fallbackData); setActivity(fallbackActivity); setActivityLoaded(false); } })
      .finally(() => current && setLoading(false));
    return () => { current = false; };
  }, [tenant.id, range]);

  const allEvents = useMemo(() => Object.entries(data.eventCounts || {}), [data]);
  const eventRows = useMemo(() => allEvents.filter(([key]) => (eventLabels[key] || key).toLowerCase().includes(query.toLowerCase()) && (eventType === 'all' || key === eventType)), [allEvents, eventType, query]);
  const pageSize = 4;
  const pageCount = Math.max(1, Math.ceil(eventRows.length / pageSize));
  const currentPage = Math.min(page, pageCount);
  const visibleRows = eventRows.slice((currentPage - 1) * pageSize, currentPage * pageSize);
  const totalEvents = allEvents.reduce((sum, [, value]) => sum + Number(value), 0);
  const maximumEventCount = Math.max(...allEvents.map(([, value]) => Number(value)), 1);
  const exportCsv = () => {
    const rows = [['Event', 'Count', 'Share'], ...eventRows.map(([key, value]) => [eventLabels[key] || key, value, `${((Number(value) / Math.max(totalEvents, 1)) * 100).toFixed(1)}%`])];
    downloadFile(rows.map((row) => row.map(csvValue).join(',')).join('\n'), `${tenant.slug}-analytics.csv`);
  };
  const resetAnd = (fn) => (value) => { fn(value); setPage(1); };

  return <div className={dark ? 'app-shell dark' : 'app-shell'}>
    <Sidebar activePage="overview" onNavigate={onNavigate} />
    <main className="main-content">
      <header className="topbar"><div className="breadcrumb">Workspace <ChevronRight size={14} /> <strong>Overview</strong></div><div className="top-actions"><button type="button" className="icon-button" aria-label="Search events" onClick={() => searchInput.current?.focus()}><Search size={18} /></button><button type="button" className="icon-button" aria-label="Notifications" aria-expanded={notificationsOpen} onClick={() => setNotificationsOpen((value) => !value)}><Bell size={18} /><i /></button><button type="button" className="icon-button" aria-label="Toggle dark mode" onClick={() => setDark((value) => !value)}>{dark ? <Sun size={18} /> : <Moon size={18} />}</button></div>{notificationsOpen && <aside className="notification-popover" role="status"><strong>All caught up</strong><span>There are no new workspace notifications.</span></aside>}</header>
      <section className="page-heading"><div><div className="eyebrow">{loading ? 'Refreshing data' : 'Live analytics'}</div><h1>Good morning, Alex <span>✦</span></h1><p>Here’s what’s happening across {tenant.name}.</p></div><div className="heading-actions"><label className="range-select"><select value={range} onChange={(event) => { setRange(event.target.value); setPage(1); }} aria-label="Date range"><option>Last 7 days</option><option>Last 30 days</option><option>Last 24 hours</option></select><ChevronDown size={15} /></label><button type="button" className="export-button" onClick={exportCsv}><Download size={16} /> Export CSV</button></div></section>
      <section className="metric-grid"><MetricCard label="Daily active users" value={number(data.dailyActiveUsers)} change="12.8%" icon={Users} accent="coral" /><MetricCard label="Total events" value={number(totalEvents)} change="8.4%" icon={Activity} accent="blue" /><MetricCard label="Conversion rate" value="27.3%" change="4.1%" icon={ArrowUpRight} accent="mint" /><MetricCard label="P95 latency" value={`${data.latency?.p95Ms || 0}ms`} change="6.2%" icon={Zap} accent="amber" /></section>
      <section className="dashboard-grid"><article className="panel chart-panel"><div className="panel-heading"><div><h2>Activity overview</h2><p>{activityLoaded ? 'Processed events for the selected period' : 'Preview data while live activity is unavailable'}</p></div><div className="chart-controls" aria-label="Chart metrics"><button type="button" className={chartMetric === 'both' ? 'active' : ''} onClick={() => setChartMetric('both')}>Both</button><button type="button" className={chartMetric === 'users' ? 'active' : ''} onClick={() => setChartMetric('users')}>Users</button><button type="button" className={chartMetric === 'events' ? 'active' : ''} onClick={() => setChartMetric('events')}>Events</button></div></div><div className="chart-wrap">{activity.length ? <ResponsiveContainer width="100%" height={250}><AreaChart data={activity}><defs><linearGradient id="userFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#f27462" stopOpacity=".28" /><stop offset="100%" stopColor="#f27462" stopOpacity="0" /></linearGradient><linearGradient id="eventFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#65a9db" stopOpacity=".18" /><stop offset="100%" stopColor="#65a9db" stopOpacity="0" /></linearGradient></defs><CartesianGrid vertical={false} stroke="var(--grid)" /><XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fill: 'var(--muted)', fontSize: 11 }} /><YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--muted)', fontSize: 11 }} /><Tooltip contentStyle={{ background: 'var(--panel)', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--text)' }} /><Area hide={chartMetric === 'events'} type="monotone" dataKey="users" name="Active users" stroke="#f27462" strokeWidth={2.5} fill="url(#userFill)" /><Area hide={chartMetric === 'users'} type="monotone" dataKey="events" name="Events" stroke="#65a9db" strokeWidth={2.5} fill="url(#eventFill)" /></AreaChart></ResponsiveContainer> : <div className="empty-state">No processed events in this period.</div>}</div></article>
        <article className="panel retention-panel"><div className="panel-heading"><div><h2>Retention</h2><p>Returning users by cohort</p></div><button type="button" className="more-button" aria-label="Toggle retention details" aria-expanded={retentionDetailOpen} onClick={() => setRetentionDetailOpen((value) => !value)}>{retentionDetailOpen ? 'Hide' : 'Details'}</button></div>{retentionDetailOpen && <p className="panel-note">Retention is calculated from distinct users with processed events in the selected range.</p>}<div className="retention-score">63.8% <span>+5.2%</span></div><div className="retention-bars">{(data.retention || fallbackData.retention).map((item) => <div className="retention-row" key={item.day}><span>Day {item.day}</span><div className="bar-track"><i style={{ width: `${Math.min(100, (item.users / (data.dailyActiveUsers || 1)) * 100)}%` }} /></div><strong>{Math.round((item.users / (data.dailyActiveUsers || 1)) * 100)}%</strong></div>)}</div><div className="retention-footer"><span>Strongest cohort</span><strong>Week 1</strong></div></article></section>
      <section className="dashboard-grid lower-grid"><article className="panel"><div className="panel-heading"><div><h2>Top events</h2><p>Most active event types this period</p></div><button type="button" className="text-button" onClick={exportCsv}>Export CSV <Download size={14} /></button></div><div className="table-tools"><div className="search-box"><Search size={15} /><input ref={searchInput} value={query} onChange={(event) => resetAnd(setQuery)(event.target.value)} placeholder="Search events" aria-label="Search events" /></div><label className="filter-button"><span className="sr-only">Filter events</span><select value={eventType} onChange={(event) => resetAnd(setEventType)(event.target.value)}><option value="all">All events</option>{allEvents.map(([key]) => <option key={key} value={key}>{eventLabels[key] || key}</option>)}</select><ChevronDown size={14} /></label></div><div className="table-scroll"><table><thead><tr><th>Event name</th><th>Volume</th><th>Share</th><th>Trend</th></tr></thead><tbody>{visibleRows.map(([key, value], index) => <tr key={key}><td><span className={`event-dot dot-${index}`} />{eventLabels[key] || key}</td><td><strong>{number(value)}</strong></td><td><div className="share"><span style={{ width: `${Math.min(100, (Number(value) / maximumEventCount) * 100)}%` }} /></div></td><td className="trend"><ArrowUpRight size={14} /> {((Number(value) / Math.max(totalEvents, 1)) * 100).toFixed(1)}%</td></tr>)}{!visibleRows.length && <tr><td colSpan="4" className="empty-table">No events match the current filters.</td></tr>}</tbody></table></div><div className="pagination"><span>Showing {eventRows.length ? (currentPage - 1) * pageSize + 1 : 0}-{Math.min(currentPage * pageSize, eventRows.length)} of {eventRows.length} events</span><div><button type="button" disabled={currentPage === 1} onClick={() => setPage((value) => value - 1)} aria-label="Previous page"><ChevronLeft size={15} /></button><button type="button" disabled={currentPage >= pageCount} onClick={() => setPage((value) => value + 1)} aria-label="Next page"><ChevronRight size={15} /></button></div></div></article>
        <article className="panel funnel-panel"><div className="panel-heading"><div><h2>Conversion funnel</h2><p>From first touch to purchase</p></div><button type="button" className="more-button" aria-label="Toggle funnel details" aria-expanded={funnelDetailOpen} onClick={() => setFunnelDetailOpen((value) => !value)}>{funnelDetailOpen ? 'Hide' : 'Details'}</button></div>{funnelDetailOpen && <p className="panel-note">Stages are ranked by processed event volume for the selected date range.</p>}<div className="funnel-list">{(data.funnels || fallbackData.funnels).map((item, index) => <div className="funnel-item" key={item.eventType}><div className="funnel-meta"><span>{String(index + 1).padStart(2, '0')}</span><strong>{item.eventType}</strong><b>{number(item.count)}</b></div><div className="funnel-track"><i className={`funnel-fill fill-${index}`} style={{ width: `${Math.max(8, (item.count / (data.funnels?.[0]?.count || 1)) * 100)}%` }} /></div><small>{index === 0 ? 'Entry point' : `${Math.round((item.count / (data.funnels?.[0]?.count || 1)) * 100)}% conversion`}</small></div>)}</div></article></section>
      <footer className="page-footer"><span>Data refreshes when the date range or organization changes</span><span className="status"><i /> {activityLoaded ? 'Live data connected' : 'Preview data shown'}</span><button type="button" onClick={() => window.print()}><FileText size={14} /> Export PDF</button></footer>
    </main>
  </div>;
}
