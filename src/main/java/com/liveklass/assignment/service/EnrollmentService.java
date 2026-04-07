package com.liveklass.assignment.service;

import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.Classmate;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.entity.Enrollment;
import com.liveklass.assignment.data.entity.Waitlist;
import com.liveklass.assignment.data.dto.EnrollmentResponse;
import com.liveklass.assignment.data.repository.ClassmateRepository;
import com.liveklass.assignment.data.repository.CourseRepository;
import com.liveklass.assignment.data.repository.EnrollmentRepository;
import com.liveklass.assignment.data.repository.WaitlistRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ClassmateRepository classmateRepository;
    private final WaitlistRepository waitlistRepository;

    // 수강 신청
    @Transactional
    public Long enroll(Long courseId, String userName) {

        // 강의 조회
        Course course = courseRepository.findByIdWithLock(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 대기열 체크: 정원이 가득 찼다면 대기열 저장 후 안내
        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            Waitlist wait = waitlistRepository.save(Waitlist.builder()
                    .course(course).userName(userName).createdAt(LocalDateTime.now()).build());
            throw new IllegalStateException("정원 초과로 대기열 " + wait.getId() + "번에 등록되었습니다.");
        }

        course.validateAvailable(); // 강의 상태, 강의 기간, 정원 체크

        Classmate classmate = classmateRepository.findByName(userName)
                .orElseGet(() -> classmateRepository.save(
                        Classmate.builder().name(userName).build()
                ));

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .classmate(classmate)
                .enrollmentStatus(EnrollmentStatus.PENDING) // 초기에는 pending
                .enrolledDate(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment).getId();
    }

    // 2. 결제 확정 처리
    @Transactional
    public void confirmEnrollment(Long enrollmentId) {

        // 1. enrollment 조회
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 없습니다."));

        // 2. course 락 조회 (핵심 ⭐)
        Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 3. 상태 변경 (엔티티가 검증)
        enrollment.confirm();

        // 4. 정원 증가 (여기서 다시 체크됨)
        course.increaseCurrentCount();
    }

    // 3. 내 수강 신청 목록 조회
    public Page<EnrollmentResponse> getMyEnrollments(Long userId, Pageable pageable) {
        return enrollmentRepository.findAllByClassmate_Id(userId, pageable)
                .map(EnrollmentResponse::from);
    }

    // 4. 강의별 수강생 목록 조회 (크리에이터용)
    public Page<EnrollmentResponse> getCourseStudents(Long courseId, Pageable pageable) {
        return enrollmentRepository.findAllByCourse_IdAndEnrollmentStatus(courseId, EnrollmentStatus.CONFIRMED, pageable)
                .map(EnrollmentResponse::from);
    }

    // 5. 수강 신청 취소 (7일 제한 + 엔티티 cancel() 활용)
    @Transactional
    public void cancelEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 수강 신청 내역을 찾을 수 없습니다."));

        boolean wasConfirmed = enrollment.getEnrollmentStatus() == EnrollmentStatus.CONFIRMED;

        enrollment.cancel();

        if (wasConfirmed) {
            enrollment.getCourse().decreaseCurrentCount();
        }
    }

}
