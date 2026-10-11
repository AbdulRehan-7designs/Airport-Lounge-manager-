package com.alm.dao;

import com.alm.model.Flight;
import com.alm.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {

    public List<Flight> getUpcomingFlights(int hours) {
        List<Flight> list = new ArrayList<>();
        // Flights departing in the next X hours
        String sql = "SELECT * FROM flights WHERE scheduled_departure BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL ? HOUR) ORDER BY scheduled_departure ASC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hours);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Flight> getAllFlights() {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT * FROM flights ORDER BY scheduled_departure ASC";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Look up a single flight by its database primary key.
     */
    public Flight findById(int flightId) {
        String sql = "SELECT * FROM flights WHERE flight_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, flightId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Look up a single flight by its flight number (e.g., "AI202").
     * Returns null if not found. Reads from DB (local cache).
     */
    public Flight findByFlightNumber(String flightNumber) {
        String sql = "SELECT * FROM flights WHERE flight_number = ? ORDER BY scheduled_departure DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, flightNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Smart Upsert — Insert or Update if exists.
     * Now includes terminal, gate, and delay_minutes from Aviationstack API.
     */
    public boolean upsertFlight(Flight f) {
        String sql = "INSERT INTO flights (flight_number, airline, origin, destination, " +
                "scheduled_departure, scheduled_arrival, status, passenger_capacity, " +
                "eligibility_percentage, terminal, gate, delay_minutes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "status = VALUES(status), " +
                "scheduled_departure = VALUES(scheduled_departure), " +
                "scheduled_arrival = VALUES(scheduled_arrival), " +
                "terminal = VALUES(terminal), " +
                "gate = VALUES(gate), " +
                "delay_minutes = VALUES(delay_minutes)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, f.getFlightNumber());
            stmt.setString(2, f.getAirline());
            stmt.setString(3, f.getOrigin());
            stmt.setString(4, f.getDestination());
            stmt.setTimestamp(5, f.getScheduledDeparture());
            stmt.setTimestamp(6, f.getScheduledArrival());
            stmt.setString(7, f.getStatus());
            stmt.setInt(8, f.getPassengerCapacity());
            stmt.setDouble(9, f.getEligibilityPercentage());
            stmt.setString(10, f.getTerminal() != null ? f.getTerminal() : "");
            stmt.setString(11, f.getGate() != null ? f.getGate() : "");
            stmt.setInt(12, f.getDelayMinutes());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Batch upsert — inserts/updates multiple flights in a single transaction.
     * Much faster than individual upserts for large sync batches.
     */
    public int batchUpsert(List<Flight> flights) {
        String sql = "INSERT INTO flights (flight_number, airline, origin, destination, " +
                "scheduled_departure, scheduled_arrival, status, passenger_capacity, " +
                "eligibility_percentage, terminal, gate, delay_minutes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "status = VALUES(status), " +
                "scheduled_departure = VALUES(scheduled_departure), " +
                "scheduled_arrival = VALUES(scheduled_arrival), " +
                "terminal = VALUES(terminal), " +
                "gate = VALUES(gate), " +
                "delay_minutes = VALUES(delay_minutes)";

        int updated = 0;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Flight f : flights) {
                    stmt.setString(1, f.getFlightNumber());
                    stmt.setString(2, f.getAirline());
                    stmt.setString(3, f.getOrigin());
                    stmt.setString(4, f.getDestination());
                    stmt.setTimestamp(5, f.getScheduledDeparture());
                    stmt.setTimestamp(6, f.getScheduledArrival());
                    stmt.setString(7, f.getStatus());
                    stmt.setInt(8, f.getPassengerCapacity());
                    stmt.setDouble(9, f.getEligibilityPercentage());
                    stmt.setString(10, f.getTerminal() != null ? f.getTerminal() : "");
                    stmt.setString(11, f.getGate() != null ? f.getGate() : "");
                    stmt.setInt(12, f.getDelayMinutes());
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                for (int r : results) {
                    if (r > 0 || r == Statement.SUCCESS_NO_INFO)
                        updated++;
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updated;
    }

    /**
     * Get flights with significant delays (> threshold minutes).
     * Used for automatic lounge access extension notifications.
     */
    public List<Flight> getDelayedFlights(int minDelayMinutes) {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT * FROM flights WHERE delay_minutes >= ? AND status != 'DEPARTED' " +
                "ORDER BY delay_minutes DESC";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, minDelayMinutes);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void clearAll() {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE flights");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Flight mapRow(ResultSet rs) throws SQLException {
        // Handle columns that may not exist yet (backward compatibility)
        String terminal = "";
        String gate = "";
        int delay = 0;
        try {
            terminal = rs.getString("terminal");
            if (terminal == null)
                terminal = "";
        } catch (SQLException ignored) {
        }
        try {
            gate = rs.getString("gate");
            if (gate == null)
                gate = "";
        } catch (SQLException ignored) {
        }
        try {
            delay = rs.getInt("delay_minutes");
        } catch (SQLException ignored) {
        }

        return new Flight(
                rs.getInt("flight_id"),
                rs.getString("flight_number"),
                rs.getString("airline"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getTimestamp("scheduled_departure"),
                rs.getTimestamp("scheduled_arrival"),
                rs.getString("status"),
                rs.getInt("passenger_capacity"),
                rs.getDouble("eligibility_percentage"),
                rs.getTimestamp("last_updated_at"),
                terminal,
                gate,
                delay);
    }
}
