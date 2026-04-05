package com.liveklass.assignment.service;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.Classmate;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.entity.Enrollment;
import com.liveklass.assignment.dto.EnrollmentResponse;
import com.liveklass.assignment.repository.ClassmateRepository;
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
    private final ClassmateRepository classmateRepository;

    // 1. 수강 신청
    @Transactional
    public Long enroll(Long courseId, String userName) {

        // 강의 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (course.getClassStatus() != CourseStatus.OPEN) {
            throw new IllegalStateException("현재 수강 신청 가능한 상태가 아닙니다. (강의 상태: " + course.getClassStatus() + ")");
        }


        Classmate classmate = classmateRepository.save(
                Classmate.builder()
                        .name(userName)
                        .build()
        );

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .classmate(classmate)
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

        enrollment.getCourse().increaseCurrentCount(); // 강의 인원 증가
    }

    // 3. 내 수강 신청 목록 조회
    public List<EnrollmentResponse> getMyEnrollments(Long userId) {
        return enrollmentRepository.findAllByClassmate_Id(userId).stream()
                .map(EnrollmentResponse::from) // DTO로 변환
                .toList();
    }

    // 4. 수강 신청 취소
    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 수강 신청 내역을 찾을 수 없습니다."));

        if (enrollment.getEnrollmentStatus() == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 수강 신청입니다.");
        }

        if (enrollment.getEnrollmentStatus() == EnrollmentStatus.CONFIRMED) {
            enrollment.getCourse().decreaseCurrentCount();
        }

        enrollment.changeStatus(EnrollmentStatus.CANCELLED);
    }
}
