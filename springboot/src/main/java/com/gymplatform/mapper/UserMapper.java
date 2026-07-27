package com.gymplatform.mapper;

import com.gymplatform.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {
    @Select("""
            SELECT id, username, password_hash, display_name, email, role, active, created_at
            FROM users
            WHERE username = #{username}
            """)
    User findByUsername(String username);

    @Select("""
            SELECT id, username, password_hash, display_name, email, role, active, created_at
            FROM users
            WHERE id = #{id}
            """)
    User findById(Long id);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username} OR email = #{email}")
    int countByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    @Insert("""
            INSERT INTO users (username, password_hash, display_name, email, role)
            VALUES (#{username}, #{passwordHash}, #{displayName}, #{email}, 'MEMBER')
            """)
    void insertMember(
            @Param("username") String username,
            @Param("passwordHash") String passwordHash,
            @Param("displayName") String displayName,
            @Param("email") String email
    );

    @Select("""
            SELECT id, username, password_hash, display_name, email, role, active, created_at
            FROM users
            ORDER BY created_at DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<User> list(@Param("limit") int limit, @Param("offset") int offset);
}
