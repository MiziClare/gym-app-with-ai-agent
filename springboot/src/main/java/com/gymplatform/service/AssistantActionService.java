package com.gymplatform.service;

import com.gymplatform.domain.ActionProposal;
import com.gymplatform.domain.BookingView;
import com.gymplatform.mapper.AssistantActionMapper;
import com.gymplatform.mapper.BookingMapper;
import com.gymplatform.mapper.SessionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AssistantActionService {
    private final AssistantActionMapper actionMapper;
    private final SessionMapper sessionMapper;
    private final BookingMapper bookingMapper;
    private final BookingService bookingService;

    public AssistantActionService(
            AssistantActionMapper actionMapper,
            SessionMapper sessionMapper,
            BookingMapper bookingMapper,
            BookingService bookingService
    ) {
        this.actionMapper = actionMapper;
        this.sessionMapper = sessionMapper;
        this.bookingMapper = bookingMapper;
        this.bookingService = bookingService;
    }

    public ActionProposal proposeBook(Long memberId, Long sessionId) {
        var sessions = sessionMapper.listAvailable(
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                null,
                null
        );
        var session = sessions.stream()
                .filter(item -> item.id().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session is not available"));
        if (session.bookedCount() >= session.capacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is full");
        }

        var id = UUID.randomUUID().toString();
        var expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
        var summary = "Book %s with %s at %s".formatted(
                session.courseName(), session.coachName(), session.startsAt()
        );
        actionMapper.insertBook(id, memberId, sessionId, summary, expiresAt);
        return new ActionProposal(id, "BOOK", summary, expiresAt);
    }

    public ActionProposal proposeCancel(Long memberId, Long bookingId) {
        BookingView booking = bookingMapper.listForMember(memberId).stream()
                .filter(item -> item.id().equals(bookingId) && "CONFIRMED".equals(item.status()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active booking not found"));
        var id = UUID.randomUUID().toString();
        var expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
        var summary = "Cancel %s at %s".formatted(booking.courseName(), booking.startsAt());
        actionMapper.insertCancel(id, memberId, bookingId, summary, expiresAt);
        return new ActionProposal(id, "CANCEL", summary, expiresAt);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void confirm(String actionId, Long memberId) {
        var action = actionMapper.findByIdForUpdate(actionId);
        if (action == null || !action.memberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Action not found");
        }
        if (!"PENDING".equals(action.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Action has already been handled");
        }
        if (action.expiresAt().isBefore(Instant.now())) {
            actionMapper.markExpired(actionId);
            throw new ResponseStatusException(HttpStatus.GONE, "Action has expired");
        }

        if ("BOOK".equals(action.actionType())) {
            bookingService.book(action.sessionId(), memberId);
        } else {
            bookingService.cancel(action.bookingId(), memberId);
        }
        if (actionMapper.markExecuted(actionId) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Action has already been handled");
        }
    }
}
