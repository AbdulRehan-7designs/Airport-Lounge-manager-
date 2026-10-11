package com.alm.model;

public class LoungeAmenity {
    private int id;
    private int loungeId;
    private String itemName;
    private int totalCount;
    private int availableCount;

    public LoungeAmenity() {}

    public LoungeAmenity(int id, int loungeId, String itemName, int totalCount, int availableCount) {
        this.id = id;
        this.loungeId = loungeId;
        this.itemName = itemName;
        this.totalCount = totalCount;
        this.availableCount = availableCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLoungeId() { return loungeId; }
    public void setLoungeId(int loungeId) { this.loungeId = loungeId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getAvailableCount() { return availableCount; }
    public void setAvailableCount(int availableCount) { this.availableCount = availableCount; }
}
