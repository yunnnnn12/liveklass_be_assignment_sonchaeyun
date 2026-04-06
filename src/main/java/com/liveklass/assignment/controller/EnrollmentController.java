package com.liveklass.assignment.controller;

import com.liveklass.assignment.dto.EnrollmentRequest;
import com.liveklass.assignment.dto.EnrollmentResponse;
import com.liveklass.assignment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // 1. 수강 신청
    @PostMapping("/enroll")
    public Long enroll(@RequestBody EnrollmentRequest request) {
        return enrollmentService.enroll(request.getCourseId(), request.getUserName());
    }

    // 2. 결제 확정 (상태 변경) - 신청 후 결제가 완료되어야 수강 확정
    @PatchMapping("/{enrollmentId}/confirm")
    public ResponseEntity<Void> confirmEnrollment(@PathVariable(name = "enrollmentId") Long enrollmentId) {
        enrollmentService.confirmEnrollment(enrollmentId);
        return ResponseEntity.ok().build();
    }

    // 3. 내 수강 신청 목록 조회
    // 수강생 기준
    @GetMapping("/me")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(@RequestParam(name = "userId") Long userId) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(userId));
    }

    // 4. 수강 취소
    @PatchMapping("/{enrollmentId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable(name = "enrollmentId") Long enrollmentId) {
        enrollmentService.cancelEnrollment(enrollmentId);

        return ResponseEntity.ok().build();
    }
}