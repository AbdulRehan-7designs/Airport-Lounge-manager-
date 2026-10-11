# Airport Lounge Manager - Pages List

## ✅ Pages Already Created (Implemented)

### Public Pages
1. **index.jsp** (`/`)
   - Home/Landing page
   - Links to login and connection status
   - Status: ✅ Complete

2. **login.jsp** (`/login`)
   - User authentication form
   - Status: ✅ Complete (with hashed password support)

3. **status.jsp** (`/status.jsp`)
   - Connection status page (Tomcat & SQL)
   - Shows database connection info
   - Status: ✅ Complete

4. **error.jsp** (`/WEB-INF/views/error.jsp`)
   - Error page (404, 500, etc.)
   - Status: ✅ Complete

### Protected Pages (Require Login)
5. **dashboard.jsp** (`/dashboard`)
   - Main dashboard with stats (capacity, pending requests, revenue)
   - Status: ✅ UI Complete (stats are hardcoded - needs backend wiring)

6. **passenger-list.jsp** (`/passengers`)
   - List of passengers
   - Status: ⚠️ UI Complete (shows mock data - needs DB integration)

7. **access-request.jsp** (`/access-requests`)
   - List of access requests
   - Status: ⚠️ UI Complete (shows mock data - needs DB integration + approve/reject actions)

### Debug/Utility
8. **login-debug.jsp** (`/login-debug.jsp`)
   - Debug page for login troubleshooting
   - Status: ✅ Complete (remove in production)

---

## 🔧 Servlets Already Created

1. **LoginServlet** (`/login`) - ✅ Complete
2. **LogoutServlet** (`/logout`) - ✅ Complete
3. **DashboardServlet** (`/dashboard`) - ✅ Complete
4. **PassengerServlet** (`/passengers`) - ⚠️ Needs DB integration
5. **AccessRequestServlet** (`/access-requests`) - ⚠️ Needs DB integration + actions

---

## ❌ Pages That Need to Be Created

### Core Management Pages

#### 1. Airport Management
- **airport-list.jsp** (`/airports`)
  - List all airports (from `airports` table)
  - Show: name, IATA code, city, country, status
  - Actions: View, Edit, Add New, Toggle Active
- **airport-form.jsp** (`/airports/add`, `/airports/edit`)
  - Form to add/edit airport
  - Fields: name, IATA code, city, country
- **airport-view.jsp** (`/airports/view?id=`)
  - View airport details
  - Show lounges at this airport
- **AirportServlet** - Handle CRUD operations

#### 2. Lounge Management
- **lounge-list.jsp** (`/lounges`)
  - List all lounges (from `lounges` table)
  - Show: name, airport, terminal, capacity, current occupancy, status
  - Filter by airport
  - Actions: View, Edit, Add New, Toggle Status
- **lounge-form.jsp** (`/lounges/add`, `/lounges/edit`)
  - Form to add/edit lounge
  - Fields: name, airport (dropdown), terminal, capacity, status
- **lounge-view.jsp** (`/lounges/view?id=`)
  - View lounge details
  - Show current occupancy, recent access requests
- **LoungeServlet** - Handle CRUD operations

#### 3. Passenger Management (Enhance Existing)
- **passenger-form.jsp** (`/passengers/add`, `/passengers/edit`)
  - Form to add/edit passenger
  - Fields: full name, passport number, nationality, phone, email
- **passenger-view.jsp** (`/passengers/view?id=`)
  - View passenger details
  - Show: memberships, access history, transactions
- **passenger-list.jsp** - ⚠️ **Wire to DB** (currently shows mock data)
- **PassengerServlet** - ⚠️ **Add CRUD operations** (currently only list)

#### 4. Access Provider Management
- **provider-list.jsp** (`/access-providers`)
  - List all access providers (from `access_providers` table)
  - Show: name, type (AIRLINE/CREDIT_CARD/AGGREGATOR), status
  - Actions: View, Edit, Add New, Toggle Active
- **provider-form.jsp** (`/access-providers/add`, `/access-providers/edit`)
  - Form to add/edit provider
  - Fields: name, type (dropdown), active status
- **AccessProviderServlet** - Handle CRUD operations

#### 5. Access Request Management (Enhance Existing)
- **access-request.jsp** - ⚠️ **Wire to DB** (currently shows mock data)
- **access-request-view.jsp** (`/access-requests/view?id=`)
  - View request details
  - Show: passenger info, lounge info, provider, status, decision reason
- **AccessRequestServlet** - ⚠️ **Add approve/reject actions** (currently only list)
  - POST `/access-requests/approve` - Approve request
  - POST `/access-requests/reject` - Reject request with reason

#### 6. Staff Management
- **staff-list.jsp** (`/staff`)
  - List all staff (from `staff` table)
  - Show: name, email, role, airport, status
  - Filter by airport or role
  - Actions: View, Edit, Add New, Toggle Active
- **staff-form.jsp** (`/staff/add`, `/staff/edit`)
  - Form to add/edit staff
  - Fields: full name, email, role (dropdown), airport (dropdown), password
- **staff-view.jsp** (`/staff/view?id=`)
  - View staff details
  - Show: audit logs by this staff member
- **StaffServlet** - Handle CRUD operations

#### 7. Transaction Management
- **transaction-list.jsp** (`/transactions`)
  - List all transactions (from `transactions` table)
  - Show: request ID, amount, currency, payment method, status, date
  - Filter by date range, status, payment method
  - Actions: View Details, Refund
- **transaction-view.jsp** (`/transactions/view?id=`)
  - View transaction details
  - Show: related access request, payment info, refund history
- **TransactionServlet** - Handle list, view, refund operations

#### 8. Occupancy Management
- **occupancy-dashboard.jsp** (`/occupancy`)
  - Real-time occupancy dashboard
  - Show: current occupancy per lounge, capacity, percentage
  - Charts/graphs for occupancy trends
- **occupancy-history.jsp** (`/occupancy/history`)
  - Historical occupancy logs (from `occupancy_logs` table)
  - Filter by lounge, date range
- **OccupancyServlet** - Handle occupancy data retrieval

#### 9. Passenger Membership Management
- **membership-list.jsp** (`/memberships`)
  - List passenger memberships (from `passenger_memberships` table)
  - Show: passenger, provider, tier, validity dates, remaining visits, status
  - Filter by provider, status, passenger
- **membership-form.jsp** (`/memberships/add`, `/memberships/edit`)
  - Form to add/edit membership
  - Fields: passenger (dropdown), provider (dropdown), tier, valid from/to, remaining visits
- **MembershipServlet** - Handle CRUD operations

#### 10. Audit Logs
- **audit-log-list.jsp** (`/audit-logs`)
  - List audit logs (from `audit_logs` table)
  - Show: staff member, action type, entity, description, timestamp
  - Filter by staff, action type, date range, entity
  - Actions: View Details
- **AuditLogServlet** - Handle log retrieval (read-only)

#### 11. User Management (Admin Only)
- **user-list.jsp** (`/users`)
  - List all users (from `users` table)
  - Show: username, email, role, active status
  - Actions: View, Edit, Add New, Toggle Active, Reset Password
- **user-form.jsp** (`/users/add`, `/users/edit`)
  - Form to add/edit user
  - Fields: username, email, password, role (dropdown), active
- **UserServlet** - Handle CRUD operations (extend existing UserDAO)

---

## 📊 Reports & Analytics Pages

1. **reports-dashboard.jsp** (`/reports`)
   - Reports overview
   - Links to various report pages

2. **revenue-report.jsp** (`/reports/revenue`)
   - Revenue by date range, lounge, provider
   - Charts and totals

3. **occupancy-report.jsp** (`/reports/occupancy`)
   - Occupancy trends, peak hours analysis

4. **access-report.jsp** (`/reports/access`)
   - Access requests by status, provider, lounge
   - Approval/rejection rates

---

## 🔐 Security & Settings Pages

1. **profile.jsp** (`/profile`)
   - Current user's profile
   - Change password, update email

2. **settings.jsp** (`/settings`)
   - Application settings (admin only)
   - System configuration

---

## 📝 Summary

### ✅ Completed: 8 pages
- Home, Login, Dashboard, Passengers (list), Access Requests (list), Error, Status, Login Debug

### ⚠️ Needs Backend Integration: 2 pages
- Passengers list, Access Requests list

### ❌ To Be Created: ~30+ pages
- Airport management (4 pages)
- Lounge management (4 pages)
- Passenger management (2 pages - form & view)
- Access Provider management (2 pages)
- Access Request management (1 page - view)
- Staff management (3 pages)
- Transaction management (2 pages)
- Occupancy management (2 pages)
- Membership management (2 pages)
- Audit logs (1 page)
- User management (2 pages)
- Reports (4 pages)
- Profile & Settings (2 pages)

### 🔧 Servlets Needed: ~15 servlets
- AirportServlet, LoungeServlet, AccessProviderServlet, StaffServlet, TransactionServlet, OccupancyServlet, MembershipServlet, AuditLogServlet, UserServlet, ReportServlet, ProfileServlet, SettingsServlet
- Plus enhance: PassengerServlet, AccessRequestServlet

---

## 🎯 Priority Order (Recommended)

### Phase 1: Core CRUD (Essential)
1. Airport Management (list, form, view)
2. Lounge Management (list, form, view)
3. Passenger Management (form, view, wire list to DB)
4. Access Request Management (wire list to DB, add approve/reject)

### Phase 2: Supporting Features
5. Access Provider Management
6. Staff Management
7. Transaction Management
8. Occupancy Dashboard

### Phase 3: Advanced Features
9. Membership Management
10. Audit Logs
11. Reports & Analytics
12. User Management
13. Profile & Settings

---

## 📌 Notes

- All pages should use the existing dark theme CSS (`assets/css/style.css`)
- All protected pages need session check (or use AuthFilter)
- Forms should include validation (client-side + server-side)
- Consider pagination for list pages
- Add search/filter functionality to list pages
- Use DAOs for database access (create missing DAOs)
- Wire dashboard stats to real data from services
