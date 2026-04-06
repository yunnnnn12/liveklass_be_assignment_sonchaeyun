package com.liveklass.assignment.controller;

import com.liveklass.assignment.dto.EnrollmentRequest;
import com.liveklass.assignment.dto.EnrollmentResponse;
import com.liveklass.assignment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // 수강 신청 (대기열 발생 시 202 Accepted 반환)
    @PostMapping("/enroll")
    public ResponseEntity<String> enroll(@RequestBody EnrollmentRequest request) {
        try {
            Long id = enrollmentService.enroll(request.getCourseId(), request.getUserName());
            return ResponseEntity.ok("신청 성공 ID: " + id);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(e.getMessage());
        }
    }

    // 내 목록 조회 (페이징: /me?userId=1&page=0&size=5)
    @GetMapping("/me")
    public ResponseEntity<Page<EnrollmentResponse>> getMyEnrollments(
            @RequestParam(name = "userId") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(userId, pageable));
    }

    // 특정 강의 수강생 목록 (크리에이터용)
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<Page<EnrollmentResponse>> getCourseStudents(
            @PathVariable(name = "courseId") Long courseId,
            Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getCourseStudents(courseId, pageable));
    }

    @PatchMapping("/{enrollmentId}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long enrollmentId) {
        enrollmentService.confirmEnrollment(enrollmentId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{enrollmentId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long enrollmentId) {
        enrollmentService.cancelEnrollment(enrollmentId);
        return ResponseEntity.ok().build();
    }
}