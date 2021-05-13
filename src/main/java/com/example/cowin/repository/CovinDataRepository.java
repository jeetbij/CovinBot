package com.example.cowin.repository;

import com.example.cowin.model.CovinData;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CovinDataRepository extends JpaRepository<CovinData, Long> {
    
}
