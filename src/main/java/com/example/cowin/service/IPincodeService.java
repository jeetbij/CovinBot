package com.example.cowin.service;

import com.google.common.base.Optional;

import com.example.cowin.model.Pincode;

public interface IPincodeService  {

    Optional<Pincode> findByPincode(String pincode, String userId);

    void save(Pincode pincode);
    
}
