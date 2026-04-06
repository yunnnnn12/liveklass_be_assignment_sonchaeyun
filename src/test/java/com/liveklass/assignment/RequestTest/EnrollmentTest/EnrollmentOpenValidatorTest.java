package com.liveklass.assignment.RequestTest.EnrollmentTest;

import com.liveklass.assignment.validate.EnrollmentOpenValidator;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EnrollmentOpenValidatorTest {

    // 테스트용 기준 시간 설정
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime nextMonth = now.plusMonths(1);

    @DisplayName("강의오픈 실패: 강의 시작일이 오늘보다 이전이면 예외가 발생한다.")
    @Test
    void validateOpen_Fail_PastStart() {
        // given
        Course course = Course.builder()
                .startDate(now.minusDays(1))
                .endDate(nextMonth)
                .build();

        // when & then
        assertThatThrownBy(() -> EnrollmentOpenValidator.validateOpen(course))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("[ERROR] 강의 시작일이 지난 강의는 오픈할 수 없습니다.");
    }

    @DisplayName("강의오픈 실패: 날짜 설정이 누락되었으면 예외가 발생한다.")
    @Test
    void validateOpen_Fail_NullDate() {
        // given
        Course course = Course.builder()
                .startDate(null)
                .endDate(nextMonth)
                .build();

        // when & then
        assertThatThrownBy(() -> EnrollmentOpenValidator.validateOpen(course))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("[ERROR] 강의 기간이 설정되지 않았습니다.");
    }

    @DisplayName("수강신청 실패: 정원이 초과되면 예외가 발생한다.")
    @Test
    void validateEnrollment_Fail_Capacity() {
        // given
        Course course = Course.builder()
                .classStatus(CourseStatus.OPEN)
                .maxCapacity(10)
                .currentCount(10)
                .endDate(nextMonth)
                .build();

        // when & then
        assertThatThrownBy(() -> EnrollmentOpenValidator.validateEnrollment(course))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("[ERROR] 수강 정원이 초과되었습니다.");
    }
}