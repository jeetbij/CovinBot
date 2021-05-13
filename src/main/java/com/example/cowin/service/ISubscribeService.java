package com.example.cowin.service;

import java.util.List;
import com.google.common.base.Optional;

import com.example.cowin.model.Subscription;
import com.example.cowin.model.User;

public interface ISubscribeService {

    List<Subscription> findByName(User user);

    Optional<Subscription> findByNamePincodeAge(User user, String pincode, Integer age);

    List<Subscription> findByPincodeAge(String pincode, Integer minAge, Integer maxAge);

    List<Subscription> findAll();

    List<String> findDistinctPincode();

    void save(Subscription s);

}
