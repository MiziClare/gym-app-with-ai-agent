package com.gymplatform.controller;

import com.gymplatform.service.MembershipPassService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/scans")
public class StaffScanController {
    private final MembershipPassService membershipPassService;

    public StaffScanController(MembershipPassService membershipPassService) {
        this.membershipPassService = membershipPassService;
    }

    @PostMapping("/resolve")
    MembershipPassService.ScanResult resolve(@Valid @RequestBody ScanRequest request) {
        return membershipPassService.resolve(request.token());
    }

    public record ScanRequest(@NotBlank @Size(max = 1000) String token) {}
}
