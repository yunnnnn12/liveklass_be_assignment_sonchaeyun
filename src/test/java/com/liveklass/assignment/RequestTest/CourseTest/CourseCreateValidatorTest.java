package com.liveklass.assignment.RequestTest.CourseTest;


import com.liveklass.assignment.validate.CourseCreateValidator;
import com.liveklass.assignment.dto.CourseCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseCreateValidatorTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime nextMonth = now.plusMonths(1);

    @DisplayName("제목이 null이거나 공백이면 예외가 발생한다.")
    @Test
    void validateTitle() {
        // given
        CourseCreateRequest request = new CourseCreateRequest(
                "", "설명", 100000, 10, now, nextMonth
        );

        // when & then
        assertThatThrownBy(() -> CourseCreateValidator.validateCreateRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 강의 제목은 필수입니다.");
    }

    @DisplayName("설명이 200자를 초과하면 예외가 발생한다.")
    @Test
    void validateDescriptionLength() {
        // given
        String longDescription = "a".repeat(201);
        CourseCreateRequest request = new CourseCreateRequest(
                "제목", longDescription, 100000, 10, now, nextMonth
        );

        // when & then
        assertThatThrownBy(() -> CourseCreateValidator.validateCreateRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 설명은 200자 이내로 입력해주세요.");
    }

    @DisplayName("가격이 음수이면 예외가 발생한다.")
    @Test
    void validatePrice() {
        // given
        CourseCreateRequest request = new CourseCreateRequest(
                "제목", "설명", -100000, 10, now, nextMonth
        );

        // when & then
        assertThatThrownBy(() -> CourseCreateValidator.validateCreateRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 가격은 0원 이상이어야 합니다.");
    }

    @DisplayName("최대 인원이 1명 미만이면 예외가 발생한다.")
    @Test
    void validateMaxCapacity() {
        // given
        CourseCreateRequest request = new CourseCreateRequest(
                "제목", "설명", 100000, 0, now, nextMonth
        );

        // when & then
        assertThatThrownBy(() -> CourseCreateValidator.validateCreateRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 최대 인원은 최소 1명 이상이어야 합니다.");
    }

    @DisplayName("날짜가 누락되면 예외가 발생한다.")
    @Test
    void validateDateNull() {
        // given
        CourseCreateRequest request = new CourseCreateRequest(
                "제목", "설명", 100000, 10, null, nextMonth
        );

        // when & then
        assertThatThrownBy(() -> CourseCreateValidator.validateCreateRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 강의 시작일과 종료일을 입력해주세요.");
    }

    @DisplayName("종료일이 시작일보다 빠르면 예외가 발생한다.")
    @Test
    void validateDateOrder() {
        // given
        CourseCreateRequest request = new CourseCreateRequest(
                "제목", "설명", 100000, 10, nextMonth, now
        );

        // when & then
        assertThatThrownBy(() -> CourseCreateValidator.validateCreateRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 종료일은 시작일 이후여야 합니다.");
    }

}