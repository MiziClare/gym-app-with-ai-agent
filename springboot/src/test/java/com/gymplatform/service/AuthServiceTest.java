package com.gymplatform.service;

import com.gymplatform.domain.Role;
import com.gymplatform.domain.User;
import com.gymplatform.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserMapper userMapper;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    MembershipService membershipService;

    @Test
    void registrationCreatesTheMemberProfileInTheSameFlow() {
        when(passwordEncoder.encode("valid-password")).thenReturn("hash");
        when(userMapper.findByUsername("member")).thenReturn(
                new User(42L, "member", "hash", "Member", "member@example.test",
                        Role.MEMBER, true, Instant.now())
        );

        new AuthService(userMapper, passwordEncoder, membershipService)
                .register(" Member ", "valid-password", " Member ", " MEMBER@example.test ");

        verify(userMapper).insertMember("member", "hash", "Member", "member@example.test");
        verify(membershipService).createProfile(42L);
    }
}
