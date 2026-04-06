package com.liveklass.assignment.data.entity;

import com.liveklass.assignment.data.EnrollmentStatus;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classmate_id")
    private Classmate classmate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // 2. 상태 및 날짜
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus enrollmentStatus;

    private LocalDateTime enrolledDate;

    public void changeStatus(EnrollmentStatus newStatus) {
        this.enrollmentStatus = newStatus;
    }
}