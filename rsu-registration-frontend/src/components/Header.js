import React from 'react';
import './Header.css';

function Header() {
  return (
    <header className="header">
      <div className="header-container">
        <div className="header-content">
          <h1>Rejoice State University</h1>
          <p>Student Registration System</p>
        </div>
        <div className="header-info">
          <span className="status-indicator">● System Online</span>
        </div>
      </div>
    </header>
  );
}

export default Header;
