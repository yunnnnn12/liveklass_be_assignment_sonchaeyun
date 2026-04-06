package com.liveklass.assignment.service;

import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.Classmate;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.entity.Enrollment;
import com.liveklass.assignment.data.entity.Waitlist;
import com.liveklass.assignment.dto.EnrollmentResponse;
import com.liveklass.assignment.repository.ClassmateRepository;
import com.liveklass.assignment.repository.CourseRepository;
import com.liveklass.assignment.repository.EnrollmentRepository;
import com.liveklass.assignment.repository.WaitlistRepository;
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
        // 강의 조회 (락 없이 일반 조회)
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 대기열 체크: 정원이 가득 찼다면 대기열 저장 후 안내
        if (course.getCurrentCount() >= course.getMaxCapacity()) {
            Waitlist wait = waitlistRepository.save(Waitlist.builder()
                    .course(course).userName(userName).createdAt(LocalDateTime.now()).build());
            throw new IllegalStateException("정원 초과로 대기열 " + wait.getId() + "번에 등록되었습니다.");
        }

        course.validateAvailable(); // 강의 상태, 강의 기간, 정원 체크

        Classmate classmate = classmateRepository.save(
                Classmate.builder()
                        .name(userName)
                        .build()
        );

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
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 없습니다."));

        Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getId()) // 락 대기
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // [변경] 엔티티 내부에서 상태 변경과 시간 기록을 동시에 처리
        enrollment.confirm();
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

        // 이미 취소된 상태인지 체크
        if (enrollment.getEnrollmentStatus() == EnrollmentStatus.CANCELED) { // 철자 주의: CANCELED -> CANCELLED (Enum 확인)
            throw new IllegalStateException("[ERROR] 이미 취소된 수강 신청입니다.");
        }

        // 7일 이내 취소 제한 체크
        if (enrollment.getEnrollmentStatus() == EnrollmentStatus.CONFIRMED) {
            if (enrollment.getConfirmedAt() != null &&
                    enrollment.getConfirmedAt().isBefore(LocalDateTime.now().minusDays(7))) {
                throw new IllegalStateException("결제 확정 후 7일이 경과하여 취소가 불가능합니다.");
            }
            enrollment.getCourse().decreaseCurrentCount();
        }

        // 엔티티 내부 메서드 호출
        enrollment.cancel();
    }

}
