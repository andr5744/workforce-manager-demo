import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import EmployeeList from './components/employees/EmployeeList';
import EmployeeForm from './components/employees/EmployeeForm';
import EmployeeDetail from './components/employees/EmployeeDetail';
import DepartmentList from './components/departments/DepartmentList';
import DepartmentForm from './components/departments/DepartmentForm';
import ProjectList from './components/projects/ProjectList';
import ProjectForm from './components/projects/ProjectForm';
import ProjectDetail from './components/projects/ProjectDetail';
import TimeEntryList from './components/timeentries/TimeEntryList';
import TimeEntryForm from './components/timeentries/TimeEntryForm';
import Reports from './components/reports/Reports';

function ProtectedRoute({ children, roles }) {
  const { user, hasRole } = useAuth();
  if (!user) return <Navigate to="/login" />;
  if (roles && !hasRole(...roles)) return <div className="page-message">Access denied</div>;
  return children;
}

export default function App() {
  const { user } = useAuth();

  if (!user) {
    return <Routes>
      <Route path="*" element={<Login />} />
    </Routes>;
  }

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/employees" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><EmployeeList /></ProtectedRoute>} />
        <Route path="/employees/new" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><EmployeeForm /></ProtectedRoute>} />
        <Route path="/employees/:id/edit" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><EmployeeForm /></ProtectedRoute>} />
        <Route path="/employees/:id" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><EmployeeDetail /></ProtectedRoute>} />
        <Route path="/departments" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><DepartmentList /></ProtectedRoute>} />
        <Route path="/departments/new" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><DepartmentForm /></ProtectedRoute>} />
        <Route path="/departments/:id/edit" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><DepartmentForm /></ProtectedRoute>} />
        <Route path="/projects" element={<ProtectedRoute><ProjectList /></ProtectedRoute>} />
        <Route path="/projects/new" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><ProjectForm /></ProtectedRoute>} />
        <Route path="/projects/:id/edit" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><ProjectForm /></ProtectedRoute>} />
        <Route path="/projects/:id" element={<ProtectedRoute><ProjectDetail /></ProtectedRoute>} />
        <Route path="/time-entries" element={<ProtectedRoute><TimeEntryList /></ProtectedRoute>} />
        <Route path="/time-entries/new" element={<ProtectedRoute><TimeEntryForm /></ProtectedRoute>} />
        <Route path="/time-entries/:id/edit" element={<ProtectedRoute><TimeEntryForm /></ProtectedRoute>} />
        <Route path="/reports" element={<ProtectedRoute roles={['ADMIN', 'MANAGER']}><Reports /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Layout>
  );
}
