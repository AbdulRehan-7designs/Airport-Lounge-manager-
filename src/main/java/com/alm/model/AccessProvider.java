package com.alm.model;

public class AccessProvider {
    private int id;
    private String providerName;
    private String providerType; // AIRLINE, CREDIT_CARD, AGGREGATOR
    private boolean active = true;

    public AccessProvider() {}

    public AccessProvider(int id, String providerName, String providerType, boolean active) {
        this.id = id;
        this.providerName = providerName;
        this.providerType = providerType;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
