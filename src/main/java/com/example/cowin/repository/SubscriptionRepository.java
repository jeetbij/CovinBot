package com.example.cowin.repository;

import java.util.List;
import com.google.common.base.Optional;

import com.example.cowin.model.Subscription;
import com.example.cowin.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("from Subscription s where s.user = ?1 AND s.isActive = 1")
    List<Subscription> findByName(User user);

    @Query("from Subscription s where s.user = ?1 AND s.pincode = ?2 AND s.age = ?3 AND s.isActive = 1")
    Optional<Subscription> findByNamePincodeAge(User user, String pincode, Integer age);

    @Query("from Subscription s where s.pincode = ?1 AND s.age >= ?2 AND s.age < ?3 AND s.isActive = 1")
    List<Subscription> findByPincodeAge(String pincode, Integer minAge, Integer maxAge);

    @Query("SELECT DISTINCT s.pincode from Subscription s where s.isActive = 1")
    List<String> findDistinctPincode();

}
