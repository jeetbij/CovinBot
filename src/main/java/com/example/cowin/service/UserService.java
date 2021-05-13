package com.example.cowin.service;

import com.google.common.base.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cowin.model.User;
import com.example.cowin.repository.UserRepository;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository repository;

    @Override
    public Optional<User> findByUserId(String userId, String channel) {
        return repository.findByUserId(userId, channel);
    }

    @Override
    public void save(User u) {
        repository.save(u);
    }
    
}
