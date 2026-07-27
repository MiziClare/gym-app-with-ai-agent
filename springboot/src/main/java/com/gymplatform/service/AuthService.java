package com.gymplatform.service;

import com.gymplatform.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MembershipService membershipService;

    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            MembershipService membershipService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.membershipService = membershipService;
    }

    @Transactional
    public void register(String username, String password, String displayName, String email) {
        var normalizedUsername = username.trim().toLowerCase();
        var normalizedEmail = email.trim().toLowerCase();
        if (userMapper.countByUsernameOrEmail(normalizedUsername, normalizedEmail) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
        }
        userMapper.insertMember(
                normalizedUsername,
                passwordEncoder.encode(password),
                displayName.trim(),
                normalizedEmail
        );
        membershipService.createProfile(userMapper.findByUsername(normalizedUsername).id());
    }
}
