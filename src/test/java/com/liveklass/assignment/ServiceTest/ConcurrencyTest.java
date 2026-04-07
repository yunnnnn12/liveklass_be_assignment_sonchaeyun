package com.liveklass.assignment.ServiceTest;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.repository.CourseRepository;
import com.liveklass.assignment.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("정원이 10명인 강의에 100명이 동시에 결제 확정을 시도하면, 정확히 10명만 성공해야 한다.")
    void concurrencyTest() throws InterruptedException {
        // 1. Given
        Course course = courseRepository.save(Course.builder()
                .title("동시성 테스트 강의")
                .maxCapacity(10)
                .currentCount(0)
                .classStatus(CourseStatus.OPEN)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .build());

        // 100개의 신청 내역(PENDING)미리 생성
        List<Long> enrollmentIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            enrollmentIds.add(enrollmentService.enroll(course.getId(), "user" + i));
        }

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 2. When
        for (Long id : enrollmentIds) {
            executorService.execute(() -> {
                try {
                    enrollmentService.confirmEnrollment(id);
                } catch (Exception e) {
                    System.out.println("신청 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 3. Then
        Course resultCourse = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(resultCourse.getCurrentCount()).isEqualTo(10);
    }
}
