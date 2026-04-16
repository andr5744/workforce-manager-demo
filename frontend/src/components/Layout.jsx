import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout({ children }) {
  const { user, logout, hasRole } = useAuth();

  const navItems = [
    { to: '/', icon: '📊', label: 'Dashboard', section: 'Overview' },
    { to: '/employees', icon: '👤', label: 'Employees', section: 'Management', roles: ['ADMIN', 'MANAGER'] },
    { to: '/departments', icon: '🏢', label: 'Departments', section: 'Management', roles: ['ADMIN', 'MANAGER'] },
    { to: '/projects', icon: '📁', label: 'Projects', section: 'Work' },
    { to: '/time-entries', icon: '⏱️', label: 'Time Entries', section: 'Work' },
    { to: '/reports', icon: '📈', label: 'Reports', section: 'Analytics', roles: ['ADMIN', 'MANAGER'] },
  ];

  const sections = [...new Set(navItems.map((n) => n.section))];

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">👥</span>
          <h2>Workforce Mgr</h2>
        </div>

        <nav className="sidebar-nav">
          {sections.map((section) => (
            <div key={section} className="nav-section">
              <div className="nav-section-title">{section}</div>
              {navItems
                .filter((n) => n.section === section)
                .filter((n) => !n.roles || hasRole(...n.roles))
                .map((n) => (
                  <NavLink
                    key={n.to}
                    to={n.to}
                    end={n.to === '/'}
                    className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
                  >
                    <span className="nav-icon">{n.icon}</span>
                    <span>{n.label}</span>
                  </NavLink>
                ))}
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{user.label[0]}</div>
            <div>
              <div className="user-name">{user.label}</div>
              <div className="user-role">{user.role}</div>
            </div>
          </div>
          <button className="logout-btn" onClick={logout}>Sign Out</button>
        </div>
      </aside>

      <main className="main-content">{children}</main>
    </div>
  );
}
