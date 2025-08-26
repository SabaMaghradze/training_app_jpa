package com.hibernate.gymapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "training_types")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;

    @OneToMany(mappedBy = "trainingType")
    private Set<Training> trainings = new HashSet<>();

    @OneToMany(mappedBy = "specialization")
    private Set<Trainer> trainers = new HashSet<>();
}
