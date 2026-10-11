package com.alm.model;

import java.sql.Timestamp;

public class Flight {
    private int flightId;
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private Timestamp scheduledDeparture;
    private Timestamp scheduledArrival;
    private String status; // SCHEDULED, BOARDING, DEPARTED, DELAYED, CANCELLED
    private int passengerCapacity;
    private double eligibilityPercentage;
    private Timestamp lastUpdatedAt;

    // ── Aviationstack API fields ─────────────────────────────────────────
    private String terminal; // departure terminal (e.g., "2")
    private String gate; // departure gate (e.g., "D11")
    private int delayMinutes; // departure delay in minutes (0 = on time)

    public Flight() {
    }

    public Flight(int flightId, String flightNumber, String airline, String origin, String destination,
            Timestamp scheduledDeparture, Timestamp scheduledArrival, String status,
            int passengerCapacity, double eligibilityPercentage, Timestamp lastUpdatedAt) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.status = status;
        this.passengerCapacity = passengerCapacity;
        this.eligibilityPercentage = eligibilityPercentage;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Flight(int flightId, String flightNumber, String airline, String origin, String destination,
            Timestamp scheduledDeparture, Timestamp scheduledArrival, String status,
            int passengerCapacity, double eligibilityPercentage, Timestamp lastUpdatedAt,
            String terminal, String gate, int delayMinutes) {
        this(flightId, flightNumber, airline, origin, destination,
                scheduledDeparture, scheduledArrival, status,
                passengerCapacity, eligibilityPercentage, lastUpdatedAt);
        this.terminal = terminal;
        this.gate = gate;
        this.delayMinutes = delayMinutes;
    }

    // Getters and Setters
    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Timestamp getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(Timestamp scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public Timestamp getScheduledArrival() {
        return scheduledArrival;
    }

    public void setScheduledArrival(Timestamp scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    public void setPassengerCapacity(int passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }

    public double getEligibilityPercentage() {
        return eligibilityPercentage;
    }

    public void setEligibilityPercentage(double eligibilityPercentage) {
        this.eligibilityPercentage = eligibilityPercentage;
    }

    public Timestamp getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Timestamp lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Logic to calculate estimated lounge load from this flight
    public int getEstimatedLoungePassengers() {
        return (int) Math.round(passengerCapacity * eligibilityPercentage);
    }

    // ── Aviationstack: Terminal, Gate, Delay ──────────────────────────────

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(int delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    // ── Convenience Helpers ──────────────────────────────────────────────

    /** Returns true if the flight is delayed by more than the given threshold. */
    public boolean isDelayed(int thresholdMinutes) {
        return delayMinutes >= thresholdMinutes;
    }

    /** Returns a display string like "T2 / Gate D11". */
    public String getTerminalGateDisplay() {
        StringBuilder sb = new StringBuilder();
        if (terminal != null && !terminal.isEmpty())
            sb.append("T").append(terminal);
        if (gate != null && !gate.isEmpty()) {
            if (sb.length() > 0)
                sb.append(" / ");
            sb.append("Gate ").append(gate);
        }
        return sb.length() > 0 ? sb.toString() : "—";
    }

    /** Returns a human-readable delay string. */
    public String getDelayDisplay() {
        if (delayMinutes <= 0)
            return "On Time";
        if (delayMinutes < 60)
            return delayMinutes + " min delayed";
        return (delayMinutes / 60) + "h " + (delayMinutes % 60) + "m delayed";
    }
}
