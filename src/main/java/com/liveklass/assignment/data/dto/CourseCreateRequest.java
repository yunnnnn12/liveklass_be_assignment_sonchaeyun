package com.liveklass.assignment.dto;

import java.time.LocalDateTime;

public record CourseCreateRequest (
        String title,
        String description,
        int price,
        Integer maxCapacity,
        LocalDateTime startDate,
        LocalDateTime endDate
){}
