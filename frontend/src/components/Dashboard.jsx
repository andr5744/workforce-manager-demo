import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { employees, departments, projects, reports } from '../api/client';

export default function Dashboard() {
  const { user, hasRole } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        const results = {};
        if (hasRole('ADMIN', 'MANAGER')) {
          const [hc, ps, depts, emps, projs] = await Promise.all([
            reports.headcount(),
            reports.projectSummary(),
            departments.list(),
            employees.list(),
            projects.list(),
          ]);
          results.headcount = hc;
          results.projectSummary = ps;
          results.departments = depts;
          results.employees = emps;
          results.projects = projs;
        } else {
          const projs = await projects.list();
          results.projects = projs;
        }
        setData(results);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [hasRole]);

  if (loading) return <div className="loading"><div className="spinner" /></div>;
  if (error) return <div className="error-box">{error}</div>;

  const isManager = hasRole('ADMIN', 'MANAGER');

  return (
    <div>
      <div className="page-header">
        <h1>Dashboard</h1>
        <span className="text-muted text-sm">Welcome back, {user.label}</span>
      </div>

      {isManager && data.headcount && (
        <>
          <div className="summary-grid">
            <div className="summary-card">
              <div className="summary-icon blue">👤</div>
              <div>
                <div className="summary-value">{data.headcount.total}</div>
                <div className="summary-label">Total Employees</div>
              </div>
            </div>
            <div className="summary-card">
              <div className="summary-icon green">🏢</div>
              <div>
                <div className="summary-value">{data.departments?.length || 0}</div>
                <div className="summary-label">Departments</div>
              </div>
            </div>
            <div className="summary-card">
              <div className="summary-icon amber">📁</div>
              <div>
                <div className="summary-value">{data.projectSummary?.total || 0}</div>
                <div className="summary-label">Total Projects</div>
              </div>
            </div>
            <div className="summary-card">
              <div className="summary-icon red">⚠️</div>
              <div>
                <div className="summary-value">{data.projectSummary?.overdueCount || 0}</div>
                <div className="summary-label">Overdue Projects</div>
              </div>
            </div>
          </div>

          <div className="chart-grid">
            <div className="card">
              <div className="card-header">Employees by Status</div>
              <div className="card-body">
                <div className="bar-chart">
                  {data.headcount.byStatus && Object.entries(data.headcount.byStatus).map(([status, count]) => (
                    <div key={status} className="bar-row">
                      <span className="bar-label">{status}</span>
                      <div className="bar-track">
                        <div
                          className={`bar-fill ${status === 'ACTIVE' ? 'green' : status === 'ON_LEAVE' ? 'blue' : 'amber'}`}
                          style={{ width: `${Math.max((count / data.headcount.total) * 100, 12)}%` }}
                        >
                          {count}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="card">
              <div className="card-header">Projects by Status</div>
              <div className="card-body">
                <div className="bar-chart">
                  {data.projectSummary.byStatus && Object.entries(data.projectSummary.byStatus).map(([status, count]) => {
                    const colors = { PLANNING: 'blue', IN_PROGRESS: 'green', ON_HOLD: 'amber', COMPLETED: 'purple', CANCELLED: 'red' };
                    return (
                      <div key={status} className="bar-row">
                        <span className="bar-label">{status.replace('_', ' ')}</span>
                        <div className="bar-track">
                          <div
                            className={`bar-fill ${colors[status] || 'blue'}`}
                            style={{ width: `${Math.max((count / data.projectSummary.total) * 100, 12)}%` }}
                          >
                            {count}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>

          <div className="chart-grid">
            <div className="card">
              <div className="card-header">
                <span>Recent Employees</span>
                <Link to="/employees" className="btn btn-sm btn-secondary">View All</Link>
              </div>
              <div className="card-body" style={{ padding: 0 }}>
                <table>
                  <thead>
                    <tr><th>Name</th><th>Department</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {(data.employees || []).slice(0, 5).map((e) => (
                      <tr key={e.id}>
                        <td>
                          <div className="cell-main">{e.firstName} {e.lastName}</div>
                          <div className="cell-sub">{e.jobTitle}</div>
                        </td>
                        <td>{e.department?.name || '—'}</td>
                        <td><span className={`badge badge-${e.status?.toLowerCase()}`}>{e.status}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="card">
              <div className="card-header">
                <span>Active Projects</span>
                <Link to="/projects" className="btn btn-sm btn-secondary">View All</Link>
              </div>
              <div className="card-body" style={{ padding: 0 }}>
                <table>
                  <thead>
                    <tr><th>Project</th><th>Client</th><th>Budget Used</th></tr>
                  </thead>
                  <tbody>
                    {(data.projects || []).filter(p => p.status === 'IN_PROGRESS').slice(0, 5).map((p) => {
                      const pct = p.budget > 0 ? (p.budgetSpent / p.budget) * 100 : 0;
                      return (
                        <tr key={p.id}>
                          <td>
                            <div className="cell-main">{p.name}</div>
                            <div className="cell-sub">{p.status}</div>
                          </td>
                          <td>{p.clientName || '—'}</td>
                          <td style={{ width: 160 }}>
                            <div className="text-sm">{pct.toFixed(0)}%</div>
                            <div className="budget-bar">
                              <div className={`budget-fill ${pct < 60 ? 'green' : pct < 85 ? 'amber' : 'red'}`} style={{ width: `${pct}%` }} />
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </>
      )}

      {!isManager && (
        <div>
          <div className="summary-grid">
            <div className="summary-card">
              <div className="summary-icon blue">📁</div>
              <div>
                <div className="summary-value">{data.projects?.length || 0}</div>
                <div className="summary-label">Available Projects</div>
              </div>
            </div>
          </div>
          <div className="card">
            <div className="card-header">
              <span>Projects</span>
              <Link to="/projects" className="btn btn-sm btn-secondary">View All</Link>
            </div>
            <div className="card-body" style={{ padding: 0 }}>
              <table>
                <thead><tr><th>Name</th><th>Status</th><th>Client</th></tr></thead>
                <tbody>
                  {(data.projects || []).slice(0, 8).map((p) => (
                    <tr key={p.id}>
                      <td><Link to={`/projects/${p.id}`}>{p.name}</Link></td>
                      <td><span className={`badge badge-${p.status?.toLowerCase()}`}>{p.status}</span></td>
                      <td>{p.clientName || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
