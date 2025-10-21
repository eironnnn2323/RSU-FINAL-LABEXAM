import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './Header.css';
import logo from '../images/logo.png';

function Header() {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <nav className={`navbar ${scrolled ? 'navbar-scrolled' : ''}`}>
      <div className="navbar-container">
        {/* Logo Section */}
        <div className="navbar-brand">
          <div className="logo-wrapper">
            <div className="logo-image-container">
              <img src={logo} alt="RSU Logo" className="logo-image" />
            </div>
            <div className="brand-text">
              <h1 className="brand-name">Rejoice State University</h1>
              <p className="brand-tagline">Student Registration Portal</p>
            </div>
          </div>
        </div>

        {/* Navigation Links */}
        <div className="navbar-navigation">
          <Link to="/" className="nav-link">
            <span className="nav-icon">🎓</span>
            <span>Student Registration</span>
          </Link>
          <Link to="/admin" className="nav-link">
            <span className="nav-icon">🛡️</span>
            <span>Admin Dashboard</span>
          </Link>
        </div>

        {/* Status Indicator */}
        <div className="navbar-actions">
          <div className="status-badge">
            <span className="status-dot"></span>
            <span className="status-text">System Online</span>
          </div>
        </div>
      </div>
    </nav>
  );
}

export default Header;
