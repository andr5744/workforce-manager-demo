import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { employees } from '../../api/client';

const STATUSES = ['ALL', 'ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED'];

export default function EmployeeList() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchParams] = useSearchParams();

  useEffect(() => {
    load();
  }, []);

  async function load() {
    try {
      setLoading(true);
      const data = await employees.list();
      setList(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this employee?')) return;
    try {
      await employees.delete(id);
      setList((prev) => prev.filter((e) => e.id !== id));
    } catch (e) {
      setError(e.message);
    }
  }

  const filtered = list.filter((e) => {
    const matchesSearch = !search ||
      `${e.firstName} ${e.lastName}`.toLowerCase().includes(search.toLowerCase()) ||
      e.email?.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || e.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const fmt = (n) => n != null ? `$${Number(n).toLocaleString()}` : '—';

  return (
    <div>
      <div className="page-header">
        <h1>Employees</h1>
        <Link to="/employees/new" className="btn btn-primary">+ Add Employee</Link>
      </div>

      {error && <div className="error-box">{error}</div>}

      <div className="table-container">
        <div className="table-toolbar">
          <input
            className="search-input"
            placeholder="Search employees..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <div className="filter-pills">
            {STATUSES.map((s) => (
              <button
                key={s}
                className={`filter-pill ${statusFilter === s ? 'active' : ''}`}
                onClick={() => setStatusFilter(s)}
              >
                {s === 'ALL' ? 'All' : s.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="loading"><div className="spinner" /></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">👤</div>
            <p>No employees found</p>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Employee</th>
                <th>Department</th>
                <th>Job Title</th>
                <th>Salary</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((e) => (
                <tr key={e.id}>
                  <td>
                    <div className="cell-main">
                      <Link to={`/employees/${e.id}`}>{e.firstName} {e.lastName}</Link>
                    </div>
                    <div className="cell-sub">{e.email}</div>
                  </td>
                  <td>{e.department?.name || '—'}</td>
                  <td>{e.jobTitle || '—'}</td>
                  <td>{fmt(e.salary)}</td>
                  <td><span className={`badge badge-${e.status?.toLowerCase()}`}>{e.status}</span></td>
                  <td>
                    <div className="btn-group">
                      <Link to={`/employees/${e.id}`} className="btn btn-sm btn-secondary">View</Link>
                      <Link to={`/employees/${e.id}/edit`} className="btn btn-sm btn-secondary">Edit</Link>
                      <button className="btn btn-sm btn-danger" onClick={() => handleDelete(e.id)}>Delete</button>
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
