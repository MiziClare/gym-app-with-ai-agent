package com.gymplatform.controller;

import com.gymplatform.service.CurrentUserService;
import com.gymplatform.service.StaffScanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/scans")
public class StaffScanController {
    private final StaffScanService staffScanService;
    private final CurrentUserService currentUserService;

    public StaffScanController(
            StaffScanService staffScanService,
            CurrentUserService currentUserService
    ) {
        this.staffScanService = staffScanService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/resolve")
    StaffScanService.StaffScanResult resolve(
            @Valid @RequestBody ScanRequest request,
            Authentication authentication
    ) {
        return staffScanService.resolve(
                request.token(),
                currentUserService.require(authentication)
        );
    }

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void checkIn(@Valid @RequestBody ScanRequest request, Authentication authentication) {
        staffScanService.checkIn(request.token(), currentUserService.require(authentication));
    }

    @PostMapping("/check-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void checkOut(@Valid @RequestBody ScanRequest request, Authentication authentication) {
        staffScanService.checkOut(request.token(), currentUserService.require(authentication));
    }

    public record ScanRequest(@NotBlank @Size(max = 1000) String token) {}
}
