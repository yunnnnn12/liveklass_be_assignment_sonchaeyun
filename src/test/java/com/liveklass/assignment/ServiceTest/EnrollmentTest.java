package com.liveklass.assignment.ServiceTest;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.*;
import com.liveklass.assignment.data.dto.EnrollmentResponse;
import com.liveklass.assignment.data.repository.ClassmateRepository;
import com.liveklass.assignment.data.repository.CourseRepository;
import com.liveklass.assignment.data.repository.EnrollmentRepository;
import com.liveklass.assignment.data.repository.WaitlistRepository;
import com.liveklass.assignment.service.CourseService;
import com.liveklass.assignment.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EnrollmentTest {

    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseService courseService;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ClassmateRepository classmateRepository;
    @Autowired private WaitlistRepository waitlistRepository;

    // --- 1. 기본 수강 신청 및 확정 로직 테스트 ---

    @Test
    @DisplayName("수강 신청 후 결제 확정까지 완료하면 인원이 1명 증가해야 한다.")
    void serviceEnrollSuccess() {
        Course course = createOpenCourse("기본 강의", 10);
        Long enrollmentId = enrollmentService.enroll(course.getId(), "userName");

        enrollmentService.confirmEnrollment(enrollmentId);

        Course updatedCourse = courseRepository.findById(course.getId()).get();
        assertThat(updatedCourse.getCurrentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("결제 확정 시: 상태는 CONFIRMED로 변경되어야 한다.")
    void confirmEnrollment_StatusCheck() {
        Course course = createOpenCourse("상태 체크 강의", 10);
        Long enrollmentId = enrollmentService.enroll(course.getId(), "userName");

        enrollmentService.confirmEnrollment(enrollmentId);

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    // --- 2. 취소 기한 테스트 (7일 제한 가산점) ---

    @Test
    @DisplayName("결제 확정 후 6일째에는 취소가 성공해야 한다.")
    void cancel_Success_Within7Days() {
        Enrollment enrollment = createEnrollmentWithConfirmedDate(LocalDateTime.now().minusDays(6));

        enrollmentService.cancelEnrollment(enrollment.getId());

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CANCELED);
    }

    @Test
    @DisplayName("결제 확정 후 8일이 지나면 취소 시 예외가 발생해야 한다.")
    void cancel_Fail_After7Days() {
        Enrollment enrollment = createEnrollmentWithConfirmedDate(LocalDateTime.now().minusDays(8));

        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(enrollment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7일이 경과하여 취소가 불가능합니다.");
    }

    // --- 3. 대기열(Waitlist) 테스트 ---

    @Test
    @DisplayName("정원이 초과된 강의에 신청하면 대기열에 등록되어야 한다.")
    void enroll_Waitlist_Success() {
        // 정원이 이미 찬 강의 생성
        Course course = courseRepository.save(Course.builder()
                .title("정원 초과 강의").maxCapacity(1).currentCount(1)
                .classStatus(CourseStatus.OPEN)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build());

        assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), "대기자A"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대기열");

        List<Waitlist> waitlists = waitlistRepository.findAll();
        assertThat(waitlists).isNotEmpty();
        assertThat(waitlists.get(0).getUserName()).isEqualTo("대기자A");
    }

    // --- 4. 페이지네이션 테스트 ---

    @Test
    @DisplayName("내 신청 내역을 5개씩 페이징하여 조회할 수 있다.")
    void getMyEnrollments_Pagination() {
        Classmate classmate = classmateRepository.save(Classmate.builder().name("userName").build());
        Course course = createOpenCourse("샘플강의", 20);

        for (int i = 0; i < 10; i++) {
            enrollmentRepository.save(Enrollment.builder()
                    .classmate(classmate).course(course)
                    .enrollmentStatus(EnrollmentStatus.PENDING).build());
        }

        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<EnrollmentResponse> result = enrollmentService.getMyEnrollments(classmate.getName(), pageRequest);

        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(10);
    }

    // --- 편의를 위한 헬퍼 메서드 ---
    private Course createOpenCourse(String title, int maxCapacity) {
        return courseRepository.save(Course.builder()
                .title(title).maxCapacity(maxCapacity).currentCount(0)
                .classStatus(CourseStatus.OPEN)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .build());
    }

    private Enrollment createEnrollmentWithConfirmedDate(LocalDateTime confirmedAt) {
        Classmate classmate = classmateRepository.save(Classmate.builder().name("userName").build());
        Course course = createOpenCourse("기한 테스트 강의", 10);
        course.increaseCurrentCount(); // 확정 상태를 가정하므로 인원 미리 증가

        Enrollment enrollment = Enrollment.builder()
                .classmate(classmate).course(course)
                .enrollmentStatus(EnrollmentStatus.CONFIRMED)
                .confirmedAt(confirmedAt)
                .build();

        return enrollmentRepository.save(enrollment);
    }
}