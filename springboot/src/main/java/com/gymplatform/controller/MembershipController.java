package com.gymplatform.controller;

import com.gymplatform.service.CurrentUserService;
import com.gymplatform.service.MembershipPassService;
import com.gymplatform.service.MembershipService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {
    private final CurrentUserService currentUserService;
    private final MembershipService membershipService;
    private final MembershipPassService membershipPassService;

    public MembershipController(
            CurrentUserService currentUserService,
            MembershipService membershipService,
            MembershipPassService membershipPassService
    ) {
        this.currentUserService = currentUserService;
        this.membershipService = membershipService;
        this.membershipPassService = membershipPassService;
    }

    @GetMapping("/me")
    MembershipService.MembershipView membership(Authentication authentication) {
        return membershipService.getFor(currentUserService.require(authentication));
    }

    @GetMapping("/me/pass")
    MembershipPassService.PassResponse pass(Authentication authentication) {
        return membershipPassService.issue(currentUserService.require(authentication));
    }
}
