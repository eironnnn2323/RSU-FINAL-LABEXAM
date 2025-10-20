import React, { useState } from 'axios';
import axios from 'axios';
import './RegistrationForm.css';

const API_URL = 'http://localhost:8080/api/v1/registrations';

function RegistrationForm() {
  const [formData, setFormData] = useState({
    studentName: '',
    studentId: '',
    email: '',
    program: '',
    yearLevel: ''
  });

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState(''); // 'success' or 'error'
  const [registrationId, setRegistrationId] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      console.log('Submitting registration:', formData);

      const response = await axios.post(`${API_URL}/submit`, formData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('Response:', response.data);

      setMessageType('success');
      setMessage(`✓ ${response.data.message}`);
      setRegistrationId(response.data.registrationId);

      // Reset form
      setFormData({
        studentName: '',
        studentId: '',
        email: '',
        program: '',
        yearLevel: ''
      });

      // Scroll to message
      setTimeout(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }, 300);

    } catch (error) {
      console.error('Error submitting registration:', error);

      setMessageType('error');
      if (error.response?.data?.message) {
        setMessage(`✗ ${error.response.data.message}`);
      } else if (error.response?.data?.errors) {
        const errorMessages = Object.values(error.response.data.errors).join(', ');
        setMessage(`✗ Validation error: ${errorMessages}`);
      } else {
        setMessage(`✗ Failed to submit registration: ${error.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="registration-form-container">
      <div className="form-card">
        <h1>Student Registration Form</h1>
        <p className="form-subtitle">Rejoice State University</p>

        {message && (
          <div className={`message ${messageType}`}>
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="registration-form">
          <div className="form-group">
            <label htmlFor="studentName">Full Name *</label>
            <input
              type="text"
              id="studentName"
              name="studentName"
              value={formData.studentName}
              onChange={handleChange}
              placeholder="Enter your full name"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="studentId">Student ID *</label>
            <input
              type="text"
              id="studentId"
              name="studentId"
              value={formData.studentId}
              onChange={handleChange}
              placeholder="Enter your student ID"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email Address *</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Enter your email"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="program">Program *</label>
            <select
              id="program"
              name="program"
              value={formData.program}
              onChange={handleChange}
              required
              disabled={loading}
            >
              <option value="">Select a program</option>
              <option value="Computer Science">Computer Science</option>
              <option value="Business Administration">Business Administration</option>
              <option value="Engineering">Engineering</option>
              <option value="Nursing">Nursing</option>
              <option value="Education">Education</option>
              <option value="Liberal Arts">Liberal Arts</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="yearLevel">Year Level *</label>
            <select
              id="yearLevel"
              name="yearLevel"
              value={formData.yearLevel}
              onChange={handleChange}
              required
              disabled={loading}
            >
              <option value="">Select year level</option>
              <option value="First Year">First Year</option>
              <option value="Second Year">Second Year</option>
              <option value="Third Year">Third Year</option>
              <option value="Fourth Year">Fourth Year</option>
            </select>
          </div>

          <button 
            type="submit" 
            className="submit-btn"
            disabled={loading}
          >
            {loading ? 'Submitting...' : 'Submit Registration'}
          </button>
        </form>

        <div className="info-box">
          <h3>ℹ️ Integration Information</h3>
          <p>
            Your registration data is being processed through our enterprise integration system.
            This form sends your information through a message queue (RabbitMQ) to our backend
            systems for processing and storage.
          </p>
          <p><strong>Processing Pattern:</strong> Asynchronous Message Queue</p>
        </div>
      </div>
    </div>
  );
}

export default RegistrationForm;
