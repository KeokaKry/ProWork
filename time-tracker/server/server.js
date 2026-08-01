import express from 'express';
import cors from 'cors';
import multer from 'multer';
import { v4 as uuidv4 } from 'uuid';
import Database from 'better-sqlite3';
import path from 'path';
import { fileURLToPath } from 'url';
import XLSX from 'xlsx';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Initialize database
const db = new Database('timetracker.db');

// Create tables
db.exec(`
  CREATE TABLE IF NOT EXISTS employees (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    profession TEXT NOT NULL,
    hourly_rate REAL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS work_sessions (
    id TEXT PRIMARY KEY,
    employee_id TEXT NOT NULL,
    date TEXT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    break_start DATETIME,
    break_end DATETIME,
    lunch_start DATETIME,
    lunch_end DATETIME,
    total_hours REAL DEFAULT 0,
    status TEXT DEFAULT 'active',
    comment TEXT,
    fine_amount REAL DEFAULT 0,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
  );

  CREATE TABLE IF NOT EXISTS work_photos (
    id TEXT PRIMARY KEY,
    session_id TEXT NOT NULL,
    photo_path TEXT NOT NULL,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES work_sessions(id)
  );

  CREATE TABLE IF NOT EXISTS tasks (
    id TEXT PRIMARY KEY,
    employee_id TEXT NOT NULL,
    day_of_week INTEGER NOT NULL,
    description TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
  );
`);

// File upload configuration
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = path.join(__dirname, 'uploads');
    import('fs').then(({ mkdirSync, existsSync }) => {
      if (!existsSync(uploadDir)) {
        mkdirSync(uploadDir, { recursive: true });
      }
      cb(null, uploadDir);
    });
  },
  filename: (req, file, cb) => {
    const uniqueName = `${uuidv4()}-${file.originalname}`;
    cb(null, uniqueName);
  }
});

const upload = multer({ storage });

// ==================== EMPLOYEE ENDPOINTS ====================

// Register employee (for demo purposes)
app.post('/api/employee/register', (req, res) => {
  const { name, profession } = req.body;
  const id = uuidv4();
  
  try {
    const stmt = db.prepare('INSERT INTO employees (id, name, profession) VALUES (?, ?, ?)');
    stmt.run(id, name, profession);
    res.json({ success: true, employee: { id, name, profession } });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Start work day
app.post('/api/employee/start-day', (req, res) => {
  const { employeeId } = req.body;
  const sessionId = uuidv4();
  const today = new Date().toISOString().split('T')[0];
  
  try {
    const stmt = db.prepare(`
      INSERT INTO work_sessions (id, employee_id, date, start_time, status)
      VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'active')
    `);
    stmt.run(sessionId, employeeId, today);
    res.json({ success: true, sessionId });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// End work day with photos
app.post('/api/employee/end-day', upload.array('photos', 10), (req, res) => {
  const { sessionId, comment } = req.body;
  const employeeId = req.body.employeeId;
  
  try {
    // Update session end time
    const updateStmt = db.prepare(`
      UPDATE work_sessions 
      SET end_time = CURRENT_TIMESTAMP, 
          status = 'completed',
          comment = ?
      WHERE id = ?
    `);
    updateStmt.run(comment || null, sessionId);
    
    // Calculate total hours excluding lunch break
    const calcStmt = db.prepare(`
      SELECT 
        (julianday(end_time) - julianday(start_time)) * 24 as gross_hours,
        CASE WHEN lunch_start IS NOT NULL AND lunch_end IS NOT NULL 
          THEN (julianday(lunch_end) - julianday(lunch_start)) * 24 
          ELSE 0 
        END as lunch_hours,
        CASE WHEN break_start IS NOT NULL AND break_end IS NOT NULL 
          THEN (julianday(break_end) - julianday(break_start)) * 24 
          ELSE 0 
        END as break_hours
      FROM work_sessions 
      WHERE id = ?
    `);
    const result = calcStmt.get(sessionId);
    
    if (result && result.gross_hours) {
      const netHours = result.gross_hours - result.lunch_hours - result.break_hours;
      const updateHours = db.prepare('UPDATE work_sessions SET total_hours = ? WHERE id = ?');
      updateHours.run(Math.max(0, netHours), sessionId);
    }
    
    // Save photos
    if (req.files && req.files.length > 0) {
      const photoStmt = db.prepare('INSERT INTO work_photos (id, session_id, photo_path) VALUES (?, ?, ?)');
      req.files.forEach(file => {
        const photoId = uuidv4();
        photoStmt.run(photoId, sessionId, file.path);
      });
    }
    
    res.json({ success: true, message: 'Work day ended successfully' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Mark break start
app.post('/api/employee/break-start', (req, res) => {
  const { sessionId } = req.body;
  
  try {
    const stmt = db.prepare(`
      UPDATE work_sessions SET break_start = CURRENT_TIMESTAMP WHERE id = ?
    `);
    stmt.run(sessionId);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Mark break end
app.post('/api/employee/break-end', (req, res) => {
  const { sessionId } = req.body;
  
  try {
    const stmt = db.prepare(`
      UPDATE work_sessions SET break_end = CURRENT_TIMESTAMP WHERE id = ?
    `);
    stmt.run(sessionId);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Mark lunch start
app.post('/api/employee/lunch-start', (req, res) => {
  const { sessionId } = req.body;
  
  try {
    const stmt = db.prepare(`
      UPDATE work_sessions SET lunch_start = CURRENT_TIMESTAMP WHERE id = ?
    `);
    stmt.run(sessionId);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Mark lunch end
app.post('/api/employee/lunch-end', (req, res) => {
  const { sessionId } = req.body;
  
  try {
    const stmt = db.prepare(`
      UPDATE work_sessions SET lunch_end = CURRENT_TIMESTAMP WHERE id = ?
    `);
    stmt.run(sessionId);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get employee tasks for today
app.get('/api/employee/tasks/:employeeId', (req, res) => {
  const { employeeId } = req.params;
  const dayOfWeek = new Date().getDay(); // 0 = Sunday, 1 = Monday, etc.
  
  try {
    const stmt = db.prepare('SELECT * FROM tasks WHERE employee_id = ? AND day_of_week = ?');
    const tasks = stmt.all(employeeId, dayOfWeek);
    res.json({ success: true, tasks });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get employee's current session
app.get('/api/employee/session/:employeeId', (req, res) => {
  const { employeeId } = req.params;
  const today = new Date().toISOString().split('T')[0];
  
  try {
    const stmt = db.prepare(`
      SELECT * FROM work_sessions 
      WHERE employee_id = ? AND date = ? AND status = 'active'
    `);
    const session = stmt.get(employeeId, today);
    res.json({ success: true, session });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// ==================== ADMIN ENDPOINTS ====================

// Get all employees
app.get('/api/admin/employees', (req, res) => {
  try {
    const stmt = db.prepare('SELECT * FROM employees');
    const employees = stmt.all();
    res.json({ success: true, employees });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Update employee hourly rate
app.put('/api/admin/employee/:id/rate', (req, res) => {
  const { id } = req.params;
  const { hourlyRate } = req.body;
  
  try {
    const stmt = db.prepare('UPDATE employees SET hourly_rate = ? WHERE id = ?');
    stmt.run(hourlyRate, id);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get all work sessions with salary calculation
app.get('/api/admin/sessions', (req, res) => {
  try {
    const stmt = db.prepare(`
      SELECT 
        ws.*,
        e.name as employee_name,
        e.profession,
        e.hourly_rate,
        (ws.total_hours * e.hourly_rate - ws.fine_amount) as calculated_salary
      FROM work_sessions ws
      JOIN employees e ON ws.employee_id = e.id
      ORDER BY ws.date DESC
    `);
    const sessions = stmt.all();
    res.json({ success: true, sessions });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Get photos for a session
app.get('/api/admin/photos/:sessionId', (req, res) => {
  const { sessionId } = req.params;
  
  try {
    const stmt = db.prepare('SELECT * FROM work_photos WHERE session_id = ?');
    const photos = stmt.all(sessionId);
    res.json({ success: true, photos });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Apply fine to a work session
app.post('/api/admin/fine', (req, res) => {
  const { sessionId, amount } = req.body;
  
  try {
    const stmt = db.prepare(`
      UPDATE work_sessions SET fine_amount = fine_amount + ? WHERE id = ?
    `);
    stmt.run(amount, sessionId);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Assign task to employee
app.post('/api/admin/task', (req, res) => {
  const { employeeId, dayOfWeek, description } = req.body;
  const taskId = uuidv4();
  
  try {
    const stmt = db.prepare(`
      INSERT INTO tasks (id, employee_id, day_of_week, description)
      VALUES (?, ?, ?, ?)
    `);
    stmt.run(taskId, employeeId, dayOfWeek, description);
    res.json({ success: true, taskId });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Export timesheet (Excel format)
app.get('/api/admin/export-timesheet/:employeeId?', (req, res) => {
  const { employeeId } = req.params;
  
  try {
    let query;
    let params = [];
    
    if (employeeId) {
      query = `
        SELECT 
          ws.date,
          ws.start_time,
          ws.end_time,
          ws.total_hours,
          e.name as employee_name,
          e.hourly_rate,
          (ws.total_hours * e.hourly_rate - ws.fine_amount) as salary,
          ws.fine_amount,
          ws.comment
        FROM work_sessions ws
        JOIN employees e ON ws.employee_id = e.id
        WHERE ws.employee_id = ?
        ORDER BY ws.date DESC
      `;
      params = [employeeId];
    } else {
      query = `
        SELECT 
          ws.date,
          ws.start_time,
          ws.end_time,
          ws.total_hours,
          e.name as employee_name,
          e.hourly_rate,
          (ws.total_hours * e.hourly_rate - ws.fine_amount) as salary,
          ws.fine_amount,
          ws.comment
        FROM work_sessions ws
        JOIN employees e ON ws.employee_id = e.id
        ORDER BY ws.date DESC
      `;
    }
    
    const stmt = db.prepare(query);
    const data = employeeId ? stmt.get(...params) : stmt.all();
    
    // Convert to Excel
    const rows = Array.isArray(data) ? data : [data];
    const excelData = [
      ['Дата', 'Сотрудник', 'Начало', 'Конец', 'Часы', 'Ставка/час', 'Зарплата', 'Штраф', 'Комментарий']
    ];
    
    rows.forEach(row => {
      excelData.push([
        row.date,
        row.employee_name,
        row.start_time || '',
        row.end_time || '',
        parseFloat(row.total_hours.toFixed(2)),
        row.hourly_rate,
        parseFloat(row.salary.toFixed(2)),
        row.fine_amount,
        row.comment || ''
      ]);
    });
    
    // Add totals row
    if (rows.length > 0) {
      const totalHours = rows.reduce((sum, row) => sum + (parseFloat(row.total_hours) || 0), 0);
      const totalSalary = rows.reduce((sum, row) => sum + (parseFloat(row.salary) || 0), 0);
      const totalFines = rows.reduce((sum, row) => sum + (parseFloat(row.fine_amount) || 0), 0);
      excelData.push(['', '', '', '', 'ИТОГО:', '', totalSalary.toFixed(2), totalFines.toFixed(2), '']);
    }
    
    const worksheet = XLSX.utils.aoa_to_sheet(excelData);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Табель');
    
    // Set column widths
    worksheet['!cols'] = [
      { wch: 12 }, // Date
      { wch: 25 }, // Employee
      { wch: 18 }, // Start
      { wch: 18 }, // End
      { wch: 10 }, // Hours
      { wch: 12 }, // Rate
      { wch: 12 }, // Salary
      { wch: 10 }, // Fine
      { wch: 30 }  // Comment
    ];
    
    const buffer = XLSX.write(workbook, { type: 'buffer', bookType: 'xlsx' });
    
    res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    res.setHeader('Content-Disposition', 'attachment; filename="timesheet.xlsx"');
    res.send(buffer);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Start server
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
