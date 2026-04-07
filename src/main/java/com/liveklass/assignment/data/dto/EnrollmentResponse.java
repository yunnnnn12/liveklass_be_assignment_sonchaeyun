package com.liveklass.assignment.data.dto;

import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.Enrollment;
import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long enrollmentId,
        Long courseId,
        String userName,
        String courseTitle,
        EnrollmentStatus status,
        LocalDateTime createdAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getClassmate().getName(),
                enrollment.getCourse().getTitle(),
                enrollment.getEnrollmentStatus(),
                enrollment.getEnrolledDate()
        );
    }
}