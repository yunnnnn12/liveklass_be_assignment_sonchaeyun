package com.liveklass.assignment.data.entity;

import com.liveklass.assignment.data.CourseStatus;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 기본 정보
    private String title;
    private String description;
    private int price;

    // 2. 인원 관련
    private int maxCapacity;
    private int currentCount;

    // 3. 날짜 및 상태
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CourseStatus classStatus = CourseStatus.DRAFT;

    public void open() {
        if (this.startDate == null || this.endDate == null) {
            throw new IllegalStateException("강의 기간이 설정되지 않았습니다.");
        }
        if (LocalDateTime.now().isAfter(this.startDate)) {
            throw new IllegalStateException("강의 시작일이 지난 강의는 오픈할 수 없습니다.");
        }
        if (this.currentCount >= this.maxCapacity) {
            throw new IllegalStateException("정원이 가득 차서 오픈할 수 없습니다.");
        }
        this.classStatus = CourseStatus.OPEN;
    }

    public void close() {
        this.classStatus = CourseStatus.CLOSED;
    }

    public void validateAvailable() {
        // 상태 체크
        if (this.classStatus != CourseStatus.OPEN) {
            throw new IllegalStateException("현재 수강 신청 가능한 상태가 아닙니다. (상태: " + this.classStatus + ")");
        }

        // 기간 체크 (현재 시간이 종료일을 지났는지)
        if (LocalDateTime.now().isAfter(this.endDate)) {
            throw new IllegalStateException("이미 강의 기간이 종료되었습니다. (종료일: " + this.endDate + ")");
        }

        // 정원 체크
        if (this.currentCount >= this.maxCapacity) {
            throw new IllegalStateException("수강 정원이 초과되었습니다.");
        }
    }

    public void increaseCurrentCount() {
        validateAvailable(); // 동시에 신청하는 것 위해 재검증
        this.currentCount++;
    }

    public void decreaseCurrentCount() {
        if (this.currentCount > 0) {
            this.currentCount--;

            if (this.classStatus == CourseStatus.CLOSED && this.currentCount < this.maxCapacity) {
                this.classStatus = CourseStatus.OPEN;
            }
        }
    }
}