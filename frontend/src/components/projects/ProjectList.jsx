import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { projects } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

const STATUSES = ['ALL', 'PLANNING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED'];

export default function ProjectList() {
  const { hasRole } = useAuth();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      setLoading(true);
      setList(await projects.list());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this project?')) return;
    try {
      await projects.delete(id);
      setList((prev) => prev.filter((p) => p.id !== id));
    } catch (e) {
      setError(e.message);
    }
  }

  const filtered = list.filter((p) => {
    const matchesSearch = !search || p.name.toLowerCase().includes(search.toLowerCase()) || p.clientName?.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || p.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const fmt = (n) => n != null ? `$${Number(n).toLocaleString()}` : '—';

  return (
    <div>
      <div className="page-header">
        <h1>Projects</h1>
        {hasRole('ADMIN', 'MANAGER') && <Link to="/projects/new" className="btn btn-primary">+ New Project</Link>}
      </div>

      {error && <div className="error-box">{error}</div>}

      <div className="table-container">
        <div className="table-toolbar">
          <input className="search-input" placeholder="Search projects..." value={search} onChange={(e) => setSearch(e.target.value)} />
          <div className="filter-pills">
            {STATUSES.map((s) => (
              <button key={s} className={`filter-pill ${statusFilter === s ? 'active' : ''}`} onClick={() => setStatusFilter(s)}>
                {s === 'ALL' ? 'All' : s.replace(/_/g, ' ')}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">📁</div><p>No projects found</p></div>
        ) : (
          <table>
            <thead>
              <tr><th>Project</th><th>Client</th><th>Status</th><th>Budget</th><th>Budget Used</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filtered.map((p) => {
                const pct = p.budget > 0 ? (p.budgetSpent / p.budget) * 100 : 0;
                return (
                  <tr key={p.id}>
                    <td>
                      <div className="cell-main"><Link to={`/projects/${p.id}`}>{p.name}</Link></div>
                      <div className="cell-sub">{p.description?.substring(0, 60)}{p.description?.length > 60 ? '...' : ''}</div>
                    </td>
                    <td>{p.clientName || '—'}</td>
                    <td><span className={`badge badge-${p.status?.toLowerCase()}`}>{p.status?.replace(/_/g, ' ')}</span></td>
                    <td>{fmt(p.budget)}</td>
                    <td style={{ minWidth: 120 }}>
                      <div className="text-sm">{fmt(p.budgetSpent)} ({pct.toFixed(0)}%)</div>
                      <div className="budget-bar">
                        <div className={`budget-fill ${pct < 60 ? 'green' : pct < 85 ? 'amber' : 'red'}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                      </div>
                    </td>
                    <td>
                      <div className="btn-group">
                        <Link to={`/projects/${p.id}`} className="btn btn-sm btn-secondary">View</Link>
                        {hasRole('ADMIN', 'MANAGER') && <>
                          <Link to={`/projects/${p.id}/edit`} className="btn btn-sm btn-secondary">Edit</Link>
                          <button className="btn btn-sm btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
                        </>}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
