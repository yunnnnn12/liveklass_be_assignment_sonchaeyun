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

        // 1. 이미 확정된 경우
        if (this.enrollmentStatus == EnrollmentStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 수강입니다.");
        }

        // 2. 취소된 건 다시 확정 불가
        if (this.enrollmentStatus == EnrollmentStatus.CANCELED) {
            throw new IllegalStateException("취소된 수강은 다시 확정할 수 없습니다.");
        }

        // 3. 정상 상태일 때만 변경
        this.enrollmentStatus = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // 수강 취소
    public void cancel() {

        // 1. 이미 취소된 경우
        if (this.enrollmentStatus == EnrollmentStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 수강입니다.");
        }

        // 2. 확정된 상태만 취소 가능
        if (this.enrollmentStatus != EnrollmentStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 수강만 취소할 수 있습니다.");
        }

        // 3. 7일 제한
        if (this.confirmedAt == null ||
                this.confirmedAt.isBefore(LocalDateTime.now().minusDays(7))) {
            throw new IllegalStateException("7일이 경과하여 취소가 불가능합니다.");
        }

        // 4. 상태 변경
        this.enrollmentStatus = EnrollmentStatus.CANCELED;
    }

    public void changeStatus(EnrollmentStatus newStatus) {
        this.enrollmentStatus = newStatus;
    }
}