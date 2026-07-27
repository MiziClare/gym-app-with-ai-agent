package com.gymplatform.service;

import com.gymplatform.domain.AssistantAction;
import com.gymplatform.mapper.AssistantActionMapper;
import com.gymplatform.mapper.BookingMapper;
import com.gymplatform.mapper.SessionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssistantActionServiceTest {
    @Mock AssistantActionMapper actionMapper;
    @Mock SessionMapper sessionMapper;
    @Mock BookingMapper bookingMapper;
    @Mock BookingService bookingService;

    @Test
    void confirmationIsBoundToTheMemberWhoCreatedTheProposal() {
        var action = new AssistantAction(
                "action-1", 12L, "BOOK", 3L, null, "Book class",
                "PENDING", Instant.now().plusSeconds(60)
        );
        when(actionMapper.findByIdForUpdate("action-1")).thenReturn(action);
        var service = new AssistantActionService(actionMapper, sessionMapper, bookingMapper, bookingService);

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.confirm("action-1", 99L)
        );

        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(bookingService);
        verify(actionMapper, never()).markExecuted(anyString());
    }
}
