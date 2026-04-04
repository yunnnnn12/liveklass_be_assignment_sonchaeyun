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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classmate_id")
    private Classmate classmate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus enrollmentStatus;

    private LocalDateTime enrolledDate;

    public void changeStatus(EnrollmentStatus newStatus){
        this.enrollmentStatus = newStatus;

    }

}
