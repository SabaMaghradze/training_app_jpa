package com.hibernate.gymapp.repository;

import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository {

    Trainer save(Trainer trainer);

    Optional<Trainer> findById(Long id);

    List<Trainer> findAll();

    void delete(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    List<Training> findTrainingsByTrainerUsernameWithCriteria(String trainerUsername,
                                                     LocalDate fromDate,
                                                     LocalDate toDate,
                                                     String traineeName,
                                                     String trainingTypeName);
}
