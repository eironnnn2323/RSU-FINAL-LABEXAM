import React from 'react';
import './App.css';
import RegistrationForm from './components/RegistrationForm';
import Header from './components/Header';

function App() {
  return (
    <div className="App">
      <Header />
      <main className="main-container">
        <RegistrationForm />
      </main>
    </div>
  );
}

export default App;
