package com.gymplatform.mapper;

import com.gymplatform.domain.BookingView;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BookingMapper {
    @Select("""
            SELECT COUNT(*)
            FROM bookings
            WHERE session_id = #{sessionId} AND status = 'CONFIRMED'
            """)
    int countConfirmed(Long sessionId);

    @Select("""
            SELECT COUNT(*)
            FROM bookings
            WHERE session_id = #{sessionId} AND member_id = #{memberId}
              AND status = 'CONFIRMED'
            """)
    int countConfirmedForMember(
            @Param("sessionId") Long sessionId,
            @Param("memberId") Long memberId
    );

    @Insert("""
            INSERT INTO bookings (session_id, member_id, status)
            VALUES (#{sessionId}, #{memberId}, 'CONFIRMED')
            ON DUPLICATE KEY UPDATE status = 'CONFIRMED', updated_at = CURRENT_TIMESTAMP
            """)
    void confirm(@Param("sessionId") Long sessionId, @Param("memberId") Long memberId);

    @Update("""
            UPDATE bookings
            SET status = 'CANCELLED'
            WHERE id = #{bookingId} AND member_id = #{memberId} AND status = 'CONFIRMED'
            """)
    int cancel(@Param("bookingId") Long bookingId, @Param("memberId") Long memberId);

    @Select("""
            SELECT b.id, b.session_id, c.name AS course_name,
                   coach.display_name AS coach_name,
                   s.starts_at, s.ends_at, b.status, b.created_at
            FROM bookings b
            JOIN course_sessions s ON s.id = b.session_id
            JOIN courses c ON c.id = s.course_id
            LEFT JOIN users coach ON coach.id = s.coach_id
            WHERE b.member_id = #{memberId}
            ORDER BY s.starts_at
            """)
    List<BookingView> listForMember(Long memberId);
}
