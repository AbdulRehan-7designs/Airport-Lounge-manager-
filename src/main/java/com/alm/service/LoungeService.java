package com.alm.service;

import com.alm.dao.LoungeDAO;
import com.alm.model.Lounge;
import java.util.List;

public class LoungeService {
    private LoungeDAO loungeDAO = new LoungeDAO();

    public List<Lounge> getAvailableLounges() {
        return loungeDAO.getAllLounges();
    }
}
