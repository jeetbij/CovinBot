package com.example.cowin.repository;

import com.example.cowin.model.CovinData;
import com.google.common.base.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CovinDataRepository extends JpaRepository<CovinData, Long> {
    
    @Query(nativeQuery = true, value = "select * from CovinData cd where cd.pincode = ?1 AND cd.date = ?2 order by cd.id desc limit 1")
    Optional<CovinData> findLastRecordByPincodeDate(String pincode, String date);

}
