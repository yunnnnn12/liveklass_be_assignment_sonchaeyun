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

    // 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classmate_id")
    private Classmate classmate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // 상태 및 날짜
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus enrollmentStatus;

    private LocalDateTime enrolledDate; // 신청 일시

    private LocalDateTime confirmedAt; // [추가] 결제 확정 일시 (취소 제한 기준)


    // 수강 신청 확정 (결제 완료)
    public void confirm() {
        this.enrollmentStatus = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // 수강 취소
    public void cancel() {
        // 취소 가능 여부 검증 로직은 서비스 레이어 혹은 여기서 호출 가능
        this.enrollmentStatus = EnrollmentStatus.CANCELED;
    }

    public void changeStatus(EnrollmentStatus newStatus) {
        this.enrollmentStatus = newStatus;
    }
}