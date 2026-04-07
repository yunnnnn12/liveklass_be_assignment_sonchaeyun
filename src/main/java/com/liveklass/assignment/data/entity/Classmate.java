package com.liveklass.assignment.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Classmate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // 한명의 classMate가 여러개의 수강신청을 가진다.
    @OneToMany(mappedBy = "classmate")
    private List<Enrollment> enrollments = new ArrayList<>();



}
