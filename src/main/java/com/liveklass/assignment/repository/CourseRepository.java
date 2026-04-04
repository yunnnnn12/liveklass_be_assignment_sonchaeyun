package com.liveklass.assignment.repository;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByClassStatus(CourseStatus status); // CourseStatus 기준으로 강의목록 찾기
}
