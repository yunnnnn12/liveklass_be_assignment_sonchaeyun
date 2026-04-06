package com.liveklass.assignment.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CourseCreateRequest (

        @NotBlank(message = "강의 제목은 필수입니다.")
        String title,

        @Size(max = 200, message = "설명은 200자 이내로 입력해주세요.")
        String description,

        @NotNull(message = "가격을 입력해주세요.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Long price,

        @NotNull(message = "최대 수강 인원을 입력해주세요.")
        @Min(value = 1, message = "최대 인원은 최소 1명 이상이어야 합니다.")
        Integer maxCapacity,

        @NotNull(message = "강의 시작일을 입력해주세요.")
        LocalDateTime startDate,

        @NotNull(message = "강의 종료일을 입력해주세요.")
        LocalDateTime endDate
){}
