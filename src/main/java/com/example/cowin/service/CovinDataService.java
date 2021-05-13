package com.example.cowin.service;

import com.example.cowin.model.CovinData;
import com.example.cowin.repository.CovinDataRepository;

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
    
}
