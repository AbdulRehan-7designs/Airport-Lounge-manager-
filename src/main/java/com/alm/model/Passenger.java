package com.alm.model;

public class Passenger {
    private int id;
    private String fullName;
    private String passportNumber;
    private String nationality;
    private String phone;
    private String email;
    private boolean active;

    // Live Passenger Status Extensions
    private String bookingReference;
    private Integer flightId;
    private String membershipType;
    private Integer membershipId;
    private boolean membershipValid;
    private int visitCountThisMonth;
    private String ticketClass;
    private java.sql.Timestamp flightTime;
    private String accessStatus;
    private Integer checkedInLoungeId;
    private String assignedSeat;
    private java.sql.Timestamp checkInTime;

    public Passenger() {
    }

    /** Full constructor matching the passengers DB table. */
    public Passenger(int id, String fullName, String passportNumber,
            String nationality, String phone, String email, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.passportNumber = passportNumber;
        this.nationality = nationality;
        this.phone = phone;
        this.email = email;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Integer getFlightId() {
        return flightId;
    }

    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }

    public Integer getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Integer membershipId) {
        this.membershipId = membershipId;
    }

    public boolean isMembershipValid() {
        return membershipValid;
    }

    public void setMembershipValid(boolean membershipValid) {
        this.membershipValid = membershipValid;
    }

    public int getVisitCountThisMonth() {
        return visitCountThisMonth;
    }

    public void setVisitCountThisMonth(int visitCountThisMonth) {
        this.visitCountThisMonth = visitCountThisMonth;
    }

    public String getTicketClass() {
        return ticketClass;
    }

    public void setTicketClass(String ticketClass) {
        this.ticketClass = ticketClass;
    }

    public java.sql.Timestamp getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(java.sql.Timestamp flightTime) {
        this.flightTime = flightTime;
    }

    public String getAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(String accessStatus) {
        this.accessStatus = accessStatus;
    }

    public Integer getCheckedInLoungeId() {
        return checkedInLoungeId;
    }

    public void setCheckedInLoungeId(Integer checkedInLoungeId) {
        this.checkedInLoungeId = checkedInLoungeId;
    }

    public String getAssignedSeat() {
        return assignedSeat;
    }

    public void setAssignedSeat(String assignedSeat) {
        this.assignedSeat = assignedSeat;
    }

    public java.sql.Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(java.sql.Timestamp checkInTime) {
        this.checkInTime = checkInTime;
    }
}
