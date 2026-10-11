package com.alm.model;

import java.sql.Timestamp;

public class SessionToken {
    private int tokenId;
    private int passengerId;
    private String tokenString;
    private Timestamp generatedAt;
    private Timestamp expiryTime;
    private boolean isUsed;

    public SessionToken() {
    }

    public SessionToken(int tokenId, int passengerId, String tokenString, Timestamp generatedAt, Timestamp expiryTime,
            boolean isUsed) {
        this.tokenId = tokenId;
        this.passengerId = passengerId;
        this.tokenString = tokenString;
        this.generatedAt = generatedAt;
        this.expiryTime = expiryTime;
        this.isUsed = isUsed;
    }

    public int getTokenId() {
        return tokenId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    public Timestamp getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Timestamp generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Timestamp getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Timestamp expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        this.isUsed = used;
    }
}
