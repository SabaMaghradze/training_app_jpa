package com.hibernate.gymapp.repository;

import com.hibernate.gymapp.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository {

    Optional<Trainer> save(Trainer trainer);

    Optional<Trainer> findById(Long id);

    List<Trainer> findAll();

    void delete(Trainer trainer);

    Optional<Trainer> findByUsername(String username);
}
