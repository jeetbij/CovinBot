package com.example.cowin.repository;

import com.example.cowin.model.Pincode;
import com.google.common.base.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PincodeRepository extends JpaRepository<Pincode, Long> {
    
    @Query("from Pincode p where p.pincode = ?1 AND p.userId = ?2")
    Optional<Pincode> findByPincode(String pincode, String userId);

}
