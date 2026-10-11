package com.alm.dao;

import com.alm.model.Passenger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for PassengerDAO using an H2 in-memory database.
 */
public class PassengerDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:passdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";

    private PassengerDAO dao;

    @BeforeClass
    public static void switchToH2() {
        com.alm.util.DBConnection.closePool();
        System.setProperty("db.driver", "org.h2.Driver");
        System.setProperty("db.url", H2_URL);
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found", e);
        }
    }

    @org.junit.AfterClass
    public static void restoreProperties() {
        com.alm.util.DBConnection.closePool();
        System.clearProperty("db.driver");
        System.clearProperty("db.url");
        System.clearProperty("db.user");
        System.clearProperty("db.password");
    }

    @Before
    public void setUp() throws Exception {
        try (java.sql.Connection conn = com.alm.util.DBConnection.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS passengers");
            stmt.executeUpdate(
                    "CREATE TABLE passengers (" +
                            "  passenger_id INT AUTO_INCREMENT PRIMARY KEY," +
                            "  full_name    VARCHAR(150) NOT NULL," +
                            "  passport_number VARCHAR(50) NOT NULL UNIQUE," +
                            "  nationality  VARCHAR(100)," +
                            "  phone        VARCHAR(30)," +
                            "  email        VARCHAR(150)," +
                            "  is_active    TINYINT DEFAULT 1," +
                            "  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "  updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")");
        }
        dao = new PassengerDAO();
    }

    // ── addPassenger ──────────────────────────────────────────────────────────

    @Test
    public void testAddPassengerReturnsTrue() {
        Passenger p = buildPassenger("Alice Smith", "PP001");
        boolean result = dao.addPassenger(p);
        assertTrue("addPassenger should return true", result);
    }

    @Test
    public void testAddPassengerSetsGeneratedId() {
        Passenger p = buildPassenger("Bob Jones", "PP002");
        dao.addPassenger(p);
        assertTrue("Generated ID must be > 0", p.getId() > 0);
    }

    @Test
    public void testAddDuplicatePassportReturnsFalse() {
        dao.addPassenger(buildPassenger("Charlie", "PP003"));
        boolean second = dao.addPassenger(buildPassenger("Delta", "PP003")); // same passport
        assertFalse("Inserting duplicate passport must return false", second);
    }

    // ── getAllPassengers ───────────────────────────────────────────────────────

    @Test
    public void testGetAllPassengersEmptyTable() {
        List<Passenger> list = dao.getAllPassengers();
        assertNotNull(list);
        assertTrue("Empty table should return empty list", list.isEmpty());
    }

    @Test
    public void testGetAllPassengersReturnsAll() {
        dao.addPassenger(buildPassenger("Eve", "PP004"));
        dao.addPassenger(buildPassenger("Frank", "PP005"));
        List<Passenger> list = dao.getAllPassengers();
        assertEquals("Should retrieve all inserted passengers", 2, list.size());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    public void testFindByIdReturnsCorrectPassenger() {
        Passenger p = buildPassenger("Grace", "PP006");
        dao.addPassenger(p);
        Passenger found = dao.findById(p.getId());
        assertNotNull(found);
        assertEquals("Grace", found.getFullName());
        assertEquals("PP006", found.getPassportNumber());
    }

    @Test
    public void testFindByIdReturnsNullForMissingId() {
        assertNull("findById with non-existent ID must return null", dao.findById(9999));
    }

    // ── findByPassport ────────────────────────────────────────────────────────

    @Test
    public void testFindByPassportFound() {
        dao.addPassenger(buildPassenger("Heidi", "PP007"));
        Passenger found = dao.findByPassport("PP007");
        assertNotNull(found);
        assertEquals("Heidi", found.getFullName());
    }

    @Test
    public void testFindByPassportNotFound() {
        assertNull(dao.findByPassport("NONEXISTENT"));
    }

    // ── deactivatePassenger ───────────────────────────────────────────────────

    @Test
    public void testDeactivatePassenger() {
        Passenger p = buildPassenger("Ivan", "PP008");
        dao.addPassenger(p);
        boolean deactivated = dao.deactivatePassenger(p.getId());
        assertTrue("deactivatePassenger should return true", deactivated);

        Passenger found = dao.findById(p.getId());
        assertNotNull(found);
        assertFalse("Deactivated passenger must have is_active = false", found.isActive());
    }

    // ── updatePassenger ───────────────────────────────────────────────────────

    @Test
    public void testUpdatePassenger() {
        Passenger p = buildPassenger("Judy", "PP009");
        dao.addPassenger(p);
        p.setFullName("Judy Updated");
        p.setPhone("+1-800-000-0000");
        boolean updated = dao.updatePassenger(p);
        assertTrue("updatePassenger should return true", updated);

        Passenger found = dao.findById(p.getId());
        assertEquals("Judy Updated", found.getFullName());
        assertEquals("+1-800-000-0000", found.getPhone());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private static Passenger buildPassenger(String name, String passport) {
        Passenger p = new Passenger();
        p.setFullName(name);
        p.setPassportNumber(passport);
        p.setNationality("British");
        p.setPhone("+44 0000 000000");
        p.setEmail(name.toLowerCase().replace(" ", ".") + "@test.com");
        p.setActive(true);
        return p;
    }
}
