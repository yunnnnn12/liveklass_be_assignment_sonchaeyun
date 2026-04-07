package com.liveklass.assignment.data.repository;

import com.liveklass.assignment.data.EnrollmentStatus;
import com.liveklass.assignment.data.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    // [수강생] 내 신청 내역 페이징 조회
    Page<Enrollment> findAllByClassmateName(String userName, Pageable pageable);

    // [크리에이터] 특정 강의의 확정된 수강생 목록 페이징 조회
    Page<Enrollment> findAllByCourseIdAndEnrollmentStatus(Long courseId, EnrollmentStatus status, Pageable pageable);
}