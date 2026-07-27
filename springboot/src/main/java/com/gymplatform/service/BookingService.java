package com.gymplatform.service;

import com.gymplatform.domain.BookingView;
import com.gymplatform.mapper.BookingMapper;
import com.gymplatform.mapper.SessionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class BookingService {
    private final SessionMapper sessionMapper;
    private final BookingMapper bookingMapper;

    public BookingService(SessionMapper sessionMapper, BookingMapper bookingMapper) {
        this.sessionMapper = sessionMapper;
        this.bookingMapper = bookingMapper;
    }

    @Transactional
    public void book(Long sessionId, Long memberId) {
        var session = sessionMapper.findByIdForUpdate(sessionId);
        if (session == null || !"OPEN".equals(session.status())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session is not available");
        }
        if (!session.startsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session has already started");
        }
        if (bookingMapper.countConfirmedForMember(sessionId, memberId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is already booked");
        }
        if (bookingMapper.countConfirmed(sessionId) >= session.capacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is full");
        }
        bookingMapper.confirm(sessionId, memberId);
    }

    @Transactional
    public void cancel(Long bookingId, Long memberId) {
        if (bookingMapper.cancel(bookingId, memberId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active booking not found");
        }
    }

    public List<BookingView> listForMember(Long memberId) {
        return bookingMapper.listForMember(memberId);
    }
}
