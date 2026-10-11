# Tomcat 8.5 — 500 Error Troubleshooting

## Changes made to fix "all pages 500"

1. **SchemaInitializer** — No longer throws on DB failure. If MySQL is down or credentials are wrong, the app still starts. Schema init errors are logged to stderr only.

2. **Error pages** — Added 500 and exception-type mappings in `web.xml`. When a 500 occurs, you'll see the error message on the error page.

3. **error.jsp** — Simplified scriptlet; shows exception message for 500 errors to aid debugging.

4. **health.jsp** — Minimal page at `/health.jsp` that returns "OK". Use it to verify the app is running:
   - `http://localhost:8080/airport-lounge-manager/health.jsp`

## Checklist if you still get 500

### 1. MySQL running?
- Start XAMPP MySQL (or your MySQL service).
- Ensure database `alm` exists: `CREATE DATABASE IF NOT EXISTS alm;`

### 2. DB credentials
- Create `src/main/resources/db.properties` (copy from `db.properties.example`).
- Or set env: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Default: `jdbc:mysql://localhost:3306/alm`, user `root`, password empty.

### 3. Tomcat logs
- Check `C:\xampp1\tomcat\logs\catalina.out` or `localhost.*.log`.
- Look for `Schema initialization failed`, `SQLException`, or `JasperException`.

### 4. Java version
- Tomcat 8.5 supports Java 7+. Project compiles with Java 17.
- Run `java -version` to confirm.

### 5. Clean deploy
```powershell
Remove-Item C:\xampp1\tomcat\webapps\airport-lounge-manager -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item C:\xampp1\tomcat\webapps\airport-lounge-manager.war -Force -ErrorAction SilentlyContinue
mvn clean package -q
Copy-Item target\airport-lounge-manager.war C:\xampp1\tomcat\webapps\ -Force
```
Then restart Tomcat.

### 6. Test health page first
- Open: `http://localhost:8080/airport-lounge-manager/health.jsp`
- If you see "OK", the app is running. If you get 500, check Tomcat logs for the exact exception.
