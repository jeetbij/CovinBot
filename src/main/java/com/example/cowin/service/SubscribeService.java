package com.example.cowin.service;

import java.util.List;
import com.google.common.base.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cowin.model.Subscription;
import com.example.cowin.model.User;
import com.example.cowin.repository.SubscriptionRepository;

@Service
public class SubscribeService implements ISubscribeService {

    @Autowired
    private SubscriptionRepository repository;

    @Override
    public List<Subscription> findByName(User user) {
        return repository.findByName(user);
    }

    @Override
    public Optional<Subscription> findByNamePincodeAge(User user, String pincode, Integer age) {
        return repository.findByNamePincodeAge(user, pincode, age);
    }

    @Override
    public List<Subscription> findByPincodeAge(String pincode, Integer minAge, Integer maxAge) {
        return repository.findByPincodeAge(pincode, minAge, maxAge);
    }

    @Override
    public List<Subscription> findAll() {
        return repository.findAll();
    }

    @Override
    public List<String> findDistinctPincode() {
        return repository.findDistinctPincode();
    }

    @Override
    public void save(Subscription s) {
        repository.save(s);
    }
    
}
