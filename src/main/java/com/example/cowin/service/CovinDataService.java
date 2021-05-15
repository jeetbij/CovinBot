package com.example.cowin.service;

import com.example.cowin.model.CovinData;
import com.example.cowin.repository.CovinDataRepository;
import com.google.common.base.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CovinDataService implements ICovinDataService {

    @Autowired
    private CovinDataRepository repository;

    @Override
    public void save(CovinData cd) {
        repository.save(cd);
    }

    @Override
    public Optional<CovinData> findLastRecordByPincodeDate(String pincode, String date) {
        return repository.findLastRecordByPincodeDate(pincode, date);
    }
    
}
