package com.gymplatform.service;

import com.gymplatform.domain.User;
import com.gymplatform.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {
    private final UserMapper userMapper;

    public CurrentUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User require(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        var user = userMapper.findByUsername(authentication.getName());
        if (user == null || !user.active()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return user;
    }
}
