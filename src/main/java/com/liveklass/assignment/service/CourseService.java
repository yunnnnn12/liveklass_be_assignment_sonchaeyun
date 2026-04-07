package com.liveklass.assignment.service;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.entity.Course;
import com.liveklass.assignment.data.dto.CourseCreateRequest;
import com.liveklass.assignment.data.dto.CourseResponse;
import com.liveklass.assignment.data.repository.CourseRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    // 강의 개설
    @Transactional
    public Long createCourse(CourseCreateRequest dto){

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new IllegalArgumentException("종료일은 시작일 이후여야 합니다.");
        }

        Course course = Course.builder()
                .title(dto.title())
                .description(dto.description())
                .price(dto.price())
                .maxCapacity(dto.maxCapacity())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .classStatus(CourseStatus.DRAFT)
                .currentCount(0)
                .build();

        return courseRepository.save(course).getId();
    }

    // 강의 목록 조회(상태에 따른)
    public List<CourseResponse> getCourses(CourseStatus status) {
        List<Course> courses;

        if (status == null) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findAllByClassStatus(status); // 상태 필터
        }

        return courses.stream()
                .map(CourseResponse::from) // Entity를 DTO로 변환
                .toList();
    }

    // 강의 상세 조회
    public CourseResponse getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 강의를 찾을 수 없습니다. ID: " + courseId));

        return CourseResponse.from(course);
    }

    @Transactional
    public void openCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        course.open();
    }

    @Transactional
    public void closeCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        course.close();
    }
}
