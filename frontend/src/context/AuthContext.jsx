import { createContext, useContext, useState, useCallback } from 'react';
import { setCredentials, clearCredentials } from '../api/client';

const AuthContext = createContext(null);

const USERS = [
  { username: 'admin', password: 'admin123', role: 'ADMIN', label: 'Admin' },
  { username: 'manager', password: 'manager123', role: 'MANAGER', label: 'Manager' },
  { username: 'employee', password: 'employee123', role: 'EMPLOYEE', label: 'Employee' },
];

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  const login = useCallback((username, password) => {
    const found = USERS.find((u) => u.username === username && u.password === password);
    if (!found) throw new Error('Invalid credentials');
    setCredentials(username, password);
    setUser(found);
  }, []);

  const logout = useCallback(() => {
    clearCredentials();
    setUser(null);
  }, []);

  const hasRole = useCallback(
    (...roles) => user && roles.includes(user.role),
    [user]
  );

  return (
    <AuthContext.Provider value={{ user, login, logout, hasRole, USERS }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
