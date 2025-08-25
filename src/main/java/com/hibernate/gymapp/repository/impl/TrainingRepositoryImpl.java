package com.hibernate.gymapp.repository.impl;

import com.hibernate.gymapp.model.Training;
import com.hibernate.gymapp.repository.TrainingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public class TrainingRepositoryImpl implements TrainingRepository {

    private final EntityManager entityManager;

    public TrainingRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Optional<Training> save(Training training) {
        try {
            if (training.getId() == null) {
                entityManager.persist(training);
            } else {
                training = entityManager.merge(training);
            }
            return Optional.of(training);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
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
    @Transactional
    public void delete(Training training) {
        if (!entityManager.contains(training)) {
            training = entityManager.merge(training);
        }
        entityManager.remove(training);
    }
}
