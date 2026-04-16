import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { login, USERS } = useAuth();

  const roleDetails = {
    ADMIN: { icon: '🛡️', desc: 'Full access to all features' },
    MANAGER: { icon: '📋', desc: 'Manage employees, projects & reports' },
    EMPLOYEE: { icon: '👤', desc: 'View projects & log time entries' },
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>👥 Workforce Manager</h1>
        <p className="subtitle">Employee & Project Management System</p>
        <p className="login-label">Sign in as:</p>
        <div className="login-buttons">
          {USERS.map((u) => (
            <button
              key={u.username}
              className="login-btn"
              onClick={() => login(u.username, u.password)}
            >
              <span className="role-icon">{roleDetails[u.role].icon}</span>
              <div className="role-info">
                <div className="role-name">{u.label}</div>
                <div className="role-desc">{roleDetails[u.role].desc}</div>
              </div>
              <span style={{ color: '#94a3b8' }}>→</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
