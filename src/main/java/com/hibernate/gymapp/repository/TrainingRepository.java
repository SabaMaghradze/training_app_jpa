package com.hibernate.gymapp.repository;

import com.hibernate.gymapp.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingRepository {

    Training save(Training training);

    Optional<Training> findById(Long id);

    List<Training> findAll();

    void delete(Training training);

}
