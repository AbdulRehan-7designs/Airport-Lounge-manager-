package com.alm.model;

public class Lounge {
    private int id;
    private int airportId;
    private String name;
    private String terminal;
    private int capacity;
    private String status;
    private String location; // e.g. "Chicago, IL"
    private boolean active;

    public Lounge() {
    }

    public Lounge(int id, int airportId, String name, String terminal, int capacity, String status) {
        this.id = id;
        this.airportId = airportId;
        this.name = name;
        this.terminal = terminal;
        this.capacity = capacity;
        this.status = status != null ? status : "ACTIVE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAirportId() {
        return airportId;
    }

    public void setAirportId(int airportId) {
        this.airportId = airportId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
