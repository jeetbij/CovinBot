package com.example.cowin.repository;

import com.google.common.base.Optional;

import com.example.cowin.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("from User u where u.userId = ?1 AND channel = ?2")
    Optional<User> findByUserId(String userId, String channel);

}
