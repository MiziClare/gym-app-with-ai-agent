package com.gymplatform.mapper;

import com.gymplatform.domain.Course;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseMapper {
    @Select("""
            <script>
            SELECT id, name, description, duration_minutes, default_capacity, cover_key, active
            FROM courses
            WHERE active = TRUE
            <if test="query != null and query != ''">
              AND (LOWER(name) LIKE CONCAT('%', LOWER(#{query}), '%')
                   OR LOWER(description) LIKE CONCAT('%', LOWER(#{query}), '%'))
            </if>
            ORDER BY name
            </script>
            """)
    List<Course> listActive(@Param("query") String query);

    @Select("""
            SELECT id, name, description, duration_minutes, default_capacity, cover_key, active
            FROM courses
            WHERE id = #{id}
            """)
    Course findById(Long id);
}
