import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { departments } from '../../api/client';

export default function DepartmentForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({ name: '', location: '', budget: '', headCountLimit: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEdit) {
      setLoading(true);
      departments.get(id)
        .then((d) => setForm({
          name: d.name || '',
          location: d.location || '',
          budget: d.budget || '',
          headCountLimit: d.headCountLimit ?? ''
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
      location: form.location || null,
      budget: parseFloat(form.budget),
      headCountLimit: form.headCountLimit ? parseInt(form.headCountLimit) : null
    };
    try {
      if (isEdit) {
        await departments.update(id, payload);
      } else {
        await departments.create(payload);
      }
      navigate('/departments');
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header"><h1>{isEdit ? 'Edit Department' : 'Add Department'}</h1></div>
      {error && <div className="error-box">{error}</div>}
      <form className="form-card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-group">
            <label>Name *</label>
            <input className="form-control" name="name" value={form.name} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Location</label>
            <input className="form-control" name="location" value={form.location} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Budget *</label>
            <input className="form-control" name="budget" type="number" step="0.01" min="0" value={form.budget} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Head Count Limit</label>
            <input className="form-control" name="headCountLimit" type="number" min="0" value={form.headCountLimit} onChange={handleChange} />
          </div>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">{isEdit ? 'Update' : 'Create'} Department</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/departments')}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
