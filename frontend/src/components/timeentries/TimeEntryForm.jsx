import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { timeEntries, employees as empApi, projects as projApi } from '../../api/client';

export default function TimeEntryForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({
    employeeId: '', projectId: '', date: '', hoursWorked: '',
    description: '', billableStatus: 'BILLABLE'
  });
  const [emps, setEmps] = useState([]);
  const [projs, setProjs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        const [empList, projList] = await Promise.all([empApi.list(), projApi.list()]);
        setEmps(empList);
        setProjs(projList);

        if (isEdit) {
          const te = await timeEntries.get(id);
          setForm({
            employeeId: te.employee?.id || '',
            projectId: te.project?.id || '',
            date: te.date ? te.date.substring(0, 10) : '',
            hoursWorked: te.hoursWorked || '',
            description: te.description || '',
            billableStatus: te.billableStatus || 'BILLABLE'
          });
        }
      } catch (e) {
        setError(e.message);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id, isEdit]);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    const payload = {
      date: form.date,
      hoursWorked: parseInt(form.hoursWorked),
      description: form.description || null,
      billableStatus: form.billableStatus
    };

    try {
      if (isEdit) {
        await timeEntries.update(id, payload);
      } else {
        await timeEntries.create(form.employeeId, form.projectId, payload);
      }
      navigate('/time-entries');
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header"><h1>{isEdit ? 'Edit Time Entry' : 'Log Time'}</h1></div>
      {error && <div className="error-box">{error}</div>}
      <form className="form-card" onSubmit={handleSubmit}>
        <div className="form-grid">
          {!isEdit && (
            <>
              <div className="form-group">
                <label>Employee *</label>
                <select className="form-control" name="employeeId" value={form.employeeId} onChange={handleChange} required>
                  <option value="">Select employee...</option>
                  {emps.map((emp) => <option key={emp.id} value={emp.id}>{emp.firstName} {emp.lastName}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Project *</label>
                <select className="form-control" name="projectId" value={form.projectId} onChange={handleChange} required>
                  <option value="">Select project...</option>
                  {projs.filter(p => p.status !== 'COMPLETED' && p.status !== 'CANCELLED').map((p) => (
                    <option key={p.id} value={p.id}>{p.name}</option>
                  ))}
                </select>
              </div>
            </>
          )}
          <div className="form-group">
            <label>Date *</label>
            <input className="form-control" name="date" type="date" value={form.date} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Hours Worked * (1-24)</label>
            <input className="form-control" name="hoursWorked" type="number" min="1" max="24" value={form.hoursWorked} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Billable Status</label>
            <select className="form-control" name="billableStatus" value={form.billableStatus} onChange={handleChange}>
              <option value="BILLABLE">Billable</option>
              <option value="NON_BILLABLE">Non-Billable</option>
              <option value="INTERNAL">Internal</option>
            </select>
          </div>
          <div className="form-group full-width">
            <label>Description</label>
            <textarea className="form-control" name="description" rows="3" value={form.description} onChange={handleChange} placeholder="What did you work on?" />
          </div>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">{isEdit ? 'Update' : 'Log'} Time Entry</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/time-entries')}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
