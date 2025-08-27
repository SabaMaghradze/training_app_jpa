package com.hibernate.gymapp.repository;

import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeRepository {

    Trainee save(Trainee trainee);

    Optional<Trainee> findById(Long id);

    Optional<Trainee> findByUsername(String username);

    List<Trainee> findAll();

    void delete(Trainee trainee);

    List<Training> findByTraineeUsernameWithCriteria(String traineeUsername,
                                                     LocalDate fromDate,
                                                     LocalDate toDate,
                                                     String trainerName,
                                                     String trainingTypeName);

}
