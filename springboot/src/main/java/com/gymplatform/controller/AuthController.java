package com.gymplatform.controller;

import com.gymplatform.domain.User;
import com.gymplatform.mapper.UserMapper;
import com.gymplatform.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            AuthenticationManager authenticationManager,
            UserMapper userMapper,
            AuthService authService
    ) {
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.authService = authService;
    }

    @GetMapping("/session")
    SessionResponse session(Authentication authentication, CsrfToken csrfToken) {
        UserSummary summary = null;
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            summary = UserSummary.from(userMapper.findByUsername(authentication.getName()));
        }
        return new SessionResponse(summary, csrfToken.getToken());
    }

    @PostMapping("/session")
    SessionResponse login(
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response,
            CsrfToken csrfToken
    ) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password())
        );
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        var user = userMapper.findByUsername(authentication.getName());
        return new SessionResponse(UserSummary.from(user), csrfToken.getToken());
    }

    @DeleteMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    void register(@Valid @RequestBody RegisterRequest body) {
        authService.register(body.username(), body.password(), body.displayName(), body.email());
    }

    public record LoginRequest(
            @NotBlank @Size(max = 64) String username,
            @NotBlank @Size(max = 100) String password
    ) {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank @Size(min = 10, max = 100) String password,
            @NotBlank @Size(max = 100) String displayName,
            @NotBlank @Email @Size(max = 255) String email
    ) {
    }

    public record SessionResponse(UserSummary user, String csrfToken) {
    }

    public record UserSummary(Long id, String username, String displayName, String email, String role) {
        static UserSummary from(User user) {
            return user == null ? null : new UserSummary(
                    user.id(), user.username(), user.displayName(), user.email(), user.role().name()
            );
        }
    }
}
