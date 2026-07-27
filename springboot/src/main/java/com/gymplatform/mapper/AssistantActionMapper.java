package com.gymplatform.mapper;

import com.gymplatform.domain.AssistantAction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;

public interface AssistantActionMapper {
    @Insert("""
            INSERT INTO assistant_actions
                (id, member_id, action_type, session_id, summary, expires_at)
            VALUES (#{id}, #{memberId}, 'BOOK', #{sessionId}, #{summary}, #{expiresAt})
            """)
    void insertBook(
            @Param("id") String id,
            @Param("memberId") Long memberId,
            @Param("sessionId") Long sessionId,
            @Param("summary") String summary,
            @Param("expiresAt") Instant expiresAt
    );

    @Insert("""
            INSERT INTO assistant_actions
                (id, member_id, action_type, booking_id, summary, expires_at)
            VALUES (#{id}, #{memberId}, 'CANCEL', #{bookingId}, #{summary}, #{expiresAt})
            """)
    void insertCancel(
            @Param("id") String id,
            @Param("memberId") Long memberId,
            @Param("bookingId") Long bookingId,
            @Param("summary") String summary,
            @Param("expiresAt") Instant expiresAt
    );

    @Select("""
            SELECT id, member_id, action_type, session_id, booking_id,
                   summary, status, expires_at
            FROM assistant_actions
            WHERE id = #{id}
            FOR UPDATE
            """)
    AssistantAction findByIdForUpdate(String id);

    @Update("""
            UPDATE assistant_actions
            SET status = 'EXECUTED', executed_at = CURRENT_TIMESTAMP
            WHERE id = #{id} AND status = 'PENDING'
            """)
    int markExecuted(String id);

    @Update("""
            UPDATE assistant_actions
            SET status = 'EXPIRED'
            WHERE id = #{id} AND status = 'PENDING'
            """)
    void markExpired(String id);
}
