-- Domains
CREATE TABLE IF NOT EXISTS domains (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    dkim_private_key TEXT,
    dkim_selector VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL, -- full email or local part
    password_hash VARCHAR(255) NOT NULL,
    domain_id INT REFERENCES domains(id),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Folders
CREATE TABLE IF NOT EXISTS folders (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    name VARCHAR(64) NOT NULL, -- Inbox, Sent, Trash, etc.
    parent_id INT REFERENCES folders(id),
    UNIQUE(user_id, name, parent_id)
);

-- Emails
CREATE TABLE IF NOT EXISTS emails (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id), -- Owner
    folder_id INT REFERENCES folders(id),
    sender VARCHAR(255) NOT NULL,
    recipients TEXT NOT NULL, -- JSON or comma-separated
    subject VARCHAR(998),
    body_text TEXT,
    body_html TEXT,
    message_id VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    size_bytes BIGINT,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attachments
CREATE TABLE IF NOT EXISTS attachments (
    id SERIAL PRIMARY KEY,
    email_id INT REFERENCES emails(id) ON DELETE CASCADE,
    filename VARCHAR(255),
    content_type VARCHAR(255),
    data BYTEA, -- Or path to file storage
    size_bytes BIGINT
);

-- Outbound Queue
CREATE TABLE IF NOT EXISTS outbound_queue (
    id SERIAL PRIMARY KEY,
    sender VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    email_data BYTEA NOT NULL, -- Raw EML content
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, RETRY, FAILED, SENT
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
