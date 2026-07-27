package com.gymplatform.controller;

import com.gymplatform.domain.BookingView;
import com.gymplatform.service.BookingService;
import com.gymplatform.service.CurrentUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final CurrentUserService currentUserService;
    private final BookingService bookingService;

    public BookingController(CurrentUserService currentUserService, BookingService bookingService) {
        this.currentUserService = currentUserService;
        this.bookingService = bookingService;
    }

    @GetMapping("/me")
    List<BookingView> mine(Authentication authentication) {
        return bookingService.listForMember(currentUserService.require(authentication).id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void book(@Valid @RequestBody CreateBookingRequest body, Authentication authentication) {
        var member = currentUserService.require(authentication);
        bookingService.book(body.sessionId(), member.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@PathVariable Long id, Authentication authentication) {
        bookingService.cancel(id, currentUserService.require(authentication).id());
    }

    public record CreateBookingRequest(@NotNull Long sessionId) {
    }
}
