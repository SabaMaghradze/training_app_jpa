package com.hibernate.gymapp.repository.impl;

import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.repository.TraineeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public class TraineeRepositoryImpl implements TraineeRepository {

    private final EntityManager entityManager;

    public TraineeRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Optional<Trainee> save(Trainee trainee) {
        try {
            if (trainee.getId() == null) {
                entityManager.persist(trainee);
            } else {
                trainee = entityManager.merge(trainee);
            }
            return Optional.of(trainee);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        TypedQuery<Trainee> query = entityManager.createQuery(
                "SELECT tr FROM Trainee tr WHERE tr.user.userName = :username", Trainee.class
        );
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }

    @Override
    public List<Trainee> findAll() {
        TypedQuery<Trainee> query = entityManager.createQuery("SELECT tr FROM Trainee tr", Trainee.class);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void delete(Trainee trainee) {
        if (!entityManager.contains(trainee)) {
            trainee = entityManager.merge(trainee);
        }
        entityManager.remove(trainee);
    }
}
