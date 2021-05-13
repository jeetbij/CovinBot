package com.example.cowin.service;

import com.google.common.base.Optional;

import com.example.cowin.model.Pincode;
import com.example.cowin.repository.PincodeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PincodeService implements IPincodeService {

    @Autowired
    private PincodeRepository repository;

    @Override
    public Optional<Pincode> findByPincode(String pincode, String userId) {
        return repository.findByPincode(pincode, userId);
    }

    @Override
    public void save(Pincode pincode) {
        repository.save(pincode);
    }
    
}
