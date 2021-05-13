package com.example.cowin.service;

import com.google.common.base.Optional;

import com.example.cowin.model.User;

public interface IUserService {

    Optional<User> findByUserId(String userId, String channel);

    void save(User u);

}
