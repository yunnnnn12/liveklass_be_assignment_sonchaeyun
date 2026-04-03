package com.liveklass.assignment.data.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
public class Classmate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 한명의 classMate가 여러개의 수강신청을 가진다.
    @OneToMany(mappedBy = "classmate")
    private List<Enrollment> enrollments = new ArrayList<>();



}
