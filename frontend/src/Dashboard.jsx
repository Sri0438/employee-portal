import { useEffect, useState } from 'react';
import { apiRequest } from './api';

export default function Dashboard({ onLogout }) {
  const fullName = localStorage.getItem('fullName');
  const [today, setToday] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);

  async function loadData() {
    setLoading(true);
    try {
      const [todayData, taskData] = await Promise.all([
        apiRequest('/api/attendance/today'),
        apiRequest('/api/tasks/mine'),
      ]);
      setToday(todayData);
      setTasks(taskData);
    } catch (err) { setMessage(err.message); }
    finally { setLoading(false); }
  }

  useEffect(() => { loadData(); }, []);

  async function handleClockIn() {
    setMessage('');
    try { setToday(await apiRequest('/api/attendance/clock-in', { method: 'POST' })); }
    catch (err) { setMessage(err.message); }
  }
  async function handleClockOut() {
    setMessage('');
    try { setToday(await apiRequest('/api/attendance/clock-out', { method: 'POST' })); }
    catch (err) { setMessage(err.message); }
  }
  async function updateTaskStatus(taskId, status) {
    try {
      const updated = await apiRequest(`/api/tasks/${taskId}/progress`, {
        method: 'PATCH',
        body: JSON.stringify({ status, progressPercent: status === 'COMPLETED' ? 100 : 50 }),
      });
      setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
    } catch (err) { setMessage(err.message); }
  }
  function handleLogout() { localStorage.clear(); onLogout(); }

  if (loading) return <p className="loading">Loading...</p>;

  const isClockedIn = today && !today.clockOutTime;
  const isDone = today && today.clockOutTime;

  return (
    <div className="dashboard">
      <header>
        <h1>Welcome, {fullName}</h1>
        <button className="logout" onClick={handleLogout}>Log Out</button>
      </header>
      <section className="card">
        <h2>Today's Attendance</h2>
        {message && <p className="error">{message}</p>}
        <p>Status: {isDone ? 'Completed for today' : isClockedIn ? 'Clocked In' : 'Not clocked in'}</p>
        <div className="buttons">
          <button onClick={handleClockIn} disabled={isClockedIn || isDone}>Clock In</button>
          <button onClick={handleClockOut} disabled={!isClockedIn}>Clock Out</button>
        </div>
      </section>
      <section className="card">
        <h2>My Tasks</h2>
        {tasks.length === 0 ? <p>No tasks assigned yet.</p> : (
          <table>
            <thead><tr><th>Title</th><th>Priority</th><th>Due</th><th>Status</th><th>Progress</th><th>Actions</th></tr></thead>
            <tbody>
              {tasks.map((t) => (
                <tr key={t.id}>
                  <td>{t.title}</td><td>{t.priority}</td><td>{t.dueDate}</td><td>{t.status}</td><td>{t.progressPercent}%</td>
                  <td>
                    {t.status !== 'IN_PROGRESS' && t.status !== 'COMPLETED' && (
                      <button onClick={() => updateTaskStatus(t.id, 'IN_PROGRESS')}>Start</button>
                    )}
                    {t.status === 'IN_PROGRESS' && (
                      <button onClick={() => updateTaskStatus(t.id, 'COMPLETED')}>Complete</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
