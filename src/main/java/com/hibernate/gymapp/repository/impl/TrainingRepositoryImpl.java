package com.hibernate.gymapp.repository.impl;

import com.hibernate.gymapp.model.Training;
import com.hibernate.gymapp.repository.TrainingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TrainingRepositoryImpl implements TrainingRepository {

    private final EntityManager entityManager;

    public TrainingRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Training save(Training training) {
        if (training.getId() == null) {
            entityManager.persist(training);
            return training;
        }
        return entityManager.merge(training);
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    @Override
    public List<Training> findAll() {
        TypedQuery<Training> query = entityManager.createQuery("SELECT t FROM Training t", Training.class);
        return query.getResultList();
    }

    @Override
    public void delete(Training training) {
        if (!entityManager.contains(training)) {
            training = entityManager.merge(training);
        }
        entityManager.remove(training);
    }

}
