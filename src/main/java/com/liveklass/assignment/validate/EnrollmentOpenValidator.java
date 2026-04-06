package com.liveklass.assignment.validate;

import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.CourseStatus;
import java.time.LocalDateTime;

public class EnrollmentOpenValidator {

    // 강의 오픈 가능 여부 검증
    public static void validateOpen(Course course) {
        // 1. 날짜 설정 누락 체크
        if (course.getStartDate() == null || course.getEndDate() == null) {
            throw new IllegalStateException("[ERROR] 강의 기간이 설정되지 않았습니다.");
        }

        // 2. 과거 날짜 오픈 방지
        if (LocalDateTime.now().isAfter(course.getStartDate())) {
            throw new IllegalStateException("[ERROR] 강의 시작일이 지난 강의는 오픈할 수 없습니다.");
        }
    }

    // 수강 신청 가능 여부 검증
    public static void validateEnrollment(Course course) {
        // 1. 상태 체크
        if (course.getClassStatus() != CourseStatus.OPEN) {
            throw new IllegalStateException("[ERROR] 현재 수강 신청 가능한 상태가 아닙니다.");
        }

        // 2. 기간 체크
        if (LocalDateTime.now().isAfter(course.getEndDate())) {
            throw new IllegalStateException("[ERROR] 이미 강의 기간이 종료되었습니다.");
        }

        // 3. 정원 체크
        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            throw new IllegalStateException("[ERROR] 수강 정원이 초과되었습니다.");
        }
    }
}