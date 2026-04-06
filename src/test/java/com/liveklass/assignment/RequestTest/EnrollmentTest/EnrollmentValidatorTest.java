package com.liveklass.assignment.RequestTest.EnrollmentTest;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.validate.EnrollmentValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EnrollmentValidatorTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime nextMonth = now.plusMonths(1);

    @DisplayName("수강신청 실패: 강의 상태가 OPEN이 아니면 예외가 발생한다.")
    @Test
    void validateEnrollment_Fail_Status() {
        Course course = Course.builder()
                .classStatus(CourseStatus.DRAFT)
                .build();

        assertThatThrownBy(() -> EnrollmentValidator.validateEnrollment(course))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("[ERROR] 현재 수강 신청 가능한 상태가 아닙니다.");
    }

    @DisplayName("수강신청 실패: 종료일이 지났으면 예외가 발생한다.")
    @Test
    void validateEnrollment_Fail_Date() {

        Course course = Course.builder()
                .classStatus(CourseStatus.OPEN)
                .endDate(now.minusDays(1))
                .build();

        assertThatThrownBy(() -> EnrollmentValidator.validateEnrollment(course))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("[ERROR] 이미 강의 기간이 종료되었습니다.");
    }

    @DisplayName("수강신청 실패: 정원이 초과되었으면 예외가 발생한다.")
    @Test
    void validateEnrollment_Fail_Capacity() {
        Course course = Course.builder()
                .classStatus(CourseStatus.OPEN)
                .endDate(nextMonth)
                .maxCapacity(10)
                .currentCount(10)
                .build();

        assertThatThrownBy(() -> EnrollmentValidator.validateEnrollment(course))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("[ERROR] 수강 정원이 초과되었습니다.");
    }
}