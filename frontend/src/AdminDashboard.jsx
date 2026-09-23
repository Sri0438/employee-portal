import { useEffect, useState } from 'react';
import { apiRequest } from './api';

export default function AdminDashboard({ onLogout }) {
  const fullName = localStorage.getItem('fullName');
  const [employees, setEmployees] = useState([]);
  const [attendance, setAttendance] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [newEmp, setNewEmp] = useState({ fullName: '', email: '', password: '' });
  const [newTask, setNewTask] = useState({ title: '', description: '', assignedToId: '', dueDate: '', priority: 'MEDIUM' });

  async function loadData() {
    setLoading(true);
    try {
      const [emps, att] = await Promise.all([
        apiRequest('/api/employees'),
        apiRequest('/api/attendance/all-today'),
      ]);
      setEmployees(emps);
      setAttendance(att);
    } catch (err) { setMessage(err.message); }
    finally { setLoading(false); }
  }

  useEffect(() => { loadData(); }, []);

  async function handleAddEmployee(e) {
    e.preventDefault();
    setMessage('');
    try {
      await apiRequest('/api/employees', { method: 'POST', body: JSON.stringify(newEmp) });
      setNewEmp({ fullName: '', email: '', password: '' });
      loadData();
    } catch (err) { setMessage(err.message); }
  }

  async function handleToggleActive(emp) {
    try {
      await apiRequest(`/api/employees/${emp.id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ active: !emp.active }),
      });
      loadData();
    } catch (err) { setMessage(err.message); }
  }

  async function handleAssignTask(e) {
    e.preventDefault();
    setMessage('');
    try {
      await apiRequest('/api/tasks', {
        method: 'POST',
        body: JSON.stringify({ ...newTask, assignedToId: Number(newTask.assignedToId), dueDate: newTask.dueDate || null }),
      });
      setNewTask({ title: '', description: '', assignedToId: '', dueDate: '', priority: 'MEDIUM' });
      setMessage('Task assigned.');
    } catch (err) { setMessage(err.message); }
  }

  function handleLogout() { localStorage.clear(); onLogout(); }

  if (loading) return <p className="loading">Loading...</p>;

  return (
    <div className="dashboard">
      <header>
        <h1>Admin Dashboard — {fullName}</h1>
        <button className="logout" onClick={handleLogout}>Log Out</button>
      </header>

      {message && <p className="error">{message}</p>}

      <section className="card">
        <h2>Employees ({employees.length})</h2>
        <table>
          <thead><tr><th>Name</th><th>Email</th><th>Active</th><th></th></tr></thead>
          <tbody>
            {employees.map((e) => (
              <tr key={e.id}>
                <td>{e.fullName}</td><td>{e.email}</td><td>{e.active ? 'Yes' : 'No'}</td>
                <td><button onClick={() => handleToggleActive(e)}>{e.active ? 'Deactivate' : 'Activate'}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
        <h3>Add Employee</h3>
        <form onSubmit={handleAddEmployee} className="inline-form">
          <input placeholder="Full name" value={newEmp.fullName} onChange={(e) => setNewEmp({ ...newEmp, fullName: e.target.value })} required />
          <input placeholder="Email" type="email" value={newEmp.email} onChange={(e) => setNewEmp({ ...newEmp, email: e.target.value })} required />
          <input placeholder="Temp password" type="text" value={newEmp.password} onChange={(e) => setNewEmp({ ...newEmp, password: e.target.value })} required />
          <button type="submit">Add</button>
        </form>
      </section>

      <section className="card">
        <h2>Assign a Task</h2>
        <form onSubmit={handleAssignTask} className="inline-form">
          <input placeholder="Title" value={newTask.title} onChange={(e) => setNewTask({ ...newTask, title: e.target.value })} required />
          <select value={newTask.assignedToId} onChange={(e) => setNewTask({ ...newTask, assignedToId: e.target.value })} required>
            <option value="">Assign to...</option>
            {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
          </select>
          <input type="date" value={newTask.dueDate} onChange={(e) => setNewTask({ ...newTask, dueDate: e.target.value })} />
          <select value={newTask.priority} onChange={(e) => setNewTask({ ...newTask, priority: e.target.value })}>
            <option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option>
          </select>
          <button type="submit">Assign</button>
        </form>
      </section>

      <section className="card">
        <h2>Today's Attendance</h2>
        {attendance.length === 0 ? <p>No one has clocked in today.</p> : (
          <table>
            <thead><tr><th>Employee</th><th>Clock In</th><th>Clock Out</th></tr></thead>
            <tbody>
              {attendance.map((a) => (
                <tr key={a.id}><td>{a.user.fullName}</td><td>{a.clockInTime}</td><td>{a.clockOutTime || '—'}</td></tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
