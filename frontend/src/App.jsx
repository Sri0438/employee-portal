import { useState } from 'react';
import Login from './Login';
import Dashboard from './Dashboard';
import AdminDashboard from './AdminDashboard';
import './App.css';

function App() {
  const [loggedIn, setLoggedIn] = useState(!!localStorage.getItem('token'));
  const role = localStorage.getItem('role');

  if (!loggedIn) return <Login onLogin={() => setLoggedIn(true)} />;
  return role === 'ADMIN'
    ? <AdminDashboard onLogout={() => setLoggedIn(false)} />
    : <Dashboard onLogout={() => setLoggedIn(false)} />;
}
export default App;
