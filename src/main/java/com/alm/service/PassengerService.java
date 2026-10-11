package com.alm.service;

import com.alm.dao.PassengerDAO;
import com.alm.model.Passenger;

import java.util.List;

public class PassengerService {
    private final PassengerDAO passengerDAO = new PassengerDAO();

    public List<Passenger> getAllPassengers() {
        return passengerDAO.getAllPassengers();
    }

    public Passenger findById(int id) {
        return passengerDAO.findById(id);
    }

    public boolean addPassenger(Passenger p) {
        return passengerDAO.addPassenger(p);
    }

    public boolean updatePassenger(Passenger p) {
        return passengerDAO.updatePassenger(p);
    }

    public boolean deactivatePassenger(int id) {
        return passengerDAO.deactivatePassenger(id);
    }
}
