package com.liveklass.assignment.repository;

import com.liveklass.assignment.data.CourseStatus;
import com.liveklass.assignment.data.entity.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 1. 기존: 상태별 목록 조회
    List<Course> findAllByClassStatus(CourseStatus status);

    // 2. 추가: 비관적 락을 적용한 단건 조회 (동시성 방어용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findByIdWithLock(@Param("id") Long id);
}