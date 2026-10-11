# Requirements Document
## Airport Lounge Manager

**Version:** 1.0  
**Date:** February 2026  
**Document Type:** Functional & Non-Functional Requirements

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Functional Requirements](#2-functional-requirements)
3. [Non-Functional Requirements](#3-non-functional-requirements)
4. [User Stories](#4-user-stories)
5. [Acceptance Criteria](#5-acceptance-criteria)
6. [Data Requirements](#6-data-requirements)
7. [Interface Requirements](#7-interface-requirements)
8. [Security Requirements](#8-security-requirements)

---

## 1. Introduction

### 1.1 Purpose
This document specifies the functional and non-functional requirements for the Airport Lounge Manager (ALM) web application.

### 1.2 Scope
The application manages airport lounge operations including passenger tracking, access requests, transactions, occupancy monitoring, and reporting.

### 1.3 Definitions & Acronyms
- **ALM:** Airport Lounge Manager
- **CRUD:** Create, Read, Update, Delete
- **IATA:** International Air Transport Association (airport codes)
- **RBAC:** Role-Based Access Control
- **XSS:** Cross-Site Scripting
- **CSRF:** Cross-Site Request Forgery

---

## 2. Functional Requirements

### 2.1 Authentication & Authorization

#### REQ-AUTH-001: User Login
- **ID:** REQ-AUTH-001
- **Description:** Users must be able to log in with username and password
- **Priority:** High
- **Status:** ✅ Implemented
- **Acceptance Criteria:**
  - Login form accepts username and password
  - Valid credentials grant access
  - Invalid credentials show error message
  - Session is created upon successful login

#### REQ-AUTH-002: Password Security
- **ID:** REQ-AUTH-002
- **Description:** Passwords must be securely stored using hashing
- **Priority:** High
- **Status:** ✅ Implemented (SHA-256 + Base64)
- **Acceptance Criteria:**
  - Passwords stored as hash, never plain text
  - Password verification compares hash
  - Minimum password length: 6 characters (future)

#### REQ-AUTH-003: Session Management
- **ID:** REQ-AUTH-003
- **Description:** System must manage user sessions
- **Priority:** High
- **Status:** ✅ Implemented (partial - no timeout)
- **Acceptance Criteria:**
  - Session created on login
  - Session invalidated on logout
  - Session timeout after 30 minutes inactivity (future)

#### REQ-AUTH-004: Role-Based Access
- **ID:** REQ-AUTH-004
- **Description:** Different user roles have different permissions
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Roles:**
  - ADMIN: Full access
  - OPERATIONS_MANAGER: Most features, user management excluded
  - RECEPTION: Passenger and access request operations only
  - SUPERVISOR: Read access, limited write

---

### 2.2 Airport Management

#### REQ-AIRPORT-001: List Airports
- **ID:** REQ-AIRPORT-001
- **Description:** Display list of all airports
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Fields:** Name, IATA Code, City, Country, Status
- **Features:** Search, filter by country, pagination

#### REQ-AIRPORT-002: Add Airport
- **ID:** REQ-AIRPORT-002
- **Description:** Create new airport record
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Required Fields:** Name, IATA Code (3 letters), City, Country
- **Validation:** IATA code must be unique, 3 uppercase letters

#### REQ-AIRPORT-003: Edit Airport
- **ID:** REQ-AIRPORT-003
- **Description:** Update existing airport details
- **Priority:** High
- **Status:** ❌ Not Implemented

#### REQ-AIRPORT-004: View Airport
- **ID:** REQ-AIRPORT-004
- **Description:** View airport details with associated lounges
- **Priority:** Medium
- **Status:** ❌ Not Implemented

---

### 2.3 Lounge Management

#### REQ-LOUNGE-001: List Lounges
- **ID:** REQ-LOUNGE-001
- **Description:** Display list of all lounges
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Fields:** Name, Airport, Terminal, Capacity, Current Occupancy, Status
- **Features:** Filter by airport, search, pagination

#### REQ-LOUNGE-002: Add Lounge
- **ID:** REQ-LOUNGE-002
- **Description:** Create new lounge record
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Required Fields:** Name, Airport (FK), Terminal, Capacity
- **Validation:** Capacity > 0, airport must exist

#### REQ-LOUNGE-003: Edit Lounge
- **ID:** REQ-LOUNGE-003
- **Description:** Update lounge details
- **Priority:** High
- **Status:** ❌ Not Implemented

#### REQ-LOUNGE-004: View Lounge
- **ID:** REQ-LOUNGE-004
- **Description:** View lounge details with occupancy info
- **Priority:** Medium
- **Status:** ❌ Not Implemented

#### REQ-LOUNGE-005: Update Lounge Status
- **ID:** REQ-LOUNGE-005
- **Description:** Change lounge status (ACTIVE, CLOSED, MAINTENANCE)
- **Priority:** Medium
- **Status:** ❌ Not Implemented

---

### 2.4 Passenger Management

#### REQ-PASSENGER-001: List Passengers
- **ID:** REQ-PASSENGER-001
- **Description:** Display list of passengers
- **Priority:** High
- **Status:** ⚠️ Partial (UI exists, mock data)
- **Fields:** ID, Name, Passport No., Flight No., Status
- **Features:** Search by name/passport, filter by status, pagination

#### REQ-PASSENGER-002: Add Passenger
- **ID:** REQ-PASSENGER-002
- **Description:** Create new passenger record
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Required Fields:** Full Name, Passport Number
- **Optional Fields:** Nationality, Phone, Email
- **Validation:** Passport number must be unique

#### REQ-PASSENGER-003: Edit Passenger
- **ID:** REQ-PASSENGER-003
- **Description:** Update passenger details
- **Priority:** High
- **Status:** ❌ Not Implemented

#### REQ-PASSENGER-004: View Passenger
- **ID:** REQ-PASSENGER-004
- **Description:** View passenger profile with history
- **Priority:** Medium
- **Status:** ❌ Not Implemented
- **Shows:** Details, memberships, access history, transactions

---

### 2.5 Access Request Management

#### REQ-REQUEST-001: List Access Requests
- **ID:** REQ-REQUEST-001
- **Description:** Display list of access requests
- **Priority:** High
- **Status:** ⚠️ Partial (UI exists, mock data)
- **Fields:** Request ID, Passenger, Lounge, Time, Status
- **Features:** Filter by status, date range, lounge, pagination

#### REQ-REQUEST-002: View Access Request
- **ID:** REQ-REQUEST-002
- **Description:** View detailed access request information
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Shows:** Passenger details, lounge info, provider, status, decision reason

#### REQ-REQUEST-003: Approve Request
- **ID:** REQ-REQUEST-003
- **Description:** Approve an access request
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Actions:** Update status to APPROVED, record decision time, log audit

#### REQ-REQUEST-004: Reject Request
- **ID:** REQ-REQUEST-004
- **Description:** Reject an access request with reason
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Actions:** Update status to DENIED, record reason and decision time, log audit

---

### 2.6 Transaction Management

#### REQ-TRANS-001: List Transactions
- **ID:** REQ-TRANS-001
- **Description:** Display list of transactions
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Fields:** Transaction ID, Request ID, Amount, Currency, Method, Status, Date
- **Features:** Filter by date range, status, payment method

#### REQ-TRANS-002: Process Payment
- **ID:** REQ-TRANS-002
- **Description:** Process payment for access request
- **Priority:** High
- **Status:** ❌ Not Implemented
- **Payment Methods:** CARD, UPI, CASH
- **Actions:** Create transaction record, update request status

#### REQ-TRANS-003: Issue Refund
- **ID:** REQ-TRANS-003
- **Description:** Process refund for transaction
- **Priority:** Medium
- **Status:** ❌ Not Implemented
- **Actions:** Update transaction status to REFUNDED, create refund record

---

### 2.7 Dashboard & Analytics

#### REQ-DASH-001: Dashboard Display
- **ID:** REQ-DASH-001
- **Description:** Show main dashboard with key metrics
- **Priority:** High
- **Status:** ⚠️ Partial (UI exists, stats hardcoded)
- **Metrics:**
  - Current lounge capacity percentage
  - Pending access requests count
  - Daily revenue
  - Occupancy trends (future)

#### REQ-DASH-002: Real-time Updates
- **ID:** REQ-DASH-002
- **Description:** Dashboard should update automatically
- **Priority:** Medium
- **Status:** ❌ Not Implemented
- **Update Frequency:** Every 30 seconds

---

### 2.8 Reporting

#### REQ-REPORT-001: Revenue Reports
- **ID:** REQ-REPORT-001
- **Description:** Generate revenue reports
- **Priority:** Medium
- **Status:** ❌ Not Implemented
- **Filters:** Date range, lounge, payment method
- **Export:** CSV, PDF

#### REQ-REPORT-002: Occupancy Reports
- **ID:** REQ-REPORT-002
- **Description:** Generate occupancy reports
- **Priority:** Medium
- **Status:** ❌ Not Implemented
- **Shows:** Occupancy trends, peak hours, capacity utilization

---

## 3. Non-Functional Requirements

### 3.1 Performance Requirements

#### REQ-PERF-001: Page Load Time
- **ID:** REQ-PERF-001
- **Description:** Pages must load within 2 seconds
- **Priority:** High
- **Measurement:** Time to first byte + render

#### REQ-PERF-002: Database Query Performance
- **ID:** REQ-PERF-002
- **Description:** Database queries must complete within 500ms
- **Priority:** High
- **Measurement:** Query execution time

#### REQ-PERF-003: Concurrent Users
- **ID:** REQ-PERF-003
- **Description:** Support at least 100 concurrent users
- **Priority:** Medium
- **Measurement:** Load testing

---

### 3.2 Security Requirements

#### REQ-SEC-001: Password Security
- **ID:** REQ-SEC-001
- **Description:** Passwords must be hashed using SHA-256
- **Priority:** High
- **Status:** ✅ Implemented

#### REQ-SEC-002: SQL Injection Prevention
- **ID:** REQ-SEC-002
- **Description:** All database queries must use PreparedStatement
- **Priority:** High
- **Status:** ✅ Implemented (in UserDAO)

#### REQ-SEC-003: XSS Prevention
- **ID:** REQ-SEC-003
- **Description:** All user input must be encoded before display
- **Priority:** High
- **Status:** ❌ Not Implemented

#### REQ-SEC-004: CSRF Protection
- **ID:** REQ-SEC-004
- **Description:** Forms must include CSRF tokens
- **Priority:** High
- **Status:** ❌ Not Implemented

#### REQ-SEC-005: Session Security
- **ID:** REQ-SEC-005
- **Description:** Sessions must be secure and HTTP-only
- **Priority:** High
- **Status:** ⚠️ Partial (basic session, not HTTP-only)

---

### 3.3 Usability Requirements

#### REQ-USE-001: Responsive Design
- **ID:** REQ-USE-001
- **Description:** Application must work on desktop, tablet, mobile
- **Priority:** High
- **Status:** ⚠️ Partial (desktop OK, mobile not optimized)

#### REQ-USE-002: Navigation
- **ID:** REQ-USE-002
- **Description:** Navigation must be intuitive and consistent
- **Priority:** High
- **Status:** ✅ Implemented

#### REQ-USE-003: Error Messages
- **ID:** REQ-USE-003
- **Description:** Error messages must be clear and actionable
- **Priority:** Medium
- **Status:** ⚠️ Partial (basic messages)

---

### 3.4 Reliability Requirements

#### REQ-REL-001: System Availability
- **ID:** REQ-REL-001
- **Description:** System must be available 99.9% of the time
- **Priority:** High
- **Uptime Target:** 99.9%

#### REQ-REL-002: Error Handling
- **ID:** REQ-REL-002
- **Description:** System must handle errors gracefully
- **Priority:** High
- **Status:** ⚠️ Partial (basic error page)

#### REQ-REL-003: Data Backup
- **ID:** REQ-REL-003
- **Description:** Database must be backed up daily
- **Priority:** High
- **Status:** ❌ Not Implemented (infrastructure)

---

### 3.5 Maintainability Requirements

#### REQ-MAIN-001: Code Documentation
- **ID:** REQ-MAIN-001
- **Description:** Code must be well-documented
- **Priority:** Medium
- **Status:** ⚠️ Partial

#### REQ-MAIN-002: Test Coverage
- **ID:** REQ-MAIN-002
- **Description:** Unit test coverage must be > 70%
- **Priority:** Medium
- **Status:** ⚠️ Partial (limited tests)

---

## 4. User Stories

### Story 1: As an Admin, I want to manage airports
- **Priority:** High
- **Acceptance:** Can add, edit, view, and deactivate airports
- **Status:** ❌ Not Implemented

### Story 2: As a Reception Staff, I want to approve access requests
- **Priority:** High
- **Acceptance:** Can view requests, approve or reject with reason
- **Status:** ❌ Not Implemented

### Story 3: As an Operations Manager, I want to view dashboard metrics
- **Priority:** High
- **Acceptance:** Dashboard shows real-time capacity, requests, revenue
- **Status:** ⚠️ Partial (UI exists, stats hardcoded)

### Story 4: As a Supervisor, I want to generate occupancy reports
- **Priority:** Medium
- **Acceptance:** Can generate and export occupancy reports
- **Status:** ❌ Not Implemented

---

## 5. Acceptance Criteria

### AC-001: Login Functionality
- ✅ User can log in with valid credentials
- ✅ User cannot log in with invalid credentials
- ✅ Session is created on successful login
- ✅ User is redirected to dashboard after login

### AC-002: Dashboard Display
- ✅ Dashboard shows after login
- ✅ Dashboard displays user's username
- ⚠️ Stats are displayed (currently hardcoded)
- ❌ Stats update from database (future)

### AC-003: Access Request Approval
- ❌ User can view access request details
- ❌ User can approve request
- ❌ User can reject request with reason
- ❌ Status updates in database
- ❌ Audit log is created

---

## 6. Data Requirements

### 6.1 Data Entities

1. **User** - id, username, email, password, role, active
2. **Airport** - airport_id, name, iata_code, city, country, is_active
3. **Lounge** - lounge_id, airport_id, name, terminal, capacity, status
4. **Passenger** - passenger_id, full_name, passport_number, nationality, phone, email
5. **Access Provider** - provider_id, provider_name, provider_type
6. **Access Request** - request_id, passenger_id, lounge_id, provider_id, status, decision_reason
7. **Transaction** - transaction_id, request_id, amount, currency, payment_method, payment_status
8. **Staff** - staff_id, airport_id, full_name, email, role, password_hash
9. **Membership** - membership_id, passenger_id, provider_id, tier, valid_from, valid_to
10. **Occupancy Log** - log_id, lounge_id, current_occupancy, recorded_at
11. **Audit Log** - audit_id, staff_id, action_type, entity_name, entity_id, description

### 6.2 Data Validation Rules

- IATA codes: 3 uppercase letters, unique
- Passport numbers: Unique per passenger
- Capacity: Positive integer
- Email: Valid email format
- Amounts: Positive decimal, 2 decimal places
- Dates: Valid date format, logical ranges

---

## 7. Interface Requirements

### 7.1 User Interface

- **Theme:** Dark theme (consistent across all pages)
- **Layout:** Responsive, card-based
- **Navigation:** Header menu with links
- **Forms:** Clear labels, validation messages
- **Tables:** Sortable, filterable, paginated

### 7.2 Browser Compatibility

- Chrome (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Edge (latest 2 versions)

---

## 8. Security Requirements

### 8.1 Authentication
- ✅ Password hashing (SHA-256)
- ✅ Session management
- ❌ Password complexity rules (future)
- ❌ Account lockout (future)

### 8.2 Authorization
- ❌ Role-based access control
- ❌ Page-level permissions
- ❌ Action-level permissions

### 8.3 Data Protection
- ✅ SQL injection prevention (PreparedStatement)
- ❌ XSS prevention (output encoding)
- ❌ CSRF protection
- ❌ Input validation

### 8.4 Audit & Compliance
- ❌ Audit logging for all actions
- ❌ Data retention policies
- ❌ Privacy compliance (GDPR, etc.)

---

## 9. Requirements Traceability

### Implementation Status Legend
- ✅ **Implemented** - Feature is complete and working
- ⚠️ **Partial** - Feature exists but incomplete or using mock data
- ❌ **Not Implemented** - Feature not yet developed

### Priority Legend
- **High** - Critical for MVP
- **Medium** - Important but not blocking
- **Low** - Nice to have, future enhancement

---

## 10. Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Feb 2026 | Initial requirements document |

---

**Document Version:** 1.0  
**Last Updated:** February 2026  
**Owner:** Development Team
