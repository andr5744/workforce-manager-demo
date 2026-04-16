const API_BASE = '/api';

let authCredentials = null;

export function setCredentials(username, password) {
  authCredentials = btoa(`${username}:${password}`);
}

export function clearCredentials() {
  authCredentials = null;
}

async function request(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(authCredentials ? { Authorization: `Basic ${authCredentials}` } : {}),
    ...options.headers
  };

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (res.status === 401) {
    throw new Error('Unauthorized');
  }
  if (res.status === 204) {
    return null;
  }
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || `Request failed: ${res.status}`);
  }
  return res.json();
}

// Employees
export const employees = {
  list: () => request('/employees'),
  get: (id) => request(`/employees/${id}`),
  create: (data) => request('/employees', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/employees/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/employees/${id}`, { method: 'DELETE' }),
  search: (name) => request(`/employees/search?name=${encodeURIComponent(name)}`),
  byDepartment: (deptId) => request(`/employees/department/${deptId}`),
  byStatus: (status) => request(`/employees/status/${status}`),
  updateStatus: (id, status) => request(`/employees/${id}/status?status=${status}`, { method: 'PATCH' }),
  directReports: (id) => request(`/employees/${id}/direct-reports`),
};

// Departments
export const departments = {
  list: () => request('/departments'),
  get: (id) => request(`/departments/${id}`),
  getWithEmployees: (id) => request(`/departments/${id}/employees`),
  create: (data) => request('/departments', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/departments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/departments/${id}`, { method: 'DELETE' }),
  byLocation: (location) => request(`/departments/by-location?location=${encodeURIComponent(location)}`),
};

// Projects
export const projects = {
  list: () => request('/projects'),
  get: (id) => request(`/projects/${id}`),
  create: (data) => request('/projects', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/projects/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/projects/${id}`, { method: 'DELETE' }),
  updateStatus: (id, status) => request(`/projects/${id}/status?status=${status}`, { method: 'PATCH' }),
  recordExpense: (id, amount) => request(`/projects/${id}/expenses?amount=${amount}`, { method: 'POST' }),
  byStatus: (status) => request(`/projects/status/${status}`),
  overdue: () => request('/projects/overdue'),
  byEmployee: (employeeId) => request(`/projects/employee/${employeeId}`),
  assignments: (id) => request(`/projects/${id}/assignments`),
  assign: (id, employeeId, role, hours) =>
    request(`/projects/${id}/assignments?employeeId=${employeeId}&role=${encodeURIComponent(role)}&hoursAllocated=${hours}`, { method: 'POST' }),
  removeAssignment: (projectId, employeeId) =>
    request(`/projects/${projectId}/assignments/${employeeId}`, { method: 'DELETE' }),
};

// Time Entries
export const timeEntries = {
  list: () => request('/time-entries'),
  get: (id) => request(`/time-entries/${id}`),
  create: (employeeId, projectId, data) =>
    request(`/time-entries/employee/${employeeId}/project/${projectId}`, { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => request(`/time-entries/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/time-entries/${id}`, { method: 'DELETE' }),
  byEmployee: (employeeId) => request(`/time-entries/employee/${employeeId}`),
  byProject: (projectId) => request(`/time-entries/project/${projectId}`),
  byDateRange: (start, end) => request(`/time-entries/range?start=${start}&end=${end}`),
  totalHours: (projectId) => request(`/time-entries/project/${projectId}/total-hours`),
  employeeHours: (employeeId, start, end) => request(`/time-entries/employee/${employeeId}/hours?start=${start}&end=${end}`),
};

// Reports
export const reports = {
  headcount: () => request('/reports/headcount'),
  projectSummary: () => request('/reports/project-summary'),
  utilization: (employeeId, start, end) => request(`/reports/employee/${employeeId}/utilization?start=${start}&end=${end}`),
  departmentSummary: (deptId) => request(`/reports/department/${deptId}/summary`),
};
