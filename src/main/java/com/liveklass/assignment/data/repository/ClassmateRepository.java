package com.liveklass.assignment.data.repository;

import com.liveklass.assignment.data.entity.Classmate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassmateRepository extends JpaRepository<Classmate, Long> {
    Optional<Classmate> findByName(String name);
}
