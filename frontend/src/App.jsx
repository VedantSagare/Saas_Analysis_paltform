export default function App() {
  return (
    <main className="flex min-h-screen items-center justify-center px-6 py-12">
      <div className="w-full max-w-3xl rounded-2xl border border-slate-700 bg-slate-900/70 p-8 shadow-2xl shadow-slate-950/50 backdrop-blur-sm">
        <div className="mb-4 inline-flex items-center rounded-full border border-indigo-500/30 bg-indigo-500/10 px-3 py-1 text-xs font-medium uppercase tracking-[0.2em] text-indigo-300">
          Frontend Scaffold
        </div>
        <h1 className="text-3xl font-bold tracking-tight text-white sm:text-4xl">
          SaaS Analytics Platform
        </h1>
        <p className="mt-4 text-base text-slate-300">
          React frontend initialized with Vite, Tailwind CSS, analytics/chart dependencies,
          and project configuration. Application pages and business logic will be added later.
        </p>
      </div>
    </main>
  );
}
