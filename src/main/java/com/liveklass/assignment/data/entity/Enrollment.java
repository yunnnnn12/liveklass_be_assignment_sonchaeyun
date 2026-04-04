package com.liveklass.assignment.data.entity;

import com.liveklass.assignment.data.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수강생, 강의
    // 1. 수강생
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classmate_id")
    private Classmate classmate;

    // 2. 강의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus enrollmentStatus;

    private LocalDateTime enrolledDate;

}
