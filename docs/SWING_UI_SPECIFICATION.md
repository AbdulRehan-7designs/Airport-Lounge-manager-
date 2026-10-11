# Swing UI Specification: Airport Lounge Manager

This document outlines the architecture and screen hierarchy for the Java Swing implementation of the Airport Lounge Manager. This system is designed for high-performance, real-time desktop usage with a premium modern aesthetic.

---

## 🏗️ 1. Layered UI Architecture

The UI is built using a decoupled MVC-style architecture in Swing to ensure scalability and easier maintenance.

### **Layer 1: Core (Infrastructure)**
- **`com.alm.ui.core.MainAppWindow`**: The primary `JFrame`. Handles the Window-level operations (close, resize) and hosts the `MainShell`.
- **`com.alm.ui.core.NavigationManager`**: Singleton service that manages the `CardLayout` transitions between panels.
- **`com.alm.ui.core.SessionContext`**: Holds the current logged-in `User` state and handles permission-based logic for UI components.
- **`com.alm.ui.core.ThemeManager`**: Configures FlatLaf, custom colors, fonts (Inter/Roboto), and rendering hints (Anti-aliasing).

### **Layer 2: Components (Reusable Widgets)**
- **`GCard`**: Glassmorphic panel with subtle borders and glow.
- **`GButton`**: Custom styled buttons with hover animations.
- **`GTextField / GPasswordField`**: Modern input fields with focus indicators.
- **`GTable`**: Custom JTable renderer with zebra-striping and hover states.
- **`GNotificationPop`**: Toast-style notifications for real-time feedback.

### **Layer 3: Shell (The App Layout)**
- **`com.alm.ui.shell.AppShell`**: A base `JPanel` containing:
    - `SidebarPanel` (Persistent Navigation)
    - `TopbarPanel` (Title, User, Live Clock)
    - `ContentArea` (The Dynamic switching panel)

### **Layer 4: Panels (Specific Views)**
- These are the actual screens, e.g., `DashboardPanel`, `LoungeListPanel`. They communicate with `Service` classes for data.

---

## 👥 2. Role-Wise Screen Access

Each role sees a different sidebar and dashboard content based on their `Permission` set.

| Role | Primary Responsibility | Key Screens |
|---|---|---|
| **ADMIN** | System Architecture | User Mgmt, Provider Config, System Settings |
| **MANAGER** | Business Oversight | Financial Reports, Shift Mgmt, Transaction Audits |
| **SUPERVISOR** | Operational Flow | Amenity Inventory, Capacity Monitor, Dispute Overrides |
| **RECEPTION** | Guest Experience | Check-in Queue, Passenger Search, Fast-Entry |
| **PASSENGER** | Self-Service | Lounge Browser, Eligibility Check, Visit History |

---

## 🖥️ 3. Screen Inventory (The Pages)

### **3.1. Public / Auth Screens**
1.  **`LoginPanel`**: Centered card with Username/Password. Features "System Check" indicators (DB status).
2.  **`ConnectionStatusPanel`**: Technical diagnostic screen (useful for support).

### **3.2. Shared Foundation**
1.  **`DashboardPanel`**: 
    - **Metrics Card**: Active Lounges, Total Capacity, Pending Requests.
    - **Live Activity**: JList showing real-time entry/exit logs.
    - **Greeting**: Dynamic "Good Morning, [Name]" header.
2.  **`ProfilePanel`**: Sensitive data updates (Change Password, Personal Info).

### **3.3. Guest Interaction (Reception / Passenger)**
1.  **`LoungeBrowserPanel`**: Grid of lounge cards with real-time occupancy bars.
2.  **`PassengerDirectoryPanel`**: Searchable table of passengers with "Quick Actions" for new entry.
3.  **`AccessRequestQueuePanel`**: Split-pane view. Left: List of pending requests. Right: Detailed evaluation (from `AccessFlowEngine`).
4.  **`CheckInFormPanel`**: High-speed form. Passport scanning simulation, Membership verification.

### **3.4. Operational Management (Supervisor / Manager)**
1.  **`LoungeDetailPanel`**: 
    - **Live Monitor**: Capacity gauge.
    - **Amenity List**: Editable table for snacks, showers, nap-pods.
    - **Status Toggle**: One-click "Close Lounge" or "Waitlist Only" buttons.
2.  **`TransactionLedgerPanel`**: Advanced filtering for payments. Export to CSV feature.
3.  **`StaffShiftPanel`**: Schedule view for receptionists and supervisors.

### **3.5. System Oversight (Admin Only)**
1.  **`UserManagementPanel`**: Role assignment, account suspension, audit log lookup.
2.  **`ProviderConfigPanel`**: Rules engine configuration (Defining VIP criteria, Aggregator limits).
3.  **`LibraryPanel`**: Master list of Airports, Terminals, and Geo-data.

---

## ⚡ 4. Advanced Technical Features

### **A. Real-Time Sychronization**
- **Monitor Thread**: A background thread polls the occupancy data every 5 seconds (or uses a Pub/Sub model if the DB supports triggers).
- **Auto-Refresh**: Swing Panels implement a `Refreshable` interface to update local labels without reloading the entire screen.

### **B. Customer Handling Techniques**
- **FCFS Queue Manager**: A dedicated panel for Receptionists that prioritizes requests based on a weighted algorithm (Time in queue + Tier priority).
- **Dispute Override**: A secure pop-up for Supervisors to manually approve a denied request (Audit-logged).

### **C. Precision Timing**
- Every "Check-In" action will track `ProcessingTimeMillis` to generate "Efficiency Reports" for management.

---

## 🎨 5. Visual Design Language (Swing)

- **Look & Feel**: `com.formdev.flatlaf.FlatDarkLaf`
- **Primary Color**: `#6366F1` (Indigo Glow)
- **Background**: `#0F172A` (Deep Slate)
- **Surface**: `#1E293B` (Elevated Card)
- **Fonts**: **Inter** (Global UI), **JetBrains Mono** (For IDs and Metrics)
