import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { departments } from '../../api/client';

export default function DepartmentList() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      setLoading(true);
      setList(await departments.list());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this department? This will fail if employees are assigned.')) return;
    try {
      await departments.delete(id);
      setList((prev) => prev.filter((d) => d.id !== id));
    } catch (e) {
      setError(e.message);
    }
  }

  const filtered = list.filter((d) =>
    !search || d.name.toLowerCase().includes(search.toLowerCase()) || d.location?.toLowerCase().includes(search.toLowerCase())
  );

  const fmt = (n) => n != null ? `$${Number(n).toLocaleString()}` : '—';

  return (
    <div>
      <div className="page-header">
        <h1>Departments</h1>
        <Link to="/departments/new" className="btn btn-primary">+ Add Department</Link>
      </div>

      {error && <div className="error-box">{error}</div>}

      <div className="table-container">
        <div className="table-toolbar">
          <input className="search-input" placeholder="Search departments..." value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>

        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🏢</div>
            <p>No departments found</p>
          </div>
        ) : (
          <table>
            <thead><tr><th>Department</th><th>Location</th><th>Budget</th><th>Head Count Limit</th><th>Actions</th></tr></thead>
            <tbody>
              {filtered.map((d) => (
                <tr key={d.id}>
                  <td><span className="cell-main">{d.name}</span></td>
                  <td>{d.location || '—'}</td>
                  <td>{fmt(d.budget)}</td>
                  <td>{d.headCountLimit ?? '—'}</td>
                  <td>
                    <div className="btn-group">
                      <Link to={`/departments/${d.id}/edit`} className="btn btn-sm btn-secondary">Edit</Link>
                      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(d.id)}>Delete</button>
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
