package com.gymplatform.service;

import com.gymplatform.domain.ActionProposal;
import com.gymplatform.domain.BookingView;
import com.gymplatform.domain.Course;
import com.gymplatform.domain.SessionView;
import com.gymplatform.mapper.BookingMapper;
import com.gymplatform.mapper.CourseMapper;
import com.gymplatform.mapper.SessionMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingAgentTools {
    private final Long memberId;
    private final CourseMapper courseMapper;
    private final SessionMapper sessionMapper;
    private final BookingMapper bookingMapper;
    private final AssistantActionService actionService;
    private ActionProposal latestProposal;

    public BookingAgentTools(
            Long memberId,
            CourseMapper courseMapper,
            SessionMapper sessionMapper,
            BookingMapper bookingMapper,
            AssistantActionService actionService
    ) {
        this.memberId = memberId;
        this.courseMapper = courseMapper;
        this.sessionMapper = sessionMapper;
        this.bookingMapper = bookingMapper;
        this.actionService = actionService;
    }

    @Tool(description = "Search active gym courses by name or description. Use an empty query to list every course.")
    public List<Course> searchCourses(
            @ToolParam(description = "Optional course search text") String query
    ) {
        return courseMapper.listActive(query);
    }

    @Tool(description = "Find open course sessions in the next 30 days. The course id is optional.")
    public List<SessionView> findSessions(
            @ToolParam(description = "Course id, or null to search all courses", required = false) Long courseId
    ) {
        return sessionMapper.listAvailable(
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                courseId,
                null
        );
    }

    @Tool(description = "List the signed-in member's bookings.")
    public List<BookingView> getMyBookings() {
        return bookingMapper.listForMember(memberId);
    }

    @Tool(description = "Prepare a booking for user confirmation. This does not create a booking.")
    public ActionProposal proposeBooking(
            @ToolParam(description = "Exact session id returned by findSessions") Long sessionId
    ) {
        latestProposal = actionService.proposeBook(memberId, sessionId);
        return latestProposal;
    }

    @Tool(description = "Prepare cancellation of the member's own booking for confirmation. This does not cancel it.")
    public ActionProposal proposeCancellation(
            @ToolParam(description = "Exact booking id returned by getMyBookings") Long bookingId
    ) {
        latestProposal = actionService.proposeCancel(memberId, bookingId);
        return latestProposal;
    }

    public ActionProposal latestProposal() {
        return latestProposal;
    }
}
