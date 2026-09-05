import { Activity, ArrowUpRight, BarChart3, ChevronDown, FileText, LayoutDashboard, MoreHorizontal, Users, Zap } from 'lucide-react';
import { useTenant } from '../context/TenantContext';

const navigation = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard },
  { id: 'reports', label: 'Reports', icon: BarChart3 },
  { id: 'events', label: 'Event streams', icon: Zap },
];

export default function Sidebar({ activePage, onNavigate }) {
  const { tenant, tenants, setTenant } = useTenant();

  return <aside className="sidebar">
    <div className="brand"><div className="brand-mark"><Activity size={20} /></div><span>signal<span className="brand-dot">.</span></span></div>
    <div className="workspace-label">Workspace</div>
    <div className="tenant-select-wrap"><select value={tenant.id} onChange={(event) => setTenant(event.target.value)} className="tenant-select" aria-label="Switch organization">{tenants.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><div className={`tenant-avatar ${tenant.tone}`}>{tenant.initials}</div><ChevronDown size={15} className="tenant-chevron" /></div>
    <nav className="nav" aria-label="Main navigation"><div className="nav-section">Manage</div>{navigation.map(({ id, label, icon: Icon }) => <button key={id} type="button" className={`nav-link ${activePage === id ? 'active' : ''}`} onClick={() => onNavigate(id)}><Icon size={17} /> {label}</button>)}<div className="nav-section spaced">Workspace</div><span className="nav-link nav-link-muted"><Users size={17} /> Members <span className="nav-badge">8</span></span><span className="nav-link nav-link-muted"><FileText size={17} /> API keys</span></nav>
    <div className="sidebar-bottom"><div className="upgrade"><div className="upgrade-title">Growth plan</div><div className="upgrade-copy">74% of monthly events used</div><div className="progress"><span /></div><button type="button">Manage plan <ArrowUpRight size={14} /></button></div><div className="profile"><div className="profile-avatar">AM</div><div><strong>Alex Morgan</strong><span>Admin</span></div><MoreHorizontal size={17} /></div></div>
  </aside>;
}
