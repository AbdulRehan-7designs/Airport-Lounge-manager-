# How Login Authentication Checks the `alm` Database

This document shows the exact path from your login form to the **alm** database.

---

## 1. You submit the form (login.jsp)

**File:** `src/main/webapp/WEB-INF/views/login.jsp`

```html
<form action="login" method="post">
    <input type="text" name="username" ...>
    <input type="password" name="password" ...>
    <button type="submit">Sign In</button>
</form>
```

- **action="login"** → browser sends **POST** to `/airport-lounge-manager/login`
- Parameters: `username` and `password`

---

## 2. LoginServlet receives the request

**File:** `src/main/java/com/alm/controller/LoginServlet.java`

```java
@Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    String u = req.getParameter("username");   // e.g. "admin"
    String p = req.getParameter("password");   // e.g. "admin"

    User user = authService.login(u, p);       // ← checks against DB here

    if (user != null) {
        session.setAttribute("user", user);
        resp.sendRedirect("dashboard");        // success
    } else {
        req.setAttribute("error", "Invalid Credentials");
        // show login again
    }
}
```

- Reads **username** and **password** from the form.
- Calls **AuthService.login(username, password)**. If it returns a user, login succeeds; otherwise you see "Invalid Credentials".

---

## 3. AuthService asks UserDAO for the user, then verifies password

**File:** `src/main/java/com/alm/service/AuthService.java`

```java
public User login(String username, String password) {
    User user = userDAO.findByUsername(username);   // ← 1) Load user from DB (alm)
    if (user != null && PasswordUtil.verifyPassword(password, user.getPassword())) {
        return user;                                 // ← 2) Check password hash
    }
    return null;
}
```

- **Step 1:** Get the user row from the database by **username** (this uses **alm** — see steps 4 and 5).
- **Step 2:** If a user exists, check that the typed password matches the stored hash using **PasswordUtil.verifyPassword(plainPassword, storedHash)**.

---

## 4. UserDAO runs the SQL on the connection from DBConnection

**File:** `src/main/java/com/alm/dao/UserDAO.java`

```java
public User findByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";

    try (Connection conn = DBConnection.getConnection();   // ← connection to alm
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, username);    // e.g. "admin"
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),   // stored hash from DB
                    rs.getString("role"));
            }
        }
    }
    return null;
}
```

- **DBConnection.getConnection()** gives a connection to MySQL (see step 5).
- The SQL runs on that connection: **`SELECT * FROM users WHERE username = ?`**
- So the **users** table is read from whichever database that connection uses → **alm**.

---

## 5. DBConnection connects to the `alm` database

**File:** `src/main/java/com/alm/util/DBConnection.java`

```java
private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/alm";
//                                                              ^^^
//                                                      database name = alm

public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(getUrl(), getUser(), getPassword());
}
```

- **getUrl()** returns the JDBC URL. By default it is **`jdbc:mysql://localhost:3306/alm`**.
- So every connection (and thus every query in UserDAO) uses the **alm** database.
- The **users** table is therefore **alm.users**.

---

## 6. Password verification (AuthService step 2)

**File:** `src/main/java/com/alm/util/PasswordUtil.java`

```java
public static boolean verifyPassword(String plainPassword, String storedHash) {
    if (plainPassword == null || storedHash == null) return false;
    return storedHash.trim().equals(hashPassword(plainPassword));
}
```

- **plainPassword** = what the user typed (e.g. "admin").
- **storedHash** = `user.getPassword()` from the DB (the **password** column from **alm.users**).
- The app hashes the typed password and compares it to the stored hash. If they match, the password is correct.

---

## Flow summary (how it checks `alm`)

```
Browser (login form)
    → POST /login  (username, password)
        → LoginServlet.doPost()
            → AuthService.login(u, p)
                → UserDAO.findByUsername(username)
                    → DBConnection.getConnection()   →  jdbc:mysql://localhost:3306/alm
                    → SELECT * FROM users WHERE username = ?   (on alm)
                ← User object (id, username, password, role) from alm.users
                → PasswordUtil.verifyPassword(password, user.getPassword())
            ← User if OK, else null
        → if user != null: redirect to dashboard
          else: show "Invalid Credentials"
```

So:

- The **only** place user data is loaded for login is **UserDAO.findByUsername()**.
- That method uses **DBConnection.getConnection()**, which points at **alm**.
- So login **always** checks the **alm** database, **users** table.

---

## Table and columns used in `alm`

| Database | Table  | Columns used by login      |
|----------|--------|-----------------------------|
| **alm**  | **users** | id, username, password, role |

The password in the DB must be the **SHA-256 (Base64)** hash of the user’s password (e.g. for "admin" use the hash from `fix-admin-password.sql`).
