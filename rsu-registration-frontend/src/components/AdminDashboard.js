import React, { useState, useEffect } from 'react';
import './AdminDashboard.css';

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('overview');
    const [stats, setStats] = useState(null);
    const [errorLogs, setErrorLogs] = useState([]);
    const [failedMessages, setFailedMessages] = useState([]);
    const [dlqMessages, setDlqMessages] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedMessage, setSelectedMessage] = useState(null);
    const [adminUser] = useState('admin@rsu.edu');
    const [retryNotes, setRetryNotes] = useState('');
    const [showRetryModal, setShowRetryModal] = useState(false);
    const [filterCategory, setFilterCategory] = useState('');
    const [filterStage, setFilterStage] = useState('');
    const [refreshInterval, setRefreshInterval] = useState(null);

    const API_BASE = 'http://localhost:8080/api/v1';

    // Fetch system statistics
    const fetchStats = async () => {
        try {
            const response = await fetch(`${API_BASE}/admin/stats`);
            const data = await response.json();
            setStats(data);
        } catch (error) {
            console.error('Error fetching stats:', error);
        }
    };

    // Fetch error logs
    const fetchErrorLogs = async () => {
        try {
            setLoading(true);
            let url = `${API_BASE}/admin/errors`;
            
            if (filterCategory || filterStage) {
                const params = new URLSearchParams();
                if (filterCategory) params.append('category', filterCategory);
                if (filterStage) params.append('stage', filterStage);
                url = `${API_BASE}/admin/errors/search?${params}`;
            }
            
            const response = await fetch(url);
            const data = await response.json();
            setErrorLogs(data);
        } catch (error) {
            console.error('Error fetching error logs:', error);
        } finally {
            setLoading(false);
        }
    };

    // Fetch failed messages
    const fetchFailedMessages = async () => {
        try {
            setLoading(true);
            const response = await fetch(`${API_BASE}/admin/failed-messages`);
            const data = await response.json();
            setFailedMessages(data);
        } catch (error) {
            console.error('Error fetching failed messages:', error);
        } finally {
            setLoading(false);
        }
    };

    // Fetch DLQ messages
    const fetchDLQMessages = async () => {
        try {
            setLoading(true);
            const response = await fetch(`${API_BASE}/admin/failed-messages/dlq`);
            const data = await response.json();
            setDlqMessages(data);
        } catch (error) {
            console.error('Error fetching DLQ messages:', error);
        } finally {
            setLoading(false);
        }
    };

    // Manual retry
    const handleManualRetry = async (messageId) => {
        try {
            const response = await fetch(
                `${API_BASE}/admin/retry/${messageId}?adminUser=${encodeURIComponent(adminUser)}&notes=${encodeURIComponent(retryNotes)}`,
                { method: 'POST' }
            );
            const result = await response.json();
            
            if (result.success) {
                alert('✅ Manual retry successful!');
                fetchFailedMessages();
                fetchDLQMessages();
                fetchStats();
            } else {
                alert('⚠️ Manual retry failed: ' + result.message);
            }
            
            setShowRetryModal(false);
            setSelectedMessage(null);
            setRetryNotes('');
        } catch (error) {
            console.error('Error during manual retry:', error);
            alert('❌ Error during manual retry: ' + error.message);
        }
    };

    // Retry all DLQ
    const handleRetryAllDLQ = async () => {
        if (!window.confirm('Are you sure you want to retry all messages in the Dead-Letter Queue?')) {
            return;
        }

        try {
            const response = await fetch(
                `${API_BASE}/admin/retry-all-dlq?adminUser=${encodeURIComponent(adminUser)}&notes=Bulk retry from admin dashboard`,
                { method: 'POST' }
            );
            const result = await response.json();
            
            alert(`✅ Bulk retry complete!\nTotal: ${result.totalMessages}\nSuccess: ${result.successCount}\nFailed: ${result.failureCount}`);
            
            fetchFailedMessages();
            fetchDLQMessages();
            fetchStats();
        } catch (error) {
            console.error('Error during bulk retry:', error);
            alert('❌ Error during bulk retry: ' + error.message);
        }
    };

    // Auto-refresh
    useEffect(() => {
        // Initial fetch
        fetchStats();
        
        // Set up auto-refresh every 10 seconds
        const interval = setInterval(() => {
            fetchStats();
            if (activeTab === 'errors') fetchErrorLogs();
            if (activeTab === 'failed') fetchFailedMessages();
            if (activeTab === 'dlq') fetchDLQMessages();
        }, 10000);
        
        setRefreshInterval(interval);
        
        return () => clearInterval(interval);
    }, [activeTab]);

    // Fetch data when tab changes
    useEffect(() => {
        switch (activeTab) {
            case 'errors':
                fetchErrorLogs();
                break;
            case 'failed':
                fetchFailedMessages();
                break;
            case 'dlq':
                fetchDLQMessages();
                break;
            default:
                break;
        }
    }, [activeTab, filterCategory, filterStage]);

    // Format timestamp
    const formatTimestamp = (timestamp) => {
        if (!timestamp) return 'N/A';
        return new Date(timestamp).toLocaleString();
    };

    // Get status badge color
    const getStatusBadgeClass = (status) => {
        const statusMap = {
            'PENDING_RETRY': 'status-pending',
            'RETRYING': 'status-retrying',
            'RETRY_SUCCESS': 'status-success',
            'MOVED_TO_DLQ': 'status-dlq',
            'AWAITING_MANUAL_RETRY': 'status-awaiting',
            'MANUAL_RETRY_SUCCESS': 'status-success',
            'FAILED': 'status-failed'
        };
        return statusMap[status] || 'status-default';
    };

    // Get category badge color
    const getCategoryBadgeClass = (category) => {
        const categoryMap = {
            'SYSTEM_DOWN': 'category-system',
            'NETWORK_TIMEOUT': 'category-network',
            'INVALID_DATA': 'category-data',
            'DATABASE_ERROR': 'category-database',
            'QUEUE_ERROR': 'category-queue',
            'TRANSLATION_ERROR': 'category-translation',
            'ROUTING_ERROR': 'category-routing',
            'AGGREGATION_ERROR': 'category-aggregation',
            'UNKNOWN': 'category-unknown'
        };
        return categoryMap[category] || 'category-default';
    };

    return (
        <div className="admin-dashboard">
            <div className="dashboard-header">
                <h1>🛡️ Admin Dashboard - Error Monitoring & Recovery</h1>
                <p className="dashboard-subtitle">Monitor system errors, manage failed registrations, and perform manual recovery operations</p>
            </div>

            {/* Tab Navigation */}
            <div className="tab-navigation">
                <button 
                    className={`tab-button ${activeTab === 'overview' ? 'active' : ''}`}
                    onClick={() => setActiveTab('overview')}
                >
                    📊 Overview
                </button>
                <button 
                    className={`tab-button ${activeTab === 'errors' ? 'active' : ''}`}
                    onClick={() => setActiveTab('errors')}
                >
                    🚨 Error Logs
                </button>
                <button 
                    className={`tab-button ${activeTab === 'failed' ? 'active' : ''}`}
                    onClick={() => setActiveTab('failed')}
                >
                    ⚠️ Failed Messages
                </button>
                <button 
                    className={`tab-button ${activeTab === 'dlq' ? 'active' : ''}`}
                    onClick={() => setActiveTab('dlq')}
                >
                    💀 Dead-Letter Queue ({dlqMessages.length})
                </button>
            </div>

            {/* Overview Tab */}
            {activeTab === 'overview' && stats && (
                <div className="tab-content">
                    <div className="stats-grid">
                        <div className="stat-card stat-primary">
                            <div className="stat-icon">📨</div>
                            <div className="stat-content">
                                <div className="stat-value">{stats.totalFailedMessages || 0}</div>
                                <div className="stat-label">Total Failed Messages</div>
                            </div>
                        </div>
                        
                        <div className="stat-card stat-warning">
                            <div className="stat-icon">💀</div>
                            <div className="stat-content">
                                <div className="stat-value">{stats.messagesInDlq || 0}</div>
                                <div className="stat-label">In Dead-Letter Queue</div>
                            </div>
                        </div>
                        
                        <div className="stat-card stat-info">
                            <div className="stat-icon">🔄</div>
                            <div className="stat-content">
                                <div className="stat-value">{stats.pendingRetry || 0}</div>
                                <div className="stat-label">Pending Retry</div>
                            </div>
                        </div>
                        
                        <div className="stat-card stat-success">
                            <div className="stat-icon">✅</div>
                            <div className="stat-content">
                                <div className="stat-value">{stats.retrySuccess || 0}</div>
                                <div className="stat-label">Retry Successes</div>
                            </div>
                        </div>
                    </div>

                    <div className="chart-grid">
                        <div className="chart-card">
                            <h3>❌ Errors by Category</h3>
                            <div className="category-list">
                                {stats.errorsByCategory && Object.entries(stats.errorsByCategory).map(([category, count]) => (
                                    <div key={category} className="category-item">
                                        <span className={`category-badge ${getCategoryBadgeClass(category)}`}>
                                            {category.replace(/_/g, ' ')}
                                        </span>
                                        <span className="category-count">{count}</span>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="chart-card">
                            <h3>📍 Failures by Stage</h3>
                            <div className="stage-list">
                                {stats.errorsByStage && Object.entries(stats.errorsByStage).map(([stage, count]) => (
                                    <div key={stage} className="stage-item">
                                        <span className="stage-name">{stage}</span>
                                        <span className="stage-count">{count}</span>
                                        <div className="stage-bar" style={{width: `${(count / Math.max(...Object.values(stats.errorsByStage))) * 100}%`}}></div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="metrics-card">
                        <h3>📈 System Metrics</h3>
                        <div className="metrics-grid">
                            <div className="metric">
                                <span className="metric-label">Total Errors:</span>
                                <span className="metric-value">{stats.totalErrors || 0}</span>
                            </div>
                            <div className="metric">
                                <span className="metric-label">Unresolved Errors:</span>
                                <span className="metric-value text-warning">{stats.unresolvedErrors || 0}</span>
                            </div>
                            <div className="metric">
                                <span className="metric-label">Awaiting Manual Retry:</span>
                                <span className="metric-value text-danger">{stats.awaitingManualRetry || 0}</span>
                            </div>
                            <div className="metric">
                                <span className="metric-label">Manual Retry Success:</span>
                                <span className="metric-value text-success">{stats.manualRetrySuccess || 0}</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Error Logs Tab */}
            {activeTab === 'errors' && (
                <div className="tab-content">
                    <div className="content-header">
                        <h2>🚨 Error Logs</h2>
                        <div className="filters">
                            <select 
                                value={filterCategory} 
                                onChange={(e) => setFilterCategory(e.target.value)}
                                className="filter-select"
                            >
                                <option value="">All Categories</option>
                                <option value="SYSTEM_DOWN">System Down</option>
                                <option value="NETWORK_TIMEOUT">Network Timeout</option>
                                <option value="INVALID_DATA">Invalid Data</option>
                                <option value="DATABASE_ERROR">Database Error</option>
                                <option value="QUEUE_ERROR">Queue Error</option>
                                <option value="TRANSLATION_ERROR">Translation Error</option>
                                <option value="ROUTING_ERROR">Routing Error</option>
                                <option value="AGGREGATION_ERROR">Aggregation Error</option>
                            </select>
                            <button onClick={fetchErrorLogs} className="refresh-button">🔄 Refresh</button>
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading">Loading error logs...</div>
                    ) : (
                        <div className="table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Timestamp</th>
                                        <th>Student</th>
                                        <th>Stage</th>
                                        <th>Category</th>
                                        <th>Error Message</th>
                                        <th>Severity</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {errorLogs.map(log => (
                                        <tr key={log.id}>
                                            <td>{log.id}</td>
                                            <td className="timestamp">{formatTimestamp(log.errorTimestamp)}</td>
                                            <td>
                                                <div>{log.studentName || 'N/A'}</div>
                                                <div className="small-text">{log.studentId || 'N/A'}</div>
                                            </td>
                                            <td><span className="stage-badge">{log.errorStage}</span></td>
                                            <td><span className={`category-badge ${getCategoryBadgeClass(log.errorCategory)}`}>
                                                {log.errorCategory}
                                            </span></td>
                                            <td className="error-message">{log.errorMessage}</td>
                                            <td><span className={`severity-badge severity-${log.severity.toLowerCase()}`}>
                                                {log.severity}
                                            </span></td>
                                            <td>{log.resolved ? '✅ Resolved' : '⚠️ Unresolved'}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            {errorLogs.length === 0 && (
                                <div className="empty-state">No error logs found</div>
                            )}
                        </div>
                    )}
                </div>
            )}

            {/* Failed Messages Tab */}
            {activeTab === 'failed' && (
                <div className="tab-content">
                    <div className="content-header">
                        <h2>⚠️ Failed Messages</h2>
                        <button onClick={fetchFailedMessages} className="refresh-button">🔄 Refresh</button>
                    </div>

                    {loading ? (
                        <div className="loading">Loading failed messages...</div>
                    ) : (
                        <div className="table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Student</th>
                                        <th>Failed At</th>
                                        <th>Failure Stage</th>
                                        <th>Category</th>
                                        <th>Status</th>
                                        <th>Retry Attempts</th>
                                        <th>Next Retry</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {failedMessages.map(msg => (
                                        <tr key={msg.id}>
                                            <td>{msg.id}</td>
                                            <td>
                                                <div>{msg.studentName}</div>
                                                <div className="small-text">{msg.studentId}</div>
                                            </td>
                                            <td className="timestamp">{formatTimestamp(msg.failedAt)}</td>
                                            <td><span className="stage-badge">{msg.failureStage}</span></td>
                                            <td><span className={`category-badge ${getCategoryBadgeClass(msg.errorCategory)}`}>
                                                {msg.errorCategory}
                                            </span></td>
                                            <td><span className={`status-badge ${getStatusBadgeClass(msg.status)}`}>
                                                {msg.status.replace(/_/g, ' ')}
                                            </span></td>
                                            <td>{msg.retryAttempts} / {msg.maxRetryAttempts}</td>
                                            <td className="timestamp small-text">
                                                {msg.status === 'PENDING_RETRY' ? formatTimestamp(msg.nextRetryAt) : '-'}
                                            </td>
                                            <td>
                                                <button 
                                                    className="action-button"
                                                    onClick={() => {
                                                        setSelectedMessage(msg);
                                                        setShowRetryModal(true);
                                                    }}
                                                >
                                                    🔄 Retry
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            {failedMessages.length === 0 && (
                                <div className="empty-state">✅ No failed messages - All clear!</div>
                            )}
                        </div>
                    )}
                </div>
            )}

            {/* Dead-Letter Queue Tab */}
            {activeTab === 'dlq' && (
                <div className="tab-content">
                    <div className="content-header">
                        <h2>💀 Dead-Letter Queue</h2>
                        <div>
                            {dlqMessages.length > 0 && (
                                <button onClick={handleRetryAllDLQ} className="action-button action-primary">
                                    🔄 Retry All DLQ Messages
                                </button>
                            )}
                            <button onClick={fetchDLQMessages} className="refresh-button">🔄 Refresh</button>
                        </div>
                    </div>

                    {loading ? (
                        <div className="loading">Loading DLQ messages...</div>
                    ) : (
                        <div className="table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Student</th>
                                        <th>Failed At</th>
                                        <th>Moved to DLQ</th>
                                        <th>Failure Stage</th>
                                        <th>Category</th>
                                        <th>Error Message</th>
                                        <th>Retry History</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {dlqMessages.map(msg => (
                                        <tr key={msg.id} className="dlq-row">
                                            <td>{msg.id}</td>
                                            <td>
                                                <div>{msg.studentName}</div>
                                                <div className="small-text">{msg.studentId}</div>
                                                <div className="small-text">{msg.email}</div>
                                            </td>
                                            <td className="timestamp">{formatTimestamp(msg.failedAt)}</td>
                                            <td className="timestamp">{formatTimestamp(msg.movedToDlqAt)}</td>
                                            <td><span className="stage-badge">{msg.failureStage}</span></td>
                                            <td><span className={`category-badge ${getCategoryBadgeClass(msg.errorCategory)}`}>
                                                {msg.errorCategory}
                                            </span></td>
                                            <td className="error-message">{msg.errorMessage}</td>
                                            <td>
                                                <details className="retry-history">
                                                    <summary>View History ({msg.retryAttempts} attempts)</summary>
                                                    <pre className="history-content">{msg.retryHistory}</pre>
                                                </details>
                                            </td>
                                            <td>
                                                <button 
                                                    className="action-button action-danger"
                                                    onClick={() => {
                                                        setSelectedMessage(msg);
                                                        setShowRetryModal(true);
                                                    }}
                                                >
                                                    🛠️ Manual Retry
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            {dlqMessages.length === 0 && (
                                <div className="empty-state">✅ Dead-Letter Queue is empty - All good!</div>
                            )}
                        </div>
                    )}
                </div>
            )}

            {/* Manual Retry Modal */}
            {showRetryModal && selectedMessage && (
                <div className="modal-overlay" onClick={() => setShowRetryModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>🛠️ Manual Retry</h3>
                            <button className="modal-close" onClick={() => setShowRetryModal(false)}>×</button>
                        </div>
                        
                        <div className="modal-body">
                            <div className="message-details">
                                <h4>Message Details:</h4>
                                <div className="detail-row">
                                    <span className="detail-label">Student:</span>
                                    <span className="detail-value">{selectedMessage.studentName} ({selectedMessage.studentId})</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Failed At:</span>
                                    <span className="detail-value">{formatTimestamp(selectedMessage.failedAt)}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Stage:</span>
                                    <span className="detail-value">{selectedMessage.failureStage}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Error:</span>
                                    <span className="detail-value error-text">{selectedMessage.errorMessage}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Retry Attempts:</span>
                                    <span className="detail-value">{selectedMessage.retryAttempts} / {selectedMessage.maxRetryAttempts}</span>
                                </div>
                            </div>

                            <div className="form-group">
                                <label>Admin Notes:</label>
                                <textarea
                                    value={retryNotes}
                                    onChange={(e) => setRetryNotes(e.target.value)}
                                    placeholder="Enter notes about this manual retry (optional)..."
                                    rows="4"
                                    className="form-textarea"
                                />
                            </div>
                        </div>

                        <div className="modal-footer">
                            <button 
                                className="button button-secondary"
                                onClick={() => {
                                    setShowRetryModal(false);
                                    setRetryNotes('');
                                }}
                            >
                                Cancel
                            </button>
                            <button 
                                className="button button-primary"
                                onClick={() => handleManualRetry(selectedMessage.id)}
                            >
                                🔄 Retry Now
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminDashboard;
