package com.gymplatform.controller;

import com.gymplatform.domain.Course;
import com.gymplatform.domain.SessionView;
import com.gymplatform.mapper.CourseMapper;
import com.gymplatform.mapper.SessionMapper;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CourseMapper courseMapper;
    private final SessionMapper sessionMapper;

    public CatalogController(CourseMapper courseMapper, SessionMapper sessionMapper) {
        this.courseMapper = courseMapper;
        this.sessionMapper = sessionMapper;
    }

    @GetMapping("/courses")
    List<Course> courses(@RequestParam(required = false) String query) {
        return courseMapper.listActive(query == null ? null : query.trim());
    }

    @GetMapping("/courses/{id}")
    Course course(@PathVariable Long id) {
        return courseMapper.findById(id);
    }

    @GetMapping("/sessions")
    List<SessionView> sessions(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long coachId
    ) {
        var start = from == null ? Instant.now() : from;
        var end = to == null ? start.plus(30, ChronoUnit.DAYS) : to;
        return sessionMapper.listAvailable(start, end, courseId, coachId);
    }
}
