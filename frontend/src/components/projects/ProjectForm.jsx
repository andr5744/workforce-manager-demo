import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { projects } from '../../api/client';

export default function ProjectForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({
    name: '', description: '', startDate: '', endDate: '',
    status: 'PLANNING', budget: '', clientName: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEdit) {
      setLoading(true);
      projects.get(id)
        .then((p) => setForm({
          name: p.name || '',
          description: p.description || '',
          startDate: p.startDate ? p.startDate.substring(0, 10) : '',
          endDate: p.endDate ? p.endDate.substring(0, 10) : '',
          status: p.status || 'PLANNING',
          budget: p.budget || '',
          clientName: p.clientName || ''
        }))
        .catch((e) => setError(e.message))
        .finally(() => setLoading(false));
    }
  }, [id, isEdit]);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    const payload = {
      name: form.name,
      description: form.description || null,
      startDate: form.startDate,
      endDate: form.endDate || null,
      status: form.status,
      budget: parseFloat(form.budget),
      clientName: form.clientName || null
    };
    try {
      if (isEdit) {
        await projects.update(id, payload);
      } else {
        await projects.create(payload);
      }
      navigate('/projects');
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header"><h1>{isEdit ? 'Edit Project' : 'New Project'}</h1></div>
      {error && <div className="error-box">{error}</div>}
      <form className="form-card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-group full-width">
            <label>Project Name *</label>
            <input className="form-control" name="name" value={form.name} onChange={handleChange} required />
          </div>
          <div className="form-group full-width">
            <label>Description</label>
            <textarea className="form-control" name="description" rows="3" value={form.description} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Start Date *</label>
            <input className="form-control" name="startDate" type="date" value={form.startDate} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>End Date</label>
            <input className="form-control" name="endDate" type="date" value={form.endDate} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Status</label>
            <select className="form-control" name="status" value={form.status} onChange={handleChange}>
              <option value="PLANNING">Planning</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="ON_HOLD">On Hold</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <div className="form-group">
            <label>Budget *</label>
            <input className="form-control" name="budget" type="number" step="0.01" min="0" value={form.budget} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Client Name</label>
            <input className="form-control" name="clientName" value={form.clientName} onChange={handleChange} />
          </div>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">{isEdit ? 'Update' : 'Create'} Project</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/projects')}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
