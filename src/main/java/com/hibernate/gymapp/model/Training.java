package com.hibernate.gymapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @NotBlank
    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @NotNull
    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @NotNull
    @Column(name = "training_duration", nullable = false)
    private Number trainingDuration;
}
