package com.example.cowin.service;

import com.example.cowin.model.CovinData;
import com.google.common.base.Optional;

public interface ICovinDataService {
 
    Optional<CovinData> findLastRecordByPincodeDate(String pincode, String date);

    void save(CovinData tmr);

}
