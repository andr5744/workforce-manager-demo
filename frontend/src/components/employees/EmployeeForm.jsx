import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { employees, departments as deptApi } from '../../api/client';

export default function EmployeeForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phoneNumber: '',
    hireDate: '', salary: '', jobTitle: '', status: 'ACTIVE',
    departmentId: '', managerId: ''
  });
  const [depts, setDepts] = useState([]);
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        const [deptsData, empsData] = await Promise.all([
          deptApi.list(),
          employees.list()
        ]);
        setDepts(deptsData);
        setManagers(empsData);

        if (isEdit) {
          const emp = await employees.get(id);
          setForm({
            firstName: emp.firstName || '',
            lastName: emp.lastName || '',
            email: emp.email || '',
            phoneNumber: emp.phoneNumber || '',
            hireDate: emp.hireDate ? emp.hireDate.substring(0, 10) : '',
            salary: emp.salary || '',
            jobTitle: emp.jobTitle || '',
            status: emp.status || 'ACTIVE',
            departmentId: emp.department?.id || '',
            managerId: emp.manager?.id || ''
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
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      phoneNumber: form.phoneNumber || null,
      hireDate: form.hireDate,
      salary: parseFloat(form.salary),
      jobTitle: form.jobTitle || null,
      status: form.status,
      department: form.departmentId ? { id: parseInt(form.departmentId) } : null,
      manager: form.managerId ? { id: parseInt(form.managerId) } : null
    };

    try {
      if (isEdit) {
        await employees.update(id, payload);
      } else {
        await employees.create(payload);
      }
      navigate('/employees');
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header">
        <h1>{isEdit ? 'Edit Employee' : 'Add Employee'}</h1>
      </div>

      {error && <div className="error-box">{error}</div>}

      <form className="form-card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-group">
            <label>First Name *</label>
            <input className="form-control" name="firstName" value={form.firstName} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Last Name *</label>
            <input className="form-control" name="lastName" value={form.lastName} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Email *</label>
            <input className="form-control" name="email" type="email" value={form.email} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Phone</label>
            <input className="form-control" name="phoneNumber" value={form.phoneNumber} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Hire Date *</label>
            <input className="form-control" name="hireDate" type="date" value={form.hireDate} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Salary *</label>
            <input className="form-control" name="salary" type="number" step="0.01" min="0" value={form.salary} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Job Title</label>
            <input className="form-control" name="jobTitle" value={form.jobTitle} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Status</label>
            <select className="form-control" name="status" value={form.status} onChange={handleChange}>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="ON_LEAVE">On Leave</option>
              <option value="TERMINATED">Terminated</option>
            </select>
          </div>
          <div className="form-group">
            <label>Department</label>
            <select className="form-control" name="departmentId" value={form.departmentId} onChange={handleChange}>
              <option value="">— None —</option>
              {depts.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Manager</label>
            <select className="form-control" name="managerId" value={form.managerId} onChange={handleChange}>
              <option value="">— None —</option>
              {managers.filter(m => m.id !== parseInt(id)).map((m) => (
                <option key={m.id} value={m.id}>{m.firstName} {m.lastName}</option>
              ))}
            </select>
          </div>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">{isEdit ? 'Update' : 'Create'} Employee</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/employees')}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
