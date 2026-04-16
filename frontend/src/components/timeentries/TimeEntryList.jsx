import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { timeEntries, employees, projects } from '../../api/client';

export default function TimeEntryList() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      setLoading(true);
      setList(await timeEntries.list());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this time entry?')) return;
    try {
      await timeEntries.delete(id);
      setList((prev) => prev.filter((t) => t.id !== id));
    } catch (e) {
      setError(e.message);
    }
  }

  const filtered = list.filter((t) => filter === 'ALL' || t.billableStatus === filter);

  return (
    <div>
      <div className="page-header">
        <h1>Time Entries</h1>
        <Link to="/time-entries/new" className="btn btn-primary">+ Log Time</Link>
      </div>

      {error && <div className="error-box">{error}</div>}

      <div className="table-container">
        <div className="table-toolbar">
          <div className="filter-pills">
            {['ALL', 'BILLABLE', 'NON_BILLABLE', 'INTERNAL'].map((s) => (
              <button key={s} className={`filter-pill ${filter === s ? 'active' : ''}`} onClick={() => setFilter(s)}>
                {s === 'ALL' ? 'All' : s.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state"><div className="empty-icon">⏱️</div><p>No time entries found</p></div>
        ) : (
          <table>
            <thead><tr><th>Employee</th><th>Project</th><th>Date</th><th>Hours</th><th>Description</th><th>Type</th><th>Actions</th></tr></thead>
            <tbody>
              {filtered.map((t) => (
                <tr key={t.id}>
                  <td>{t.employee ? `${t.employee.firstName} ${t.employee.lastName}` : '—'}</td>
                  <td>{t.project ? <Link to={`/projects/${t.project.id}`}>{t.project.name}</Link> : '—'}</td>
                  <td>{t.date ? new Date(t.date).toLocaleDateString() : '—'}</td>
                  <td><strong>{t.hoursWorked}h</strong></td>
                  <td className="text-sm">{t.description || '—'}</td>
                  <td><span className={`badge badge-${t.billableStatus?.toLowerCase()}`}>{t.billableStatus}</span></td>
                  <td>
                    <div className="btn-group">
                      <Link to={`/time-entries/${t.id}/edit`} className="btn btn-sm btn-secondary">Edit</Link>
                      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(t.id)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
