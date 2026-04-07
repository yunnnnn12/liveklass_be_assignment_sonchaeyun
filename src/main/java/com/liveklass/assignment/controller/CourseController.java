package com.liveklass.assignment.controller;

import com.liveklass.assignment.data.CourseStatus;

import com.liveklass.assignment.data.dto.CourseCreateRequest;
import com.liveklass.assignment.data.dto.CourseResponse;
import com.liveklass.assignment.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 1. 강의 개설
    @PostMapping
    public ResponseEntity<Long> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    // 2. 강의 목록 조회 (상태값 필터링 포함)
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses(@RequestParam(name = "status", required = false) CourseStatus status) {
        return ResponseEntity.ok(courseService.getCourses(status));
    }

    // 3. 강의 상세 조회
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> getCourseDetail(@PathVariable(name = "courseId") Long courseId) {
        return ResponseEntity.ok(courseService.getCourseDetail(courseId));
    }

    // 4. 강의 OPEN
    @PatchMapping("/{courseId}/open")
    public ResponseEntity<Void> openCourse(@PathVariable(name = "courseId") Long courseId) {
        courseService.openCourse(courseId);
        return ResponseEntity.ok().build();
    }

    // 5. 강의 CLOSED
    @PatchMapping("/{courseId}/close")
    public ResponseEntity<Void> closeCourse(@PathVariable(name = "courseId") Long courseId) {
        courseService.closeCourse(courseId);
        return ResponseEntity.ok().build();
    }
}