package com.alm.model;

import java.sql.Timestamp;

public class Airport {
    private int id;
    private String name;
    private String iataCode;
    private String city;
    private String country;
    private boolean active;
    private Timestamp createdAt;

    public Airport() {
    }

    public Airport(int id, String name, String iataCode, String city, String country, boolean active,
            Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.iataCode = iataCode;
        this.city = city;
        this.country = country;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public boolean isActive() {
        return active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
