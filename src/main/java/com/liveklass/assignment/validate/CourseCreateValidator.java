package com.liveklass.assignment.validate;

import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.dto.CourseCreateRequest;

import java.time.LocalDateTime;

public class CourseCreateValidator {

    // 강의 개설 검증
    public static void validateCreateRequest(CourseCreateRequest request) {
        // 1. 제목 검증
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("[ERROR] 강의 제목은 필수입니다.");
        }

        // 2. 설명 길이 검증
        if (request.description() != null && request.description().length() > 200) {
            throw new IllegalArgumentException("[ERROR] 설명은 200자 이내로 입력해주세요.");
        }

        // 3. 가격 검증
        if (request.price() < 0) {
            throw new IllegalArgumentException("[ERROR] 가격은 0원 이상이어야 합니다.");
        }

        // 4. 인원 검증
        if (request.maxCapacity() == null || request.maxCapacity() < 1) {
            throw new IllegalArgumentException("[ERROR] 최대 인원은 최소 1명 이상이어야 합니다.");
        }

        // 5. 날짜 누락 및 순서 검증
        if (request.startDate() == null || request.endDate() == null) {
            throw new IllegalArgumentException("[ERROR] 강의 시작일과 종료일을 입력해주세요.");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("[ERROR] 종료일은 시작일 이후여야 합니다.");
        }
    }


    // 강의 오픈 가능 여부 검증
    public static void validateOpen(Course course) {
        if (course.getStartDate() == null || course.getEndDate() == null) {
            throw new IllegalStateException("[ERROR] 강의 기간이 설정되지 않았습니다.");
        }
        if (LocalDateTime.now().isAfter(course.getStartDate())) {
            throw new IllegalStateException("[ERROR] 강의 시작일이 지난 강의는 오픈할 수 없습니다.");
        }
    }
}