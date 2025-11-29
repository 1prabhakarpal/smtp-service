# Frontend Development Guide - SMTP Service API

> **Complete API Reference and Integration Guide**  
> API Base URL: `http://localhost:8095` (Development) | `https://api.yourdomain.com` (Production)

---

## 📋 Table of Contents

1. [Quick Start](#quick-start)
2. [Authentication](#authentication)
3. [API Endpoints Reference](#api-endpoints-reference)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [Integration Examples](#integration-examples)
7. [Frontend Best Practices](#frontend-best-practices)
8. [WebSocket/Real-time Updates](#websocket-real-time-updates)
9. [CORS Configuration](#cors-configuration)

---

## Quick Start

### 1. API Overview

- **Protocol**: HTTP/HTTPS REST API
- **Authentication**: JWT Bearer Token
- **Content-Type**: `application/json`
- **Base URL**: `http://localhost:8095/api`
- **API Documentation**: `http://localhost:8095/swagger-ui.html`

### 2. Basic Authentication Flow

```javascript
// 1. Register User
POST /api/register
Body: { "username": "john", "password": "secure123" }

// 2. Login
POST /api/login  
Body: { "username": "john", "password": "secure123" }
Response: { "token": "eyJhbGci...", "type": "Bearer", "username": "john", "roles": "USER" }

// 3. Use Token in Headers
GET /api/emails
Headers: { "Authorization": "Bearer eyJhbGci..." }
```

### 3. Quick Integration Snippet

```javascript
const API_BASE = 'http://localhost:8095/api';

// Login and get token
async function login(username, password) {
  const response = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  const data = await response.json();
  localStorage.setItem('token', data.token);
  return data;
}

// Fetch emails with authentication
async function getEmails(page = 0, size = 20) {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE}/emails?page=${page}&size=${size}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return await response.json();
}
```

---

## Authentication

### Overview
- **Method**: JWT (JSON Web Token)
- **Token Location**: `Authorization` header
- **Format**: `Bearer <token>`
- **Expiration**: 24 hours (configurable via `JWT_EXPIRATION_MS`)

### Endpoints

#### 1. Register User
```http
POST /api/register
```

**Request Body:**
```json
{
  "username": "john.doe",
  "password": "SecurePassword123!"
}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "username": "john.doe",
  "roles": "USER"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Username already exists"
}
```

---

#### 2. Login
```http
POST /api/login
```

**Request Body:**
```json
{
  "username": "john.doe",
  "password": "SecurePassword123!"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZSIsInJvbGVzIjoiVVNFUiIsImlhdCI6MTYzODM2MTIwMCwiZXhwIjoxNjM4NDQ3NjAwfQ.xyz",
  "type": "Bearer",
  "username": "john.doe",
  "roles": "USER"
}
```

**Error Response (401 Unauthorized):**
```json
"Invalid credentials"
```

**Frontend Integration:**
```javascript
async function login(credentials) {
  const response = await fetch('/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials)
  });
  
  if (!response.ok) {
    throw new Error('Login failed');
  }
  
  const data = await response.json();
  // Store token in localStorage or secure storage
  localStorage.setItem('authToken', data.token);
  localStorage.setItem('username', data.username);
  
  return data;
}
```

---

#### 3. Logout
```http
POST /api/logout
```

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
"Logged out successfully"
```

**Frontend Integration:**
```javascript
function logout() {
  localStorage.removeItem('authToken');
  localStorage.removeItem('username');
  // Redirect to login page
  window.location.href = '/login';
}
```

---

#### 4. Get Current User
```http
GET /api/me
```

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "username": "john.doe",
  "roles": "USER"
}
```

---

## API Endpoints Reference

### Email Management

#### 1. List Emails (Paginated)
```http
GET /api/emails?page=0&size=20&folderId={folderId}
```

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | integer | No | 0 | Page number (0-indexed) |
| size | integer | No | 20 | Items per page |
| folderId | long | No | null | Filter by folder ID |

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "sender": "alice@example.com",
      "recipient": "john@yourdomain.com",
      "subject": "Hello",
      "body": "Email content here",
      "receivedAt": "2024-11-29T10:30:00",
      "folder": {
        "id": 1,
        "name": "Inbox"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 42,
  "totalPages": 3,
  "last": false,
  "first": true,
  "number": 0,
  "numberOfElements": 20,
  "size": 20,
  "empty": false
}
```

**Frontend Integration:**
```javascript
async function fetchEmails(page = 0, size = 20, folderId = null) {
  const token = localStorage.getItem('authToken');
  let url = `/api/emails?page=${page}&size=${size}`;
  if (folderId) url += `&folderId=${folderId}`;
  
  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return await response.json();
}
```

---

#### 2. Get Single Email
```http
GET /api/emails/{id}
```

**Headers:**
```
Authorization: Bearer <token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | long | Email ID |

**Success Response (200 OK):**
```json
{
  "id": 1,
  "sender": "alice@example.com",
  "recipient": "john@yourdomain.com",
  "subject": "Meeting Tomorrow",
  "body": "Let's discuss the project at 10 AM",
  "receivedAt": "2024-11-29T14:30:00",
  "folder": {
    "id": 1,
    "name": "Inbox"
  },
  "user": {
    "id": 1,
    "username": "john.doe"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "Email not found"
}
```

---

#### 3. Send Email
```http
POST /api/emails
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "to": "recipient@example.com",
  "subject": "Meeting Tomorrow",
  "body": "Let's meet at 10 AM in the conference room."
}
```

**Success Response (201 Created):**
```json
{
  "id": 5,
  "status": "QUEUED",
  "message": "Email queued for delivery"
}
```

**Frontend Integration:**
```javascript
async function sendEmail(emailData) {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch('/api/emails', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(emailData)
  });
  
  if (!response.ok) {
    throw new Error('Failed to send email');
  }
  
  return await response.json();
}

// Usage
sendEmail({
  to: 'recipient@example.com',
  subject: 'Hello',
  body: 'This is a test email'
});
```

---

#### 4. Update Email (Mark as Read/Unread)
```http
PATCH /api/emails/{id}
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "read": true
}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "read": true
}
```

---

#### 5. Delete Email
```http
DELETE /api/emails/{id}
```

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
{
  "message": "Email deleted successfully"
}
```

**Frontend Integration:**
```javascript
async function deleteEmail(emailId) {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch(`/api/emails/${emailId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return response.ok;
}
```

---

#### 6. Move Email to Folder
```http
POST /api/emails/{id}/move?folderId={folderId}
```

**Headers:**
```
Authorization: Bearer <token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| id | long | Email ID |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| folderId | long | Yes | Target folder ID |

**Success Response (200 OK):**
```json
{
  "message": "Email moved successfully"
}
```

**Frontend Integration:**
```javascript
async function moveEmail(emailId, folderId) {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch(`/api/emails/${emailId}/move?folderId=${folderId}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return await response.json();
}
```

---

### Folder Management

#### 1. List All Folders
```http
GET /api/folders
```

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Inbox",
    "parent": null,
    "user": {
      "id": 1,
      "username": "john.doe"
    }
  },
  {
    "id": 2,
    "name": "Sent",
    "parent": null,
    "user": {
      "id": 1,
      "username": "john.doe"
    }
  },
  {
    "id": 3,
    "name": "Work Projects",
    "parent": {
      "id": 1,
      "name": "Inbox"
    },
    "user": {
      "id": 1,
      "username": "john.doe"
    }
  }
]
```

**Frontend Integration:**
```javascript
async function getFolders() {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch('/api/folders', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return await response.json();
}
```

---

#### 2. Create Folder
```http
POST /api/folders
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Work Projects",
  "parentId": 1
}
```

**Field Descriptions:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Folder name |
| parentId | long | No | Parent folder ID (for nested folders) |

**Success Response (200 OK):**
```json
{
  "message": "Folder created successfully"
}
```

**Frontend Integration:**
```javascript
async function createFolder(name, parentId = null) {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch('/api/folders', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ name, parentId })
  });
  
  return await response.json();
}
```

---

#### 3. Update Folder
```http
PATCH /api/folders/{id}
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Updated Folder Name"
}
```

**Success Response (200 OK):**
```json
{
  "message": "Folder updated successfully"
}
```

---

#### 4. Delete Folder
```http
DELETE /api/folders/{id}
```

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
{
  "message": "Folder deleted successfully"
}
```

---

### Settings Management

#### 1. Get User Settings
```http
GET /api/settings
```

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "theme": "dark",
  "notificationsEnabled": true,
  "signature": "Best regards,\nJohn Doe\nSoftware Engineer",
  "user": {
    "id": 1,
    "username": "john.doe"
  }
}
```

---

#### 2. Update Settings
```http
PATCH /api/settings
```

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "theme": "dark",
  "notificationsEnabled": true,
  "signature": "Best regards,\nJohn Doe"
}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "theme": "dark",
  "notificationsEnabled": true,
  "signature": "Best regards,\nJohn Doe",
  "user": {
    "id": 1,
    "username": "john.doe"
  }
}
```

**Frontend Integration:**
```javascript
async function updateSettings(settings) {
  const token = localStorage.getItem('authToken');
  
  const response = await fetch('/api/settings', {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(settings)
  });
  
  return await response.json();
}
```

---

### Admin Operations (Requires ADMIN Role)

#### 1. List All Users
```http
GET /api/admin/users
```

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "username": "john.doe",
    "roles": "USER"
  },
  {
    "id": 2,
    "username": "admin",
    "roles": "ADMIN,USER"
  }
]
```

---

#### 2. Create User (Admin)
```http
POST /api/admin/users
```

**Headers:**
```
Authorization: Bearer <admin_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "newuser",
  "password": "SecurePass123!",
  "roles": "USER"
}
```

**Success Response (200 OK):**
```json
{
  "id": 3,
  "username": "newuser",
  "roles": "USER"
}
```

---

#### 3. Update User (Admin)
```http
PATCH /api/admin/users/{id}
```

**Request Body:**
```json
{
  "username": "updated.username",
  "roles": "ADMIN,USER"
}
```

---

#### 4. Delete User (Admin)
```http
DELETE /api/admin/users/{id}
```

**Success Response (200 OK):**
```json
{
  "message": "User deleted successfully"
}
```

---

#### 5. Get System Statistics
```http
GET /api/admin/stats
```

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Success Response (200 OK):**
```json
{
  "users": 150,
  "emails": 5420,
  "folders": 320,
  "queuedEmails": 5
}
```

---

## Data Models

### User
```typescript
interface User {
  id: number;
  username: string;
  roles: string;  // Comma-separated: "USER" or "ADMIN,USER"
  // password is never returned in API responses
}
```

### Email
```typescript
interface Email {
  id: number;
  sender: string;       // Email address
  recipient: string;    // Email address
  subject: string;
  body: string;         // Plain text or HTML
  receivedAt: string;   // ISO 8601 date-time
  folder?: Folder;
  user?: User;
}
```

### Folder
```typescript
interface Folder {
  id: number;
  name: string;
  parent?: Folder;      // For nested folders
  user?: User;
}
```

### Settings
```typescript
interface Settings {
  id: number;
  theme: string;        // "light" or "dark"
  notificationsEnabled: boolean;
  signature?: string;   // Email signature
  user?: User;
}
```

### Pagination Response
```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  number: number;
  numberOfElements: number;
  size: number;
  empty: boolean;
}
```

### Request DTOs

#### LoginRequest
```typescript
interface LoginRequest {
  username: string;
  password: string;
}
```

#### LoginResponse
```typescript
interface LoginResponse {
  token: string;
  type: string;         // "Bearer"
  username: string;
  roles: string;
}
```

#### EmailRequest
```typescript
interface EmailRequest {
  to: string;          // Recipient email
  subject: string;
  body: string;
}
```

#### FolderRequest
```typescript
interface FolderRequest {
  name: string;
  parentId?: number;   // Optional parent folder ID
}
```

---

## Error Handling

### Error Response Format

```typescript
interface ErrorResponse {
  error: string;
  message?: string;
  status?: number;
}
```

### HTTP Status Codes

| Code | Meaning | When It Occurs |
|------|---------|----------------|
| 200 | OK | Successful request |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Internal Server Error | Server-side error |

### Frontend Error Handling

```javascript
async function apiCall(url, options = {}) {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        'Content-Type': 'application/json',
        ...options.headers
      }
    });
    
    if (response.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('authToken');
      window.location.href = '/login';
      throw new Error('Authentication required');
    }
    
    if (response.status === 403) {
      throw new Error('Insufficient permissions');
    }
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Request failed');
    }
    
    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
```

---

## Integration Examples

### React Integration

#### 1. API Service Layer
```javascript
// services/api.js
const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8095/api';

class ApiService {
  getAuthHeader() {
    const token = localStorage.getItem('authToken');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }

  async request(endpoint, options = {}) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...this.getAuthHeader(),
        ...options.headers
      }
    });

    if (response.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }

    return await response.json();
  }

  // Auth
  async login(credentials) {
    const data = await this.request('/login', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
    localStorage.setItem('authToken', data.token);
    return data;
  }

  async register(credentials) {
    return await this.request('/register', {
      method: 'POST',
      body: JSON.stringify(credentials)
    });
  }

  async getCurrentUser() {
    return await this.request('/me');
  }

  // Emails
  async getEmails(page = 0, size = 20, folderId = null) {
    let url = `/emails?page=${page}&size=${size}`;
    if (folderId) url += `&folderId=${folderId}`;
    return await this.request(url);
  }

  async getEmail(id) {
    return await this.request(`/emails/${id}`);
  }

  async sendEmail(emailData) {
    return await this.request('/emails', {
      method: 'POST',
      body: JSON.stringify(emailData)
    });
  }

  async deleteEmail(id) {
    return await this.request(`/emails/${id}`, { method: 'DELETE' });
  }

  async moveEmail(id, folderId) {
    return await this.request(`/emails/${id}/move?folderId=${folderId}`, {
      method: 'POST'
    });
  }

  // Folders
  async getFolders() {
    return await this.request('/folders');
  }

  async createFolder(folderData) {
    return await this.request('/folders', {
      method: 'POST',
      body: JSON.stringify(folderData)
    });
  }

  async deleteFolder(id) {
    return await this.request(`/folders/${id}`, { method: 'DELETE' });
  }

  // Settings
  async getSettings() {
    return await this.request('/settings');
  }

  async updateSettings(settings) {
    return await this.request('/settings', {
      method: 'PATCH',
      body: JSON.stringify(settings)
    });
  }
}

export default new ApiService();
```

#### 2. React Component Example
```jsx
// components/EmailList.jsx
import React, { useState, useEffect } from 'react';
import api from '../services/api';

function EmailList() {
  const [emails, setEmails] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadEmails();
  }, [page]);

  async function loadEmails() {
    try {
      setLoading(true);
      const data = await api.getEmails(page, 20);
      setEmails(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Failed to load emails:', error);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(emailId) {
    if (!window.confirm('Delete this email?')) return;
    
    try {
      await api.deleteEmail(emailId);
      loadEmails(); // Refresh list
    } catch (error) {
      alert('Failed to delete email');
    }
  }

  if (loading) return <div>Loading...</div>;

  return (
    <div>
      <h2>Emails</h2>
      {emails.map(email => (
        <div key={email.id} className="email-item">
          <h3>{email.subject}</h3>
          <p>From: {email.sender}</p>
          <p>{email.body.substring(0, 100)}...</p>
          <button onClick={() => handleDelete(email.id)}>Delete</button>
        </div>
      ))}
      
      <div className="pagination">
        <button 
          onClick={() => setPage(p => p - 1)} 
          disabled={page === 0}
        >
          Previous
        </button>
        <span>Page {page + 1} of {totalPages}</span>
        <button 
          onClick={() => setPage(p => p + 1)} 
          disabled={page >= totalPages - 1}
        >
          Next
        </button>
      </div>
    </div>
  );
}

export default EmailList;
```

---

### Vue.js Integration

```javascript
// services/emailService.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8095/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add auth token to requests
api.interceptors.request.use(config => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default {
  async login(credentials) {
    const { data } = await api.post('/login', credentials);
    localStorage.setItem('authToken', data.token);
    return data;
  },

  async getEmails(page = 0, size = 20) {
    const { data } = await api.get('/emails', {
      params: { page, size }
    });
    return data;
  },

  async sendEmail(emailData) {
    const { data } = await api.post('/emails', emailData);
    return data;
  }
};
```

---

### Angular Integration

```typescript
// services/email.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

interface Email {
  id: number;
  sender: string;
  recipient: string;
  subject: string;
  body: string;
  receivedAt: string;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
}

@Injectable({
  providedIn: 'root'
})
export class EmailService {
  private apiUrl = 'http://localhost:8095/api';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getEmails(page: number = 0, size: number = 20): Observable<PageResponse<Email>> {
    return this.http.get<PageResponse<Email>>(
      `${this.apiUrl}/emails?page=${page}&size=${size}`,
      { headers: this.getHeaders() }
    );
  }

  sendEmail(email: { to: string; subject: string; body: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/emails`, email, {
      headers: this.getHeaders()
    });
  }

  deleteEmail(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/emails/${id}`, {
      headers: this.getHeaders()
    });
  }
}
```

---

## Frontend Best Practices

### 1. Token Management

```javascript
// Use interceptors to automatically add tokens
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Refresh page on token expiration
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 2. State Management (Redux Example)

```javascript
// store/emailSlice.js
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../services/api';

export const fetchEmails = createAsyncThunk(
  'emails/fetchEmails',
  async ({ page, size, folderId }) => {
    const response = await api.getEmails(page, size, folderId);
    return response;
  }
);

const emailSlice = createSlice({
  name: 'emails',
  initialState: {
    items: [],
    loading: false,
    error: null,
    currentPage: 0,
    totalPages: 0
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchEmails.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchEmails.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload.content;
        state.totalPages = action.payload.totalPages;
      })
      .addCase(fetchEmails.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
      });
  }
});

export default emailSlice.reducer;
```

### 3. Loading States

```jsx
function EmailList() {
  const [state, setState] = useState({
    emails: [],
    loading: true,
    error: null
  });

  useEffect(() => {
    (async () => {
      try {
        setState(s => ({ ...s, loading: true }));
        const data = await api.getEmails();
        setState({ emails: data.content, loading: false, error: null });
      } catch (error) {
        setState({ emails: [], loading: false, error: error.message });
      }
    })();
  }, []);

  if (state.loading) return <Spinner />;
  if (state.error) return <Error message={state.error} />;
  return <EmailTable emails={state.emails} />;
}
```

### 4. Optimistic Updates

```javascript
async function deleteEmail(id) {
  // Immediately update UI
  setEmails(emails.filter(e => e.id !== id));
  
  try {
    await api.deleteEmail(id);
  } catch (error) {
    // Rollback on error
    loadEmails();
    alert('Failed to delete email');
  }
}
```

---

## WebSocket / Real-time Updates

**Note:** Currently, the API does not support WebSocket connections. For real-time updates, implement polling:

```javascript
// Polling example
function useEmailPolling(interval = 30000) {
  const [emails, setEmails] = useState([]);

  useEffect(() => {
    async function fetchEmails() {
      const data = await api.getEmails();
      setEmails(data.content);
    }

    fetchEmails();
    const intervalId = setInterval(fetchEmails, interval);

    return () => clearInterval(intervalId);
  }, [interval]);

  return emails;
}
```

**Future Enhancement:** WebSocket support can be added for:
- New email notifications
- Real-time delivery status
- Folder synchronization

---

## CORS Configuration

The API service should have CORS enabled for frontend development. If you encounter CORS issues:

### Backend Configuration (Already Configured)

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:8080")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### Frontend Proxy (Development)

**React (package.json):**
```json
{
  "proxy": "http://localhost:8095"
}
```

**Vue (vue.config.js):**
```javascript
module.exports = {
  devServer: {
    proxy: 'http://localhost:8095'
  }
};
```

---

## Testing

### Manual API Testing with cURL

```bash
# Register
curl -X POST http://localhost:8095/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8095/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}' \
  | jq -r .token)

# Get emails
curl -X GET http://localhost:8095/api/emails \
  -H "Authorization: Bearer $TOKEN"

# Send email
curl -X POST http://localhost:8095/api/emails \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"to":"user@example.com","subject":"Test","body":"Hello"}'
```

---

## Additional Resources

- **Swagger UI**: `http://localhost:8095/swagger-ui.html`
- **Health Check**: `http://localhost:8095/actuator/health`
- **Metrics**: `http://localhost:8095/actuator/metrics`

---

## Support

For questions or issues:
- **Email**: prabhakar.ms.pal@gmail.com
- **GitHub**: https://github.com/1prabhakarpal/smtp-service/issues

---

**Last Updated**: 2024-11-29  
**API Version**: 0.0.1-SNAPSHOT
