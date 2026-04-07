package com.liveklass.assignment.data.dto;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.entity.Course;

public record CourseResponse(
        Long id,
        String title,
        Integer price,
        Integer maxCapacity,
        Integer currentCount,
        CourseStatus status
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getPrice(),
                course.getMaxCapacity(),
                course.getCurrentCount(),
                course.getClassStatus()
        );
    }
}