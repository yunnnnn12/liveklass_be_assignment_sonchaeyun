package com.liveklass.assignment.ServiceTest;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.entity.Enrollment;
import com.liveklass.assignment.repository.CourseRepository;
import com.liveklass.assignment.repository.EnrollmentRepository;
import com.liveklass.assignment.service.CourseService;
import com.liveklass.assignment.service.EnrollmentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseService courseService;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    @DisplayName("수강 신청 후 결제 확정까지 완료하면 인원이 1명 증가해야 한다.")
    void serviceEnrollSuccess() {
        // 1. Given
        Course course = courseRepository.save(Course.builder()
                .title("클린코드 강의").maxCapacity(10).currentCount(0)
                .classStatus(CourseStatus.OPEN)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .build());

        // 2. When
        // 신청서 먼저 작성 (상태: PENDING, 인원: 0)
        Long enrollmentId = enrollmentService.enroll(course.getId(), "userName");

        // 결제 확정 실행 (상태: CONFIRMED, 인원: 1)
        enrollmentService.confirmEnrollment(enrollmentId);

        // 3. Then
        Course updatedCourse = courseRepository.findById(course.getId()).get();
        assertThat(updatedCourse.getCurrentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("서비스를 통해 강의를 개설하면 DB의 상태가 OPEN으로 업데이트되어야 한다.")
    void openCourse_Success() {
        // 1. Given
        Course course = courseRepository.save(Course.builder()
                .title("스프링 마스터 클래스")
                .classStatus(CourseStatus.DRAFT)
                .maxCapacity(20)
                .currentCount(0)
                .startDate(LocalDateTime.now().plusDays(2)) // 미래 시작
                .endDate(LocalDateTime.now().plusDays(10))
                .build());

        // 2. When
        courseService.openCourse(course.getId());

        // 3. Then
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(updatedCourse.getClassStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("강의 시작일이 지난 강의를 개설하려고 하면 서비스에서 예외가 발생한다.")
    void openCourse_Fail_PastDate() {
        // 1. Given
        Course course = courseRepository.save(Course.builder()
                .title("지나간 강의")
                .classStatus(CourseStatus.DRAFT)
                .startDate(LocalDateTime.now().minusDays(1)) // 어제
                .endDate(LocalDateTime.now().plusDays(5))
                .build());

        // 2. When & Then
        assertThatThrownBy(() -> courseService.openCourse(course.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("강의 시작일이 지난 강의는 오픈할 수 없습니다.");
    }

    @Test
    @DisplayName("결제 확정 시: 상태는 CONFIRMED로, 강의 인원은 +1 되어야 한다.")
    void confirmEnrollment_Success() {
        // 1. Given
        Course course = courseRepository.save(Course.builder()
                .title("테스트 강의").maxCapacity(10).currentCount(0)
                .classStatus(CourseStatus.OPEN)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .build());

        Long enrollmentId = enrollmentService.enroll(course.getId(), "userName");

        // 2. When
        enrollmentService.confirmEnrollment(enrollmentId);

        // 3. Then
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(updatedCourse.getCurrentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("수강 취소 시: 상태는 CANCELLED로, 강의 인원은 -1 되어야 한다.")
    void cancelEnrollment_Success() {
        // 1. Given
        Course course = courseRepository.save(Course.builder()
                .title("테스트 강의").maxCapacity(10).currentCount(0)
                .classStatus(CourseStatus.OPEN)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .build());

        Long enrollmentId = enrollmentService.enroll(course.getId(), "userName");
        enrollmentService.confirmEnrollment(enrollmentId);

        // 2. When
        enrollmentService.cancelEnrollment(enrollmentId);

        // 3. Then
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        Course updatedCourse = courseRepository.findById(course.getId()).orElseThrow();

        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CANCELED);
        assertThat(updatedCourse.getCurrentCount()).isEqualTo(0);
    }


}