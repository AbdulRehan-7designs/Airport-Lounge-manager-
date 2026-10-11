# Roles and Access

This document describes the **separate login** and **role-based process** for Admin, Manager, and Staff (e.g. Reception) in the Airport Lounge Manager.

## Single login form, role from database

- **One login URL:** `/login`
- **Role** is read from `users.role` after successful authentication.
- After login, **redirect** depends on role:
  - **RECEPTION** → `/access-requests`
  - **PASSENGER** → `/profile`
  - **ADMIN**, **OPERATIONS_MANAGER**, **SUPERVISOR** → `/dashboard`

## Roles

| Role (DB value)   | Description        | Typical use                    |
|-------------------|--------------------|--------------------------------|
| `ADMIN`           | System administrator | Full access, user management |
| `OPERATIONS_MANAGER` | Manager          | Lounges, access providers, reports |
| `SUPERVISOR`      | Supervisor         | Same landing as manager       |
| `RECEPTION`       | Receptionist / staff | Day-to-day access requests   |
| `PASSENGER`       | Registered user    | Sign in, register, edit profile |

## Role-wise process (what each role can do)

### Admin

- **Landing:** Dashboard
- **Can access:** Dashboard, Passengers, Access Requests, Reports, **Users**
- **Process:** Full application access; manage users and settings; view all reports and data.

### Manager (Operations Manager / Supervisor)

- **Landing:** Dashboard
- **Can access:** Dashboard, Passengers, Access Requests, **Lounges**, **Access Providers**, Reports
- **Cannot access:** Users, Settings (admin-only)
- **Process:** Check lounges (what each has, capacity); add items (e.g. chair availability); manage access providers (airlines, credit cards, aggregators); run reports.

### Staff (Reception)

- **Landing:** Access Requests (for quick handling)
- **Can access:** Dashboard, Passengers, Access Requests
- **Cannot access:** Reports, Lounges, Access Providers, Users, Settings
- **Process:** Check in passengers; create and process access requests; view passenger list and dashboard.

### Passenger (registered user)

- **Landing:** Profile (after sign-in)
- **Can access:** Dashboard, **My Profile** (edit details and preferences)
- **Process:** Sign in; register as user; edit profile (username, email).

## URL protection (AuthFilter)

- **Excluded (no login required):** `/login`, `/logout`, `/register`, `/`, `/index.jsp`, `/status.jsp`, `/assets/*`
- **Protected:** All other paths require a valid session. If not logged in → redirect to `/login`.
- **Admin-only paths:** `/users`, `/settings` → only `ADMIN`; others get 403.
- **Manager-only paths:** `/airports`, `/staff`, `/reports`, `/lounges`, `/access-providers` → `ADMIN` or `OPERATIONS_MANAGER`; others get 403.

## Navigation

- **Header nav** is role-based (see `WEB-INF/includes/nav-role.jsp`):
  - **Passenger:** Dashboard, My Profile, Logout
  - **Staff/Manager/Admin:** Dashboard, Passengers, Access Requests, Lounges (manager+), Access Providers (manager+), Reports (manager+), Users (admin only), Logout

## Database

- Store role in `users.role` as one of: `ADMIN`, `OPERATIONS_MANAGER`, `SUPERVISOR`, `RECEPTION`, `PASSENGER`.
- **Registration** creates a user with role `PASSENGER` (hashed password). Use **Register** on the login page.
- Example:

```sql
UPDATE alm.users SET role = 'ADMIN' WHERE username = 'admin';
INSERT INTO alm.users (username, email, password, role) VALUES ('manager1', 'm@example.com', '<hash>', 'OPERATIONS_MANAGER');
INSERT INTO alm.users (username, email, password, role) VALUES ('reception1', 'r@example.com', '<hash>', 'RECEPTION');
-- Passengers register via /register; role is set to PASSENGER automatically.
```

## Summary

- **Separate login:** No — one form; role comes from DB.
- **Role-wise process:** Yes — redirect after login, role-based nav, and AuthFilter enforce access by role.
