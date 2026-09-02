import { useEffect, useMemo, useState } from 'react';
import {
	Activity, ArrowDownRight, ArrowUpRight, BarChart3, Bell, ChevronDown, ChevronLeft,
	ChevronRight, Download, FileText, LayoutDashboard, Moon, MoreHorizontal, Search,
	Sun, Users, Zap,
} from 'lucide-react';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getAnalyticsSummary } from '../services/api';
import { useTenant } from '../context/TenantContext';

const fallbackData = {
	dailyActiveUsers: 2847,
	eventCounts: { page_view: 18240, checkout_started: 6420, purchase_completed: 3891, search: 2190, signup: 1480 },
	topEvents: { page_view: 18240, checkout_started: 6420, purchase_completed: 3891, search: 2190, signup: 1480 },
	funnels: [
		{ stage: 1, eventType: 'Landing page', count: 10000 },
		{ stage: 2, eventType: 'Product viewed', count: 7420 },
		{ stage: 3, eventType: 'Checkout started', count: 4210 },
		{ stage: 4, eventType: 'Purchase completed', count: 2730 },
	],
	retention: [{ day: 1, users: 2847 }, { day: 7, users: 1810 }, { day: 14, users: 1240 }, { day: 30, users: 834 }],
	latency: { p95Ms: 182, p99Ms: 426 },
};

const chartData = [
	{ day: 'Aug 27', users: 1810, events: 4980 }, { day: 'Aug 28', users: 2140, events: 6210 },
	{ day: 'Aug 29', users: 1980, events: 5720 }, { day: 'Aug 30', users: 2470, events: 7440 },
	{ day: 'Aug 31', users: 2310, events: 6830 }, { day: 'Sep 01', users: 2690, events: 8120 },
	{ day: 'Sep 02', users: 2847, events: 9230 },
];

const eventLabels = { page_view: 'Page view', checkout_started: 'Checkout started', purchase_completed: 'Purchase completed', search: 'Search', signup: 'Sign up' };
const money = (value) => new Intl.NumberFormat('en-US').format(value);

function downloadFile(content, name, type) {
	const link = document.createElement('a');
	link.href = URL.createObjectURL(new Blob([content], { type }));
	link.download = name;
	link.click();
	URL.revokeObjectURL(link.href);
}

function exportCsv(data) {
	const rows = [['Event', 'Count'], ...Object.entries(data.eventCounts).map(([key, value]) => [eventLabels[key] || key, value])];
	downloadFile(rows.map((row) => row.join(',')).join('\n'), 'northstar-analytics.csv', 'text/csv');
}

function MetricCard({ label, value, change, icon: Icon, accent }) {
	return <article className="metric-card">
		<div className={`metric-icon ${accent}`}><Icon size={19} /></div>
		<div className="metric-label">{label}<MoreHorizontal size={16} /></div>
		<div className="metric-value">{value}</div>
		<div className="metric-change"><ArrowUpRight size={14} /> {change} <span>vs last period</span></div>
	</article>;
}

function DashboardPage() {
	const { tenant, tenants, setTenant } = useTenant();
	const [data, setData] = useState(fallbackData);
	const [dark, setDark] = useState(true);
	const [range, setRange] = useState('Last 7 days');
	const [page, setPage] = useState(1);
	const [query, setQuery] = useState('');
	const [loading, setLoading] = useState(false);

	useEffect(() => {
		const to = new Date();
		const from = new Date(to);
		from.setDate(to.getDate() - (range === 'Last 30 days' ? 30 : range === 'Last 24 hours' ? 1 : 7));
		setLoading(true);
		getAnalyticsSummary(tenant.id, from, to).then(setData).catch(() => setData(fallbackData)).finally(() => setLoading(false));
	}, [tenant.id, range]);

	const eventRows = useMemo(() => Object.entries(data.eventCounts || {}).filter(([key]) => (eventLabels[key] || key).toLowerCase().includes(query.toLowerCase())), [data, query]);
	const pageSize = 4;
	const pageCount = Math.max(1, Math.ceil(eventRows.length / pageSize));
	const visibleRows = eventRows.slice((page - 1) * pageSize, page * pageSize);

	return <div className={dark ? 'app-shell dark' : 'app-shell'}>
		<aside className="sidebar">
			<div className="brand"><div className="brand-mark"><Activity size={20} /></div><span>signal<span className="brand-dot">.</span></span></div>
			<div className="workspace-label">Workspace</div>
			<div className="tenant-select-wrap">
				<select value={tenant.id} onChange={(event) => setTenant(event.target.value)} className="tenant-select" aria-label="Switch organization">
					{tenants.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
				</select>
				<div className={`tenant-avatar ${tenant.tone}`}>{tenant.initials}</div><ChevronDown size={15} className="tenant-chevron" />
			</div>
			<nav className="nav"><div className="nav-section">Manage</div><a className="nav-link active"><LayoutDashboard size={17} /> Overview</a><a className="nav-link"><BarChart3 size={17} /> Reports</a><a className="nav-link"><Zap size={17} /> Event streams</a><div className="nav-section spaced">Workspace</div><a className="nav-link"><Users size={17} /> Members <span className="nav-badge">8</span></a><a className="nav-link"><FileText size={17} /> API keys</a></nav>
			<div className="sidebar-bottom"><div className="upgrade"><div className="upgrade-title">Growth plan</div><div className="upgrade-copy">74% of monthly events used</div><div className="progress"><span /></div><button>Manage plan <ArrowUpRight size={14} /></button></div><div className="profile"><div className="profile-avatar">AM</div><div><strong>Alex Morgan</strong><span>Admin</span></div><MoreHorizontal size={17} /></div></div>
		</aside>
		<main className="main-content">
			<header className="topbar"><div className="breadcrumb">Workspace <ChevronRight size={14} /> <strong>Overview</strong></div><div className="top-actions"><button className="icon-button" aria-label="Search"><Search size={18} /></button><button className="icon-button" aria-label="Notifications"><Bell size={18} /><i /></button><button className="icon-button" aria-label="Toggle dark mode" onClick={() => setDark((value) => !value)}>{dark ? <Sun size={18} /> : <Moon size={18} />}</button></div></header>
			<section className="page-heading"><div><div className="eyebrow">{loading ? 'Refreshing data' : 'Tuesday, September 02, 2026'}</div><h1>Good morning, Alex <span>✦</span></h1><p>Here’s what’s happening across {tenant.name}.</p></div><div className="heading-actions"><div className="range-select"><select value={range} onChange={(event) => { setRange(event.target.value); setPage(1); }} aria-label="Date range"><option>Last 7 days</option><option>Last 30 days</option><option>Last 24 hours</option></select><ChevronDown size={15} /></div><button className="export-button" onClick={() => exportCsv(data)}><Download size={16} /> Export <ChevronDown size={14} /></button></div></section>
			<section className="metric-grid"><MetricCard label="Daily active users" value={money(data.dailyActiveUsers)} change="12.8%" icon={Users} accent="coral" /><MetricCard label="Total events" value={money(Object.values(data.eventCounts || {}).reduce((sum, value) => sum + value, 0))} change="8.4%" icon={Activity} accent="blue" /><MetricCard label="Conversion rate" value="27.3%" change="4.1%" icon={ArrowUpRight} accent="mint" /><MetricCard label="P95 latency" value={`${data.latency?.p95Ms || 0}ms`} change="6.2%" icon={Zap} accent="amber" /></section>
			<section className="dashboard-grid"><article className="panel chart-panel"><div className="panel-heading"><div><h2>Activity overview</h2><p>Unique users and event volume</p></div><div className="legend"><span><i className="legend-dot coral" /> Active users</span><span><i className="legend-dot blue" /> Events</span></div></div><div className="chart-wrap"><ResponsiveContainer width="100%" height={250}><AreaChart data={chartData}><defs><linearGradient id="userFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#f27462" stopOpacity=".28" /><stop offset="100%" stopColor="#f27462" stopOpacity="0" /></linearGradient></defs><CartesianGrid vertical={false} stroke="var(--grid)" /><XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fill: 'var(--muted)', fontSize: 11 }} /><YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--muted)', fontSize: 11 }} /><Tooltip contentStyle={{ background: 'var(--panel)', border: '1px solid var(--border)', borderRadius: 8, color: 'var(--text)' }} /><Area type="monotone" dataKey="users" stroke="#f27462" strokeWidth={2.5} fill="url(#userFill)" /><Area type="monotone" dataKey="events" stroke="#65a9db" strokeWidth={2} fill="none" /></AreaChart></ResponsiveContainer></div></article>
				<article className="panel retention-panel"><div className="panel-heading"><div><h2>Retention</h2><p>Returning users by cohort</p></div><button className="more-button" aria-label="More retention options"><MoreHorizontal size={18} /></button></div><div className="retention-score">63.8% <span>+5.2%</span></div><div className="retention-bars">{(data.retention || fallbackData.retention).map((item) => <div className="retention-row" key={item.day}><span>Day {item.day}</span><div className="bar-track"><i style={{ width: `${(item.users / (data.dailyActiveUsers || 2847)) * 100}%` }} /></div><strong>{Math.round((item.users / (data.dailyActiveUsers || 2847)) * 100)}%</strong></div>)}</div><div className="retention-footer"><span>Strongest cohort</span><strong>Week 1</strong></div></article>
			</section>
			<section className="dashboard-grid lower-grid"><article className="panel"><div className="panel-heading"><div><h2>Top events</h2><p>Most active event types this period</p></div><button className="text-button" onClick={() => exportCsv(data)}>Export CSV <Download size={14} /></button></div><div className="table-tools"><div className="search-box"><Search size={15} /><input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1); }} placeholder="Search events" /></div><button className="filter-button">All events <ChevronDown size={14} /></button></div><div className="table-scroll"><table><thead><tr><th>Event name</th><th>Volume</th><th>Share</th><th>Trend</th></tr></thead><tbody>{visibleRows.map(([key, value], index) => <tr key={key}><td><span className={`event-dot dot-${index}`} />{eventLabels[key] || key}</td><td><strong>{money(value)}</strong></td><td><div className="share"><span style={{ width: `${Math.min(100, (value / 18240) * 100)}%` }} /></div></td><td className="trend"><ArrowUpRight size={14} /> {['18.2%', '12.4%', '9.8%', '6.1%', '4.8%'][index] || '3.2%'}</td></tr>)}</tbody></table></div><div className="pagination"><span>Showing {eventRows.length ? (page - 1) * pageSize + 1 : 0}-{Math.min(page * pageSize, eventRows.length)} of {eventRows.length} events</span><div><button disabled={page === 1} onClick={() => setPage((value) => value - 1)} aria-label="Previous page"><ChevronLeft size={15} /></button><button disabled={page >= pageCount} onClick={() => setPage((value) => value + 1)} aria-label="Next page"><ChevronRight size={15} /></button></div></div></article>
				<article className="panel funnel-panel"><div className="panel-heading"><div><h2>Conversion funnel</h2><p>From first touch to purchase</p></div><button className="more-button" aria-label="More funnel options"><MoreHorizontal size={18} /></button></div><div className="funnel-list">{(data.funnels || fallbackData.funnels).map((item, index) => <div className="funnel-item" key={item.eventType}><div className="funnel-meta"><span>{String(index + 1).padStart(2, '0')}</span><strong>{item.eventType}</strong><b>{money(item.count)}</b></div><div className="funnel-track"><i className={`funnel-fill fill-${index}`} style={{ width: `${Math.max(8, (item.count / (data.funnels?.[0]?.count || 10000)) * 100)}%` }} /></div><small>{index === 0 ? 'Entry point' : `${Math.round((item.count / (data.funnels?.[0]?.count || 10000)) * 100)}% conversion`}</small></div>)}</div></article></section>
			<footer className="page-footer"><span>Data updates every 15 minutes</span><span className="status"><i /> All systems operational</span><button onClick={() => window.print()}><FileText size={14} /> Export PDF</button></footer>
		</main>
	</div>;
}

export default DashboardPage;
