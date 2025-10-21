import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './RegistrationForm.css';
import logo from '../images/logo.png';

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
  const [routingInfo, setRoutingInfo] = useState(null); // Routing decision info
  const [translationChain, setTranslationChain] = useState(null); // Message translation chain
  const [aggregatedProfile, setAggregatedProfile] = useState(null); // Aggregated profile
  const [loadingProfile, setLoadingProfile] = useState(false);
  const [showModal, setShowModal] = useState(false); // Modal visibility
  const [submittedStudentId, setSubmittedStudentId] = useState(''); // Store student ID for download

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
      setSubmittedStudentId(formData.studentId); // Store for XML download
      
      // Store routing information for display
      if (response.data.routedTo && response.data.routedTo.length > 0) {
        setRoutingInfo({
          isFirstYear: response.data.isFirstYear,
          routedTo: response.data.routedTo,
          routingMessage: response.data.routingMessage
        });
      }

      // Store translation chain information
      if (response.data.translationChain) {
        setTranslationChain(response.data.translationChain);
        console.log('Translation Chain:', response.data.translationChain);
      }

      // Show modal
      setShowModal(true);

      // Fetch aggregated profile after a short delay (wait for systems to respond)
      setTimeout(() => {
        fetchAggregatedProfile(formData.studentId);
      }, 3000); // Wait 3 seconds for aggregation to complete

      // Reset form
      setFormData({
        studentName: '',
        studentId: '',
        email: '',
        program: '',
        yearLevel: ''
      });

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

  const fetchAggregatedProfile = async (studentId) => {
    setLoadingProfile(true);
    try {
      console.log('Fetching aggregated profile for:', studentId);
      
      const response = await axios.get(`${API_URL}/profile/${studentId}`);
      
      console.log('Aggregated Profile Response:', response.data);
      console.log('Profile Status:', response.data?.aggregationStatus);
      console.log('Profile Data:', {
        academicRecords: response.data?.academicRecords,
        housing: response.data?.housing,
        billing: response.data?.billing,
        library: response.data?.library
      });
      
      setAggregatedProfile(response.data);
      
    } catch (error) {
      console.error('Error fetching aggregated profile:', error);
      console.error('Error response:', error.response?.data);
      // Don't show error to user, profile might still be aggregating
    } finally {
      setLoadingProfile(false);
    }
  };

  const closeModal = () => {
    setShowModal(false);
    setMessage('');
    setMessageType('');
    setRoutingInfo(null);
    setTranslationChain(null);
    setAggregatedProfile(null);
    setSubmittedStudentId('');
  };

  const downloadXml = async () => {
    if (!submittedStudentId) {
      alert('No student ID available for download');
      return;
    }

    try {
      const response = await axios.get(`${API_URL}/download-xml/${submittedStudentId}`, {
        responseType: 'blob'
      });

      // Create a blob from the response
      const blob = new Blob([response.data], { type: 'application/xml' });
      const url = window.URL.createObjectURL(blob);
      
      // Create a temporary link and trigger download
      const link = document.createElement('a');
      link.href = url;
      link.download = `registration_${submittedStudentId}.xml`;
      document.body.appendChild(link);
      link.click();
      
      // Clean up
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      alert('✅ XML file downloaded successfully!');
    } catch (error) {
      console.error('Error downloading XML:', error);
      alert('❌ Failed to download XML file. Please try again.');
    }
  };

  const handleModalOverlayClick = (e) => {
    if (e.target.className === 'modal-overlay') {
      closeModal();
    }
  };

  // Add escape key listener
  useEffect(() => {
    const handleEscapeKey = (e) => {
      if (e.key === 'Escape' && showModal) {
        closeModal();
      }
    };

    document.addEventListener('keydown', handleEscapeKey);
    return () => document.removeEventListener('keydown', handleEscapeKey);
  }, [showModal]);

  return (
    <div className="registration-form-container">
      {/* Left Side - Information Panel */}
      <div className="info-panel">
        <div className="info-panel-content">
          <div className="university-logo">
            <div className="logo-circle">
              <img src={logo} alt="RSU Logo" />
            </div>
            <h2>Rejoice State University</h2>
            <p className="tagline">Excellence in Education </p>
          </div>

          <div className="welcome-section">
            <h3>Welcome to Student Registration</h3>
            <p>Join thousands of students in their journey towards academic excellence.</p>
          </div>

          <div className="info-box-left">
            <h3>🔀 Content-Based Routing</h3>
            <p>
              Your registration uses <strong>Content-Based Routing</strong> - an Enterprise Integration Pattern 
              that intelligently routes messages to different systems based on your year level.
            </p>
            
            <div className="routing-rules">
              <h4>Routing Rules:</h4>
              <ul>
                <li><strong>🏠 First Year Students</strong> → Housing System</li>
                <li><strong>💰 Returning Students</strong> → Billing System</li>
                <li><strong>📚 All Students</strong> → Library System</li>
              </ul>
            </div>

            <div className="tech-stack">
              <h4>🔧 Technology Stack:</h4>
              <div className="tech-badges">
                <span className="tech-badge">React</span>
                <span className="tech-badge">Spring Boot</span>
                <span className="tech-badge">RabbitMQ</span>
                <span className="tech-badge">PostgreSQL</span>
              </div>
            </div>
          </div>

          <div className="features-section">
            <h4>✨ Key Features:</h4>
            <div className="feature-item">
              <span className="feature-icon">⚡</span>
              <div>
                <strong>Fast Processing</strong>
                <p>Instant registration with real-time updates</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">🔒</span>
              <div>
                <strong>Secure & Reliable</strong>
                <p>Your data is protected and safely stored</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">🎯</span>
              <div>
                <strong>Smart Routing</strong>
                <p>Automatic system allocation based on your profile</p>
              </div>
            </div>
          </div>

          <div className="stats-section">
            <div className="stat-item">
              <div className="stat-number">10K+</div>
              <div className="stat-label">Students Enrolled</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">50+</div>
              <div className="stat-label">Programs Offered</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">98%</div>
              <div className="stat-label">Satisfaction Rate</div>
            </div>
          </div>
        </div>
      </div>

      {/* Right Side - Registration Form */}
      <div className="form-panel">
        <div className="form-card">
          <h1>Student Registration</h1>
          <p className="form-subtitle">Fill in your details to get started</p>

          {/* Only show error messages inline */}
          {message && messageType === 'error' && (
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
              {loading ? (
                <>
                  <span className="btn-spinner"></span>
                  Submitting...
                </>
              ) : (
                <>
                  <span>Submit Registration</span>
                  <span className="btn-arrow">→</span>
                </>
              )}
            </button>
          </form>
        </div>
      </div>

      {/* Success Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={handleModalOverlayClick}>
          <div className="modal-content">
            <div className="modal-header">
              <h2>✅ Registration Successful!</h2>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            
            <div className="modal-body">
              <div className="success-message">
                {message}
              </div>

              {/* Display routing information */}
              {routingInfo && (
                <div className="routing-info">
                  <h4>🔀 Routing Decision:</h4>
                  <p className="routing-message">{routingInfo.routingMessage}</p>
                  <div className="routing-systems">
                    <strong>Systems Processing Your Registration:</strong>
                    <ul>
                      {routingInfo.routedTo.map((system, index) => (
                        <li key={index}>
                          {system === 'Housing System' && '🏠 '}
                          {system === 'Billing System' && '💰 '}
                          {system === 'Library System' && '📚 '}
                          {system}
                        </li>
                      ))}
                    </ul>
                  </div>
                  <div className="student-type">
                    <span className={`badge ${routingInfo.isFirstYear ? 'first-year' : 'returning'}`}>
                      {routingInfo.isFirstYear ? '🎓 First Year Student' : '📖 Returning Student'}
                    </span>
                  </div>
                </div>
              )}

              {/* Display Message Translation Chain */}
              {translationChain && (
                <div className="translation-chain">
                  <h4>🔄 Message Translation Chain:</h4>
                  <p className="translation-info">
                    Your registration data was automatically translated between {translationChain.translationSteps?.length || 0} different formats 
                    for different backend systems. Total translation time: <strong>{translationChain.totalTranslationTimeMs}ms</strong>
                  </p>
                  
                  <div className="translation-steps">
                    {translationChain.translationSteps && translationChain.translationSteps.map((step, index) => (
                      <div key={index} className="translation-step">
                        <div className="step-header">
                          <span className="step-number">Step {index + 1}</span>
                          <span className="step-time">{step.durationMs}ms</span>
                        </div>
                        <div className="step-content">
                          <div className="format-flow">
                            <span className="format-badge source">{step.fromFormat}</span>
                            <span className="arrow">→</span>
                            <span className="format-badge target">{step.toFormat}</span>
                          </div>
                          <p className="translator-name">{step.translatorName}</p>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="translation-summary">
                    <div className="summary-item">
                      <span className="summary-label">Original Format:</span>
                      <span className="summary-value">{translationChain.originalFormat}</span>
                    </div>
                    <div className="summary-item">
                      <span className="summary-label">Total Steps:</span>
                      <span className="summary-value">{translationChain.translationSteps?.length || 0}</span>
                    </div>
                    <div className="summary-item">
                      <span className="summary-label">Status:</span>
                      <span className={`summary-value ${translationChain.successful ? 'success' : 'error'}`}>
                        {translationChain.successful ? '✓ Successful' : '✗ Failed'}
                      </span>
                    </div>
                  </div>
                </div>
              )}

              {/* Display Aggregated Profile */}
              <div className="aggregated-profile">
                {loadingProfile && (
                  <div className="profile-loading">
                    <div className="spinner"></div>
                    <p>🔄 Aggregating responses from all systems...</p>
                    <small>This may take a few seconds as we gather data from all university systems</small>
                  </div>
                )}

                {!loadingProfile && !aggregatedProfile && (
                  <div className="profile-pending">
                    <p>⏳ Profile aggregation in progress...</p>
                    <small>Your registration is being processed across multiple systems. This information will be available shortly.</small>
                  </div>
                )}

                {!loadingProfile && aggregatedProfile && (
                  <div className="profile-complete">
                    <h3 className="profile-title">
                      📋 Complete Profile Overview
                    </h3>
                    
                    <div className="profile-status">
                      <span className={`status-badge ${aggregatedProfile.aggregationStatus?.toLowerCase() || 'pending'}`}>
                        {aggregatedProfile.aggregationStatus || 'PENDING'}
                      </span>
                      <span className="response-count">
                        {aggregatedProfile.responsesReceived || 0}/{aggregatedProfile.responsesExpected || 3} Systems Responded
                      </span>
                      <span className="aggregation-time">
                        ⚡ {aggregatedProfile.aggregationTimeMs || 0}ms
                      </span>
                    </div>

                    {/* Academic Records */}
                    {aggregatedProfile.academicRecords && (
                      <div className="profile-section">
                        <h4>📚 Academic Records</h4>
                        <div className="profile-details">
                          <div className="detail-item">
                            <strong>Status:</strong> {aggregatedProfile.academicRecords.enrollmentStatus}
                          </div>
                          <div className="detail-item">
                            <strong>Program:</strong> {aggregatedProfile.academicRecords.program}
                          </div>
                          <div className="detail-item">
                            <strong>Advisor:</strong> {aggregatedProfile.academicRecords.advisorName}
                          </div>
                          <div className="detail-item">
                            <strong>GPA:</strong> {aggregatedProfile.academicRecords.gpa?.toFixed(2)}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Housing (for first-year) */}
                    {aggregatedProfile.housing && (
                      <div className="profile-section">
                        <h4>🏠 Housing Assignment</h4>
                        <div className="profile-details">
                          <div className="detail-item">
                            <strong>Dormitory:</strong> {aggregatedProfile.housing.dormitoryBuilding}
                          </div>
                          <div className="detail-item">
                            <strong>Room:</strong> {aggregatedProfile.housing.roomAssignment}
                          </div>
                          <div className="detail-item">
                            <strong>Roommate:</strong> {aggregatedProfile.housing.roommateName}
                          </div>
                          <div className="detail-item">
                            <strong>Move-in Date:</strong> {aggregatedProfile.housing.moveInDate}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Billing (for returning) */}
                    {aggregatedProfile.billing && (
                      <div className="profile-section">
                        <h4>💰 Billing Information</h4>
                        <div className="profile-details">
                          <div className="detail-item">
                            <strong>Total Fees:</strong> ₱{aggregatedProfile.billing.totalFeeAmount}
                          </div>
                          <div className="detail-item">
                            <strong>Tuition:</strong> ₱{aggregatedProfile.billing.tuitionFee}
                          </div>
                          <div className="detail-item">
                            <strong>Payment Deadline:</strong> {aggregatedProfile.billing.paymentDeadline}
                          </div>
                          <div className="detail-item">
                            <strong>Account Status:</strong> {aggregatedProfile.billing.accountStatus}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Library */}
                    {aggregatedProfile.library && (
                      <div className="profile-section">
                        <h4>📚 Library Services</h4>
                        <div className="profile-details">
                          <div className="detail-item">
                            <strong>Library Card:</strong> {aggregatedProfile.library.libraryCardNumber}
                          </div>
                          <div className="detail-item">
                            <strong>Status:</strong> {aggregatedProfile.library.accountStatus}
                          </div>
                          <div className="detail-item">
                            <strong>Max Books:</strong> {aggregatedProfile.library.maxBooksAllowed}
                          </div>
                          <div className="detail-item">
                            <strong>Valid Until:</strong> {aggregatedProfile.library.expirationDate}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>

              <div className="modal-footer">
                <button className="download-xml-btn" onClick={downloadXml}>
                  📥 Download as XML
                </button>
                <button className="modal-close-btn" onClick={closeModal}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default RegistrationForm;
