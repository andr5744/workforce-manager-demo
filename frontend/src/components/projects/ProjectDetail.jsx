import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { projects, employees as empApi, timeEntries } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

export default function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const [project, setProject] = useState(null);
  const [assignments, setAssignments] = useState([]);
  const [entries, setEntries] = useState([]);
  const [totalHours, setTotalHours] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Assignment modal
  const [showAssign, setShowAssign] = useState(false);
  const [allEmployees, setAllEmployees] = useState([]);
  const [assignForm, setAssignForm] = useState({ employeeId: '', role: '', hoursAllocated: '' });

  // Expense modal
  const [showExpense, setShowExpense] = useState(false);
  const [expenseAmount, setExpenseAmount] = useState('');

  useEffect(() => { load(); }, [id]);

  async function load() {
    try {
      const [proj, assigns, ents, hours] = await Promise.all([
        projects.get(id),
        projects.assignments(id).catch(() => []),
        timeEntries.byProject(id).catch(() => []),
        timeEntries.totalHours(id).catch(() => 0),
      ]);
      setProject(proj);
      setAssignments(assigns);
      setEntries(ents);
      setTotalHours(hours);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function openAssignModal() {
    try {
      const emps = await empApi.list();
      setAllEmployees(emps);
      setShowAssign(true);
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleAssign(e) {
    e.preventDefault();
    try {
      await projects.assign(id, assignForm.employeeId, assignForm.role, assignForm.hoursAllocated);
      setShowAssign(false);
      setAssignForm({ employeeId: '', role: '', hoursAllocated: '' });
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleRemoveAssignment(employeeId) {
    if (!window.confirm('Remove this assignment?')) return;
    try {
      await projects.removeAssignment(id, employeeId);
      load();
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleRecordExpense(e) {
    e.preventDefault();
    try {
      await projects.recordExpense(id, expenseAmount);
      setShowExpense(false);
      setExpenseAmount('');
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleStatusChange(status) {
    try {
      await projects.updateStatus(id, status);
      load();
    } catch (e) {
      setError(e.message);
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>;
  if (error && !project) return <div className="error-box">{error}</div>;
  if (!project) return <div className="page-message">Project not found</div>;

  const fmt = (n) => n != null ? `$${Number(n).toLocaleString()}` : '—';
  const pct = project.budget > 0 ? (project.budgetSpent / project.budget) * 100 : 0;
  const remaining = project.budget - project.budgetSpent;

  return (
    <div>
      <div className="page-header">
        <h1>{project.name}</h1>
        <div className="btn-group">
          {hasRole('ADMIN', 'MANAGER') && <Link to={`/projects/${id}/edit`} className="btn btn-primary">Edit</Link>}
          <button className="btn btn-secondary" onClick={() => navigate('/projects')}>Back</button>
        </div>
      </div>

      {error && <div className="error-box">{error}</div>}

      <div className="detail-grid">
        <div className="card detail-card">
          <div className="card-header">Project Details</div>
          <div className="card-body">
            <div className="detail-row"><span className="detail-label">Client</span><span className="detail-value">{project.clientName || '—'}</span></div>
            <div className="detail-row"><span className="detail-label">Status</span><span className="detail-value"><span className={`badge badge-${project.status?.toLowerCase()}`}>{project.status?.replace(/_/g, ' ')}</span></span></div>
            <div className="detail-row"><span className="detail-label">Start Date</span><span className="detail-value">{project.startDate ? new Date(project.startDate).toLocaleDateString() : '—'}</span></div>
            <div className="detail-row"><span className="detail-label">End Date</span><span className="detail-value">{project.endDate ? new Date(project.endDate).toLocaleDateString() : '—'}</span></div>
            <div className="detail-row"><span className="detail-label">Description</span><span className="detail-value">{project.description || '—'}</span></div>
            {hasRole('ADMIN', 'MANAGER') && (
              <div className="detail-row">
                <span className="detail-label">Change Status</span>
                <div className="btn-group">
                  {['PLANNING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED']
                    .filter(s => s !== project.status)
                    .map(s => (
                      <button key={s} className="btn btn-sm btn-secondary" onClick={() => handleStatusChange(s)}>{s.replace(/_/g, ' ')}</button>
                    ))}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="card detail-card">
          <div className="card-header">
            <span>Budget</span>
            {hasRole('ADMIN', 'MANAGER') && <button className="btn btn-sm btn-primary" onClick={() => setShowExpense(true)}>+ Expense</button>}
          </div>
          <div className="card-body">
            <div className="detail-row"><span className="detail-label">Total Budget</span><span className="detail-value">{fmt(project.budget)}</span></div>
            <div className="detail-row"><span className="detail-label">Spent</span><span className="detail-value">{fmt(project.budgetSpent)}</span></div>
            <div className="detail-row"><span className="detail-label">Remaining</span><span className="detail-value" style={{ color: remaining < 0 ? 'var(--danger)' : 'var(--success)' }}>{fmt(remaining)}</span></div>
            <div className="detail-row">
              <span className="detail-label">Usage</span>
              <div style={{ flex: 1 }}>
                <div className="text-sm">{pct.toFixed(1)}%</div>
                <div className="budget-bar" style={{ height: 12 }}>
                  <div className={`budget-fill ${pct < 60 ? 'green' : pct < 85 ? 'amber' : 'red'}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                </div>
              </div>
            </div>
            <div className="detail-row"><span className="detail-label">Hours Logged</span><span className="detail-value">{totalHours} hours</span></div>
          </div>
        </div>
      </div>

      {/* Assignments */}
      <div className="card detail-card">
        <div className="card-header">
          <span>Team Assignments ({assignments.length})</span>
          {hasRole('ADMIN', 'MANAGER') && <button className="btn btn-sm btn-primary" onClick={openAssignModal}>+ Assign</button>}
        </div>
        <div className="card-body" style={{ padding: 0 }}>
          {assignments.length === 0 ? (
            <div className="empty-state"><p>No team members assigned</p></div>
          ) : (
            <table>
              <thead><tr><th>Employee</th><th>Role</th><th>Hours Allocated</th><th>Assigned Date</th>{hasRole('ADMIN', 'MANAGER') && <th>Actions</th>}</tr></thead>
              <tbody>
                {assignments.map((a) => (
                  <tr key={a.id}>
                    <td>{a.employee ? <Link to={`/employees/${a.employee.id}`}>{a.employee.firstName} {a.employee.lastName}</Link> : '—'}</td>
                    <td>{a.role}</td>
                    <td>{a.hoursAllocated}h</td>
                    <td>{a.assignedDate ? new Date(a.assignedDate).toLocaleDateString() : '—'}</td>
                    {hasRole('ADMIN', 'MANAGER') && (
                      <td><button className="btn btn-sm btn-danger" onClick={() => handleRemoveAssignment(a.employee?.id)}>Remove</button></td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Recent Time Entries */}
      <div className="card detail-card">
        <div className="card-header">Recent Time Entries ({entries.length})</div>
        <div className="card-body" style={{ padding: 0 }}>
          {entries.length === 0 ? (
            <div className="empty-state"><p>No time entries</p></div>
          ) : (
            <table>
              <thead><tr><th>Employee</th><th>Date</th><th>Hours</th><th>Description</th><th>Billable</th></tr></thead>
              <tbody>
                {entries.slice(0, 10).map((te) => (
                  <tr key={te.id}>
                    <td>{te.employee ? `${te.employee.firstName} ${te.employee.lastName}` : '—'}</td>
                    <td>{te.date ? new Date(te.date).toLocaleDateString() : '—'}</td>
                    <td>{te.hoursWorked}h</td>
                    <td className="text-sm">{te.description || '—'}</td>
                    <td><span className={`badge badge-${te.billableStatus?.toLowerCase()}`}>{te.billableStatus}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Assignment Modal */}
      {showAssign && (
        <div className="modal-overlay" onClick={() => setShowAssign(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Assign Employee</h2>
            <form onSubmit={handleAssign}>
              <div className="form-group mb-4">
                <label>Employee *</label>
                <select className="form-control" value={assignForm.employeeId} onChange={(e) => setAssignForm(f => ({ ...f, employeeId: e.target.value }))} required>
                  <option value="">Select employee...</option>
                  {allEmployees.map((emp) => <option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName}</option>)}
                </select>
              </div>
              <div className="form-group mb-4">
                <label>Role *</label>
                <input className="form-control" value={assignForm.role} onChange={(e) => setAssignForm(f => ({ ...f, role: e.target.value }))} required placeholder="e.g. Developer, Lead, QA" />
              </div>
              <div className="form-group mb-4">
                <label>Hours Allocated *</label>
                <input className="form-control" type="number" min="1" value={assignForm.hoursAllocated} onChange={(e) => setAssignForm(f => ({ ...f, hoursAllocated: e.target.value }))} required />
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">Assign</button>
                <button type="button" className="btn btn-secondary" onClick={() => setShowAssign(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Expense Modal */}
      {showExpense && (
        <div className="modal-overlay" onClick={() => setShowExpense(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Record Expense</h2>
            <form onSubmit={handleRecordExpense}>
              <div className="form-group mb-4">
                <label>Amount ($) *</label>
                <input className="form-control" type="number" step="0.01" min="0.01" value={expenseAmount} onChange={(e) => setExpenseAmount(e.target.value)} required />
              </div>
              <div className="text-sm text-muted mb-4">Remaining budget: {fmt(remaining)}</div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">Record</button>
                <button type="button" className="btn btn-secondary" onClick={() => setShowExpense(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
