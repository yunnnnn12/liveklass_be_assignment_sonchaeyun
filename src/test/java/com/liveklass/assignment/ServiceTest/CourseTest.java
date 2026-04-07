package com.liveklass.assignment.ServiceTest;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.dto.CourseCreateRequest;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.repository.CourseRepository;
import com.liveklass.assignment.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CourseTest {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("강의 생성 성공")
    void createCourse() {
        CourseCreateRequest dto = new CourseCreateRequest(
                "강의", "설명", 100000, 10,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );

        Long id = courseService.createCourse(dto);

        Course course = courseRepository.findById(id).orElseThrow();
        assertThat(course.getClassStatus()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    @DisplayName("강의 오픈 성공")
    void openCourse() {
        Course course = courseRepository.save(Course.builder()
                .title("강의")
                .maxCapacity(10)
                .currentCount(0)
                .classStatus(CourseStatus.DRAFT)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .build());

        courseService.openCourse(course.getId());

        assertThat(courseRepository.findById(course.getId()).get().getClassStatus())
                .isEqualTo(CourseStatus.OPEN);
    }
}