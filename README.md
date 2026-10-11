# Airport Lounge Manager

## Overview
A web-based application for managing airport lounge access, passenger tracking, and transactions.

## Project Structure
- `src/main/java`: Java source code (Controllers, Services, DAOs, Models)
- `src/main/webapp`: Views (JSP), CSS, and external assets
- `src/test`: Unit tests

## Technology Stack
- **Backend**: Java Servlets, JSP
- **Build Tool**: Maven
- **Database**: JDBC (MySQL/H2 compatible)
- **Frontend**: HTML5, Vanilla CSS (Modern Dark Theme)

## How to Run

### Option 1: Run with Maven (no Tomcat install needed)
1. **Prerequisites**: Java JDK 17+, Maven 3.8+
2. **MySQL**: Ensure the `alm` database exists and the `users` table is set up (see schema or phpMyAdmin).
3. From the project folder run:
   ```bash
   mvn clean package cargo:run
   ```
4. In your browser open: **http://localhost:8080/airport-lounge-manager**  
   (Wait a few seconds for Tomcat to start the first time.)

### Option 2: Run on your own Tomcat
1. **Prerequisites**: Java JDK 17+, Maven 3.8+, Tomcat 9+ installed.
2. Build the WAR:
   ```bash
   mvn clean package
   ```
3. Copy `target/airport-lounge-manager.war` into your Tomcat `webapps` folder.
4. Start Tomcat (e.g. run `bin/startup.bat` on Windows or `bin/startup.sh` on Linux/Mac).
5. Open: **http://localhost:8080/airport-lounge-manager**

**If the app fails to start:** This project uses Java 17. Tomcat 8.5 supports up to Java 11. Use **Tomcat 9 or 10** with Java 17, or run Tomcat with Java 11. Check `logs/catalina.*.log` for the exact error.

## Default Login
- **Username**: admin
- **Password**: admin (stored as SHA-256 hash in DB)

## Database configuration
- **Default**: connects to `jdbc:mysql://localhost:3306/alm` as `root` with no password.
- **Override**: set env vars `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_DRIVER`, or copy `src/main/resources/db.properties.example` to `db.properties` (in classpath) and set values. Env vars take precedence over `db.properties`.
