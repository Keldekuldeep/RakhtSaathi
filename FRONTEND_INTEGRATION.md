# Frontend Integration Guide
## Replace Firebase calls with Spring Boot REST API

### Setup Axios in React

```bash
npm install axios
```

### Create src/services/api.js

```javascript
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({ baseURL: API_BASE });

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Handle 401 globally
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/';
    }
    return Promise.reject(err);
  }
);

export default api;
```

---

### AUTH APIs

```javascript
// Register
const register = (email, password, fullName, userType) =>
  api.post('/auth/register', { email, password, fullName, userType });

// Login - saves token to localStorage
const login = async (email, password) => {
  const res = await api.post('/auth/login', { email, password });
  const { token, userId, fullName, userType } = res.data.data;
  localStorage.setItem('token', token);
  localStorage.setItem('userType', userType);
  localStorage.setItem('userId', userId);
  return res.data.data;
};
```

---

### NEEDY APIs

```javascript
// Create needy profile (after registration)
const createNeedyProfile = (city, age, gender, relationToPatient) =>
  api.post('/needy/profile', { city, age, gender, relationToPatient });

// Get my profile
const getNeedyProfile = () => api.get('/needy/profile');

// Create blood request
const createBloodRequest = (data) => api.post('/requests', data);

// Get request status
const getRequestStatus = (id) => api.get(`/requests/${id}`);

// Get my requests (history)
const getMyRequests = (page = 0, size = 10) =>
  api.get(`/requests/my?page=${page}&size=${size}`);

// Cancel request
const cancelRequest = (id) => api.put(`/requests/${id}/cancel`);

// Mark as fulfilled
const fulfillRequest = (id) => api.put(`/requests/${id}/fulfill`);

// Submit feedback
const submitFeedback = (requestId, rating, comment) =>
  api.post('/feedback', { requestId, rating, comment });
```

---

### DONOR APIs

```javascript
// Create donor profile
const createDonorProfile = (data) => api.post('/donor/profile', data);

// Get my notifications (blood requests)
const getMyNotifications = (page = 0) =>
  api.get(`/donor/notifications?page=${page}`);

// Accept request
const acceptRequest = (requestId) =>
  api.put(`/donor/requests/${requestId}/accept`);

// Reject request
const rejectRequest = (requestId) =>
  api.put(`/donor/requests/${requestId}/reject`);

// Donation history
const getDonationHistory = () => api.get('/donor/history');
```

---

### ADMIN APIs

```javascript
// Dashboard stats
const getAdminDashboard = () => api.get('/admin/dashboard');

// All requests
const getAllRequests = (page = 0) =>
  api.get(`/admin/requests?page=${page}`);

// Update request status
const updateRequestStatus = (id, status) =>
  api.put(`/admin/requests/${id}/status?status=${status}`);

// All donors
const getAllDonors = (page = 0) =>
  api.get(`/admin/donors?page=${page}`);

// Toggle user active/inactive
const toggleUserStatus = (userId) =>
  api.put(`/admin/users/${userId}/toggle-status`);
```

---

### ALL API ENDPOINTS SUMMARY

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | Public | Register user |
| POST | /api/auth/login | Public | Login, get JWT |
| POST | /api/needy/profile | NEEDY | Create profile |
| GET | /api/needy/profile | NEEDY | Get my profile |
| PUT | /api/needy/profile | NEEDY | Update profile |
| POST | /api/requests | NEEDY | Create blood request |
| GET | /api/requests/{id} | Any | Get request by ID |
| GET | /api/requests/my | NEEDY | My requests |
| PUT | /api/requests/{id}/cancel | NEEDY | Cancel request |
| PUT | /api/requests/{id}/fulfill | NEEDY | Mark fulfilled |
| POST | /api/feedback | Any | Submit feedback |
| POST | /api/donor/profile | DONOR | Create profile |
| GET | /api/donor/profile | DONOR | Get my profile |
| PUT | /api/donor/profile | DONOR | Update profile |
| GET | /api/donor/notifications | DONOR | My blood requests |
| PUT | /api/donor/requests/{id}/accept | DONOR | Accept request |
| PUT | /api/donor/requests/{id}/reject | DONOR | Reject request |
| PUT | /api/donor/requests/{id}/complete | DONOR | Mark donated |
| GET | /api/donor/history | DONOR | Donation history |
| GET | /api/admin/dashboard | ADMIN | Stats |
| GET | /api/admin/requests | ADMIN | All requests |
| PUT | /api/admin/requests/{id}/status | ADMIN | Update status |
| GET | /api/admin/users | ADMIN | All users |
| PUT | /api/admin/users/{id}/toggle-status | ADMIN | Block/unblock |
| GET | /api/admin/donors | ADMIN | All donors |
| GET | /api/admin/feedback | ADMIN | All feedback |
