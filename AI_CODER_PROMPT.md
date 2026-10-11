# AI Coder Prompt: Airport Lounge Manager System

This document contains a comprehensive, single prompt that you can provide to an AI coding assistant (like Claude, ChatGPT, or Gemini) to recreate the entire **Airport Lounge Manager** system from scratch.

To use this, simply copy the section below the horizontal line and paste it as your first message to the AI.

---

**Role & Objective**
You are an expert full-stack Java and Web developer. Your task is to build a complete, production-ready "Airport Lounge Manager" web application from scratch. You will generate the project structure, configuration files, backend Java code, database schema logic, and frontend JSP/CSS files. 

**Technology Stack**
- **Backend:** Java 17, Servlet API 3.1.0 (compatible with Tomcat 8.5)
- **Dependency Management & Build:** Maven (`pom.xml` with WAR packaging)
- **Database:** MySQL 8 with HikariCP for connection pooling and native JDBC (no ORM/Hibernate)
- **Frontend:** JSP, JSTL (Standard Tag Library), HTML5, Vanilla CSS 
- **Server:** Apache Tomcat 8.5
- **Security:** BCrypt for password hashing

**Design System & UI/UX**
The user interface MUST be modern, utilizing a premium "dark mode" aesthetic. 
- **Colors:** Deep void backgrounds (`#080D1A`), indigo/violet accents, emerald success states, rose error states.
- **Components:** Implement glassmorphism (translucent panels with blur), subtle glow effects, smooth hover transitions, and rounded corners.
- **Layout:** A grid-based App Shell approach. Include a persistent sidebar for navigation and a top bar containing the page title, a live clock, and a dynamic notification bell.
- **No JS Frameworks:** Rely solely on pure CSS for animations and layout, and vanilla JavaScript only when absolutely necessary (e.g., toggling menus or live clocks).

**Core Features & Domains**
1. **Authentication & Authorization**
   - Implement an `AuthFilter` that implements `javax.servlet.Filter`.
   - Protect all `/WEB-INF/views/` and system actions. 
   - Maintain Session state. Create a `User` model, `UserDAO`, and a `login.jsp` with error handling.
   - Define a `Role` Enum with permission sets: `SUPER_ADMIN`, `MANAGER`, `RECEPTIONIST`.

2. **Database Management**
   - Create a `DBConnection` utility using `HikariDataSource`.
   - Implement an auto-initialization mechanism that builds tables (Users, Lounges, Passengers, AccessRequests, Notifications) automatically on startup if they don't exist, and seeds a default admin user (`admin` / `admin123`).

3. **Dashboard (`dashboard.jsp`)**
   - Provide high-level metrics: Active Lounges, Total Capacity, Pending Requests.
   - Display a "Recent Activity" feed combining recent transactions and access requests.
   - Welcome the user by their name and dynamically display a greeting (Morning/Afternoon/Evening) based on the server time.

4. **Lounge Management**
   - Manage physical lounges (Code, Name, Terminal Number, Capacity, Operating Hours).
   - Require DAO methods and Servlets to List, Add, and Update lounges.

5. **Passenger Management**
   - Track passengers (Passport Number, Full Name, Email, Frequent Flyer Tier).
   - Require DAO methods and Servlets for creating and looking up passengers.

6. **Access Control & Requests**
   - Allow receptionists to log requested entries into lounges. 
   - Track the status of requests (PENDING, APPROVED, DENIED) and Payment Status.

7. **Notifications System**
   - A `NotificationDAO` handling alerts for the user.
   - Expose an API endpoint (`/api/notifications`) or use pure JSP/JSTL to render unread notifications in the top bar.

**Coding Guidelines & Constraints**
- **Servlet API 3.1 Strictness:** Do NOT use features from Servlet API 4.0 or above (do not use `HttpFilter`, use `Filter`).
- **JSP Scriptlets:** Keep Java logic inside JSPs to an absolute minimum. Use `<c:forEach>`, `<c:if>`, `<c:choose>`, and `<fmt:formatDate>` from JSTL to prevent Tomcat compilation errors. Never use `<% ... %>` for large logic blocks.
- **Absolute Paths:** When defining the POM and Web XML, ensure deployment configs match the constraints of `src/main/webapp/WEB-INF/web.xml`.
- **Modularity:** Separate your Java classes into packages: `com.alm.model`, `com.alm.dao`, `com.alm.servlet`, `com.alm.filter`, `com.alm.util`, `com.alm.config`. Include shared JSPs like `sidebar.jsp` and `topbar.jsp` in a `/WEB-INF/includes/` folder.

**Your Action Plan**
To execute this, please output your responses in the following order, providing full code for every file:
1. `pom.xml`, `web.xml`, and the `DBConnection.java` pool logic.
2. The Database Schema initialization scripts or Java logic.
3. The Models and DAOs for Users and core structures.
4. The Security layer (`PasswordUtil.java`, `AuthFilter.java`) and `LoginServlet.java`.
5. The Design System (`style.css`), `sidebar.jsp`, and `topbar.jsp`.
6. The Views and Servlets for the Dashboard and specific domains (Lounges, Passengers).

Please begin by acknowledging these instructions and providing the files for Step 1.
