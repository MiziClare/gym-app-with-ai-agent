package com.gymplatform.mapper;

import com.gymplatform.domain.CourseSession;
import com.gymplatform.domain.SessionView;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.List;

public interface SessionMapper {
    @Select("""
            <script>
            SELECT s.id, s.course_id, c.name AS course_name,
                   s.coach_id, u.display_name AS coach_name,
                   s.starts_at, s.ends_at, s.capacity,
                   SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS booked_count,
                   s.status
            FROM course_sessions s
            JOIN courses c ON c.id = s.course_id
            JOIN users u ON u.id = s.coach_id
            LEFT JOIN bookings b ON b.session_id = s.id
            WHERE s.status = 'OPEN'
              AND s.starts_at &gt;= #{from}
              AND s.starts_at &lt; #{to}
            <if test="courseId != null">AND s.course_id = #{courseId}</if>
            <if test="coachId != null">AND s.coach_id = #{coachId}</if>
            GROUP BY s.id, s.course_id, c.name, s.coach_id, u.display_name,
                     s.starts_at, s.ends_at, s.capacity, s.status
            ORDER BY s.starts_at
            </script>
            """)
    List<SessionView> listAvailable(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("courseId") Long courseId,
            @Param("coachId") Long coachId
    );

    @Select("""
            SELECT id, course_id, coach_id, starts_at, ends_at, capacity, status
            FROM course_sessions
            WHERE id = #{id}
            FOR UPDATE
            """)
    CourseSession findByIdForUpdate(Long id);
}
