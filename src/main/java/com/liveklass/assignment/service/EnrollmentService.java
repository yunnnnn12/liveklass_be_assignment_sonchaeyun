package com.liveklass.assignment.service;

import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.entity.Enrollment;
import com.liveklass.assignment.dto.EnrollmentResponse;
import com.liveklass.assignment.repository.CourseRepository;
import com.liveklass.assignment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    // 1. 수강 신청
    @Transactional
    public Long enroll(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            throw new IllegalStateException("수강 정원이 초과되었습니다. (정원: " + course.getMaxCapacity() + ")");
        }


        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .enrollmentStatus(EnrollmentStatus.PENDING)
                .enrolledDate(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment).getId();
    }

    // 2. 결제 확정 처리
    @Transactional
    public void confirmEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();

        enrollment.changeStatus(EnrollmentStatus.CONFIRMED);

        enrollment.getCourse().increaseCurrentCount();
    }

    // 3. 내 수강 신청 목록 조회
    public List<EnrollmentResponse> getMyEnrollments(Long userId) {
        return enrollmentRepository.findAllByUserId(userId).stream()
                .map(EnrollmentResponse::from) // DTO로 변환
                .toList();
    }
}
