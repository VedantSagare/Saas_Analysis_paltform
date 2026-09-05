import { BarChart3, ChevronDown, Download, Moon, Sun, TrendingUp } from 'lucide-react';
import Sidebar from '../components/Sidebar';
import { useTenant } from '../context/TenantContext';

const reports = [
  { name: 'Acquisition performance', description: 'Visitors, signups, and sources', value: '12.8%', detail: 'growth in active users' },
  { name: 'Conversion funnel', description: 'Landing page to completed purchase', value: '27.3%', detail: 'overall conversion rate' },
  { name: 'Retention cohorts', description: 'Returning users over 30 days', value: '63.8%', detail: 'week-one retention' },
];

export default function ReportsPage({ dark, setDark, onNavigate }) {
  const { tenant } = useTenant();
  const downloadReport = () => {
    const content = ['Report,Metric,Value', ...reports.map((report) => `${report.name},${report.detail},${report.value}`)].join('\n');
    const link = document.createElement('a');
    link.href = URL.createObjectURL(new Blob([content], { type: 'text/csv' }));
    link.download = `${tenant.slug}-reports.csv`;
    link.click();
    URL.revokeObjectURL(link.href);
  };

  return <div className={dark ? 'app-shell dark' : 'app-shell'}>
    <Sidebar activePage="reports" onNavigate={onNavigate} />
    <main className="main-content">
      <header className="topbar"><div className="breadcrumb">Workspace <ChevronDown size={14} /> <strong>Reports</strong></div><button type="button" className="icon-button" aria-label="Toggle dark mode" onClick={() => setDark((value) => !value)}>{dark ? <Sun size={18} /> : <Moon size={18} />}</button></header>
      <section className="page-heading"><div><div className="eyebrow">Analytics reports</div><h1>Reports</h1><p>Saved views of the most important signals for {tenant.name}.</p></div><button type="button" className="export-button" onClick={downloadReport}><Download size={16} /> Export CSV</button></section>
      <section className="report-grid">{reports.map((report) => <article className="panel report-card" key={report.name}><div className="report-icon"><BarChart3 size={19} /></div><h2>{report.name}</h2><p>{report.description}</p><strong>{report.value}</strong><span><TrendingUp size={14} /> {report.detail}</span><button type="button" className="text-button" onClick={() => onNavigate('events')}>View related events</button></article>)}</section>
    </main>
  </div>;
}
