package com.gymplatform.service;

import com.gymplatform.domain.CourseSession;
import com.gymplatform.mapper.BookingMapper;
import com.gymplatform.mapper.SessionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    SessionMapper sessionMapper;
    @Mock
    BookingMapper bookingMapper;
    @Mock
    JdbcTemplate jdbc;

    @Test
    void booksOnlyWhenTheLockedSessionHasCapacity() {
        var service = new BookingService(sessionMapper, bookingMapper, jdbc);
        when(sessionMapper.findByIdForUpdate(7L)).thenReturn(
                new CourseSession(7L, 2L, 3L, Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(7200), 2, "OPEN")
        );
        when(jdbc.queryForObject(contains("gym_closed_days"), eq(Integer.class), any())).thenReturn(0);
        when(bookingMapper.countConfirmedForMember(7L, 11L)).thenReturn(0);
        when(bookingMapper.countConfirmed(7L)).thenReturn(1);

        service.book(7L, 11L);

        verify(bookingMapper).confirm(7L, 11L);
    }

    @Test
    void rejectsAFullSessionWithoutWriting() {
        var service = new BookingService(sessionMapper, bookingMapper, jdbc);
        when(sessionMapper.findByIdForUpdate(7L)).thenReturn(
                new CourseSession(7L, 2L, 3L, Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(7200), 1, "OPEN")
        );
        when(jdbc.queryForObject(contains("gym_closed_days"), eq(Integer.class), any())).thenReturn(0);
        when(bookingMapper.countConfirmedForMember(7L, 11L)).thenReturn(0);
        when(bookingMapper.countConfirmed(7L)).thenReturn(1);

        var error = assertThrows(ResponseStatusException.class, () -> service.book(7L, 11L));

        assertEquals(409, error.getStatusCode().value());
        verify(bookingMapper, never()).confirm(anyLong(), anyLong());
    }

    @Test
    void rejectsBookingOnClosedGymDay() {
        var service = new BookingService(sessionMapper, bookingMapper, jdbc);
        when(sessionMapper.findByIdForUpdate(7L)).thenReturn(
                new CourseSession(7L, 2L, 3L, Instant.now().plusSeconds(3600),
                        Instant.now().plusSeconds(7200), 2, "OPEN")
        );
        when(jdbc.queryForObject(contains("gym_closed_days"), eq(Integer.class), any())).thenReturn(1);

        var error = assertThrows(ResponseStatusException.class, () -> service.book(7L, 11L));

        assertEquals(409, error.getStatusCode().value());
        verifyNoInteractions(bookingMapper);
    }
}
