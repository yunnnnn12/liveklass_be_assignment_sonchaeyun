package com.liveklass.assignment.validate;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.entity.Course;

import java.time.LocalDateTime;

public class EnrollmentValidator {
    // 수강 신청 가능 여부 통합 검증
    public static void validateEnrollment(Course course) {
        // 1. 상태 체크
        if (course.getClassStatus() != CourseStatus.OPEN) {
            throw new IllegalStateException("[ERROR] 현재 수강 신청 가능한 상태가 아닙니다. (상태: " + course.getClassStatus() + ")");
        }

        // 2. 기간 체크
        if (LocalDateTime.now().isAfter(course.getEndDate())) {
            throw new IllegalStateException("[ERROR] 이미 강의 기간이 종료되었습니다. (종료일: " + course.getEndDate() + ")");
        }

        // 3. 정원 체크
        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            throw new IllegalStateException("[ERROR] 수강 정원이 초과되었습니다.");
        }
    }
}
