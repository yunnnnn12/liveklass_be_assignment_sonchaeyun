package com.liveklass.assignment.repository;

import com.liveklass.assignment.data.entity.Classmate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassmateRepository extends JpaRepository<Classmate, Long> {
}
