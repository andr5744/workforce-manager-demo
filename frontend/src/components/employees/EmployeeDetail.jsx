import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { employees, projects } from '../../api/client';

export default function EmployeeDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [emp, setEmp] = useState(null);
  const [directReports, setDirectReports] = useState([]);
  const [empProjects, setEmpProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        const [empData, reports, projs] = await Promise.all([
          employees.get(id),
          employees.directReports(id).catch(() => []),
          projects.byEmployee(id).catch(() => []),
        ]);
        setEmp(empData);
        setDirectReports(reports);
        setEmpProjects(projs);
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id]);

  if (loading) return <div className="loading"><div className="spinner" /></div>;
  if (error) return <div className="error-box">{error}</div>;
  if (!emp) return <div className="page-message">Employee not found</div>;

  const fmt = (n) => n != null ? `$${Number(n).toLocaleString()}` : '—';

  return (
    <div>
      <div className="page-header">
        <h1>{emp.firstName} {emp.lastName}</h1>
        <div className="btn-group">
          <Link to={`/employees/${id}/edit`} className="btn btn-primary">Edit</Link>
          <button className="btn btn-secondary" onClick={() => navigate('/employees')}>Back</button>
        </div>
      </div>

      <div className="detail-grid">
        <div className="card detail-card">
          <div className="card-header">Personal Information</div>
          <div className="card-body">
            <div className="detail-row"><span className="detail-label">Email</span><span className="detail-value">{emp.email}</span></div>
            <div className="detail-row"><span className="detail-label">Phone</span><span className="detail-value">{emp.phoneNumber || '—'}</span></div>
            <div className="detail-row"><span className="detail-label">Job Title</span><span className="detail-value">{emp.jobTitle || '—'}</span></div>
            <div className="detail-row"><span className="detail-label">Status</span><span className="detail-value"><span className={`badge badge-${emp.status?.toLowerCase()}`}>{emp.status}</span></span></div>
          </div>
        </div>

        <div className="card detail-card">
          <div className="card-header">Employment Details</div>
          <div className="card-body">
            <div className="detail-row"><span className="detail-label">Department</span><span className="detail-value">{emp.department?.name || '—'}</span></div>
            <div className="detail-row"><span className="detail-label">Hire Date</span><span className="detail-value">{emp.hireDate ? new Date(emp.hireDate).toLocaleDateString() : '—'}</span></div>
            <div className="detail-row"><span className="detail-label">Salary</span><span className="detail-value">{fmt(emp.salary)}</span></div>
            <div className="detail-row"><span className="detail-label">Manager</span><span className="detail-value">{emp.manager ? <Link to={`/employees/${emp.manager.id}`}>{emp.manager.firstName} {emp.manager.lastName}</Link> : '—'}</span></div>
          </div>
        </div>
      </div>

      {directReports.length > 0 && (
        <div className="card detail-card">
          <div className="card-header">Direct Reports ({directReports.length})</div>
          <div className="card-body" style={{ padding: 0 }}>
            <table>
              <thead><tr><th>Name</th><th>Job Title</th><th>Status</th></tr></thead>
              <tbody>
                {directReports.map((r) => (
                  <tr key={r.id}>
                    <td><Link to={`/employees/${r.id}`}>{r.firstName} {r.lastName}</Link></td>
                    <td>{r.jobTitle || '—'}</td>
                    <td><span className={`badge badge-${r.status?.toLowerCase()}`}>{r.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {empProjects.length > 0 && (
        <div className="card detail-card">
          <div className="card-header">Assigned Projects ({empProjects.length})</div>
          <div className="card-body" style={{ padding: 0 }}>
            <table>
              <thead><tr><th>Project</th><th>Client</th><th>Status</th></tr></thead>
              <tbody>
                {empProjects.map((p) => (
                  <tr key={p.id}>
                    <td><Link to={`/projects/${p.id}`}>{p.name}</Link></td>
                    <td>{p.clientName || '—'}</td>
                    <td><span className={`badge badge-${p.status?.toLowerCase()}`}>{p.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
