import { useState, useEffect } from 'react';
import { reports, departments as deptApi, employees as empApi } from '../../api/client';

export default function Reports() {
  const [headcount, setHeadcount] = useState(null);
  const [projectSummary, setProjectSummary] = useState(null);
  const [deptSummaries, setDeptSummaries] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Utilization
  const [employees, setEmployees] = useState([]);
  const [utilEmpId, setUtilEmpId] = useState('');
  const [utilStart, setUtilStart] = useState('');
  const [utilEnd, setUtilEnd] = useState('');
  const [utilResult, setUtilResult] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        const [hc, ps, depts, emps] = await Promise.all([
          reports.headcount(),
          reports.projectSummary(),
          deptApi.list(),
          empApi.list(),
        ]);
        setHeadcount(hc);
        setProjectSummary(ps);
        setDepartments(depts);
        setEmployees(emps);

        const summaries = await Promise.all(depts.map((d) => reports.departmentSummary(d.id).catch(() => null)));
        setDeptSummaries(summaries.filter(Boolean));
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  async function fetchUtilization(e) {
    e.preventDefault();
    if (!utilEmpId || !utilStart || !utilEnd) return;
    try {
      const res = await reports.utilization(utilEmpId, utilStart, utilEnd);
      setUtilResult(res);
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header"><h1>Reports & Analytics</h1></div>
      {error && <div className="error-box">{error}</div>}

      {/* Summary Cards */}
      {headcount && (
        <div className="summary-grid">
          <div className="summary-card">
            <div className="summary-icon blue">👥</div>
            <div>
              <div className="summary-value">{headcount.total}</div>
              <div className="summary-label">Total Headcount</div>
            </div>
          </div>
          <div className="summary-card">
            <div className="summary-icon green">📁</div>
            <div>
              <div className="summary-value">{projectSummary?.total || 0}</div>
              <div className="summary-label">Total Projects</div>
            </div>
          </div>
          <div className="summary-card">
            <div className="summary-icon red">⚠️</div>
            <div>
              <div className="summary-value">{projectSummary?.overdueCount || 0}</div>
              <div className="summary-label">Overdue Projects</div>
            </div>
          </div>
          <div className="summary-card">
            <div className="summary-icon amber">🏢</div>
            <div>
              <div className="summary-value">{departments.length}</div>
              <div className="summary-label">Departments</div>
            </div>
          </div>
        </div>
      )}

      <div className="chart-grid">
        {/* Employee Distribution */}
        {headcount && (
          <div className="card">
            <div className="card-header">Headcount by Status</div>
            <div className="card-body">
              <div className="bar-chart">
                {headcount.byStatus && Object.entries(headcount.byStatus).map(([status, count]) => {
                  const colors = { ACTIVE: 'green', INACTIVE: 'amber', ON_LEAVE: 'blue', TERMINATED: 'red' };
                  return (
                    <div key={status} className="bar-row">
                      <span className="bar-label">{status}</span>
                      <div className="bar-track">
                        <div className={`bar-fill ${colors[status] || 'blue'}`} style={{ width: `${Math.max((count / headcount.total) * 100, 12)}%` }}>
                          {count}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* Employees by Department */}
        {headcount?.byDepartment && (
          <div className="card">
            <div className="card-header">Headcount by Department</div>
            <div className="card-body">
              <div className="bar-chart">
                {Object.entries(headcount.byDepartment).map(([dept, count]) => {
                  const colors = ['blue', 'green', 'amber', 'purple', 'red'];
                  const idx = Object.keys(headcount.byDepartment).indexOf(dept);
                  return (
                    <div key={dept} className="bar-row">
                      <span className="bar-label">{dept}</span>
                      <div className="bar-track">
                        <div className={`bar-fill ${colors[idx % colors.length]}`} style={{ width: `${Math.max((count / headcount.total) * 100, 12)}%` }}>
                          {count}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Department Summaries */}
      {deptSummaries.length > 0 && (
        <div className="card detail-card mt-4">
          <div className="card-header">Department Summary</div>
          <div className="card-body" style={{ padding: 0 }}>
            <table>
              <thead><tr><th>Department</th><th>Total Employees</th><th>Active</th><th>On Leave</th></tr></thead>
              <tbody>
                {deptSummaries.map((ds, i) => (
                  <tr key={i}>
                    <td><strong>{departments.find(d => d.id === ds.departmentId)?.name || `Dept ${ds.departmentId}`}</strong></td>
                    <td>{ds.totalEmployees}</td>
                    <td><span className="badge badge-active">{ds.activeEmployees}</span></td>
                    <td><span className="badge badge-on_leave">{ds.onLeave}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Utilization Report */}
      <div className="card detail-card mt-4">
        <div className="card-header">Employee Utilization</div>
        <div className="card-body">
          <form onSubmit={fetchUtilization} style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: 20 }}>
            <div className="form-group">
              <label>Employee</label>
              <select className="form-control" value={utilEmpId} onChange={(e) => setUtilEmpId(e.target.value)} required>
                <option value="">Select...</option>
                {employees.map((emp) => <option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Start Date</label>
              <input className="form-control" type="date" value={utilStart} onChange={(e) => setUtilStart(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input className="form-control" type="date" value={utilEnd} onChange={(e) => setUtilEnd(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary">Run Report</button>
          </form>

          {utilResult && (
            <div className="card" style={{ background: '#f8fafc' }}>
              <div className="card-body">
                <div className="detail-row"><span className="detail-label">Employee</span><span className="detail-value">{utilResult.employeeName}</span></div>
                <div className="detail-row"><span className="detail-label">Total Hours</span><span className="detail-value">{utilResult.totalHoursLogged}h</span></div>
                <div className="detail-row"><span className="detail-label">Active Projects</span><span className="detail-value">{utilResult.activeProjectCount}</span></div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Project Status Summary */}
      {projectSummary && (
        <div className="card detail-card mt-4">
          <div className="card-header">Project Status Overview</div>
          <div className="card-body">
            <div className="bar-chart">
              {projectSummary.byStatus && Object.entries(projectSummary.byStatus).map(([status, count]) => {
                const colors = { PLANNING: 'blue', IN_PROGRESS: 'green', ON_HOLD: 'amber', COMPLETED: 'purple', CANCELLED: 'red' };
                return (
                  <div key={status} className="bar-row">
                    <span className="bar-label">{status.replace(/_/g, ' ')}</span>
                    <div className="bar-track">
                      <div className={`bar-fill ${colors[status] || 'blue'}`} style={{ width: `${Math.max((count / projectSummary.total) * 100, 12)}%` }}>
                        {count}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
