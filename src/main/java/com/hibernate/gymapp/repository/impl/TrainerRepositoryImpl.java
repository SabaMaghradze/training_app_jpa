package com.hibernate.gymapp.repository.impl;

import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.repository.TrainerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public class TrainerRepositoryImpl implements TrainerRepository {

    private final EntityManager entityManager;

    public TrainerRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Trainer save(Trainer trainer) {
        if (trainer.getId() == null) {
            entityManager.persist(trainer);
            return trainer;
        }
        return entityManager.merge(trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    @Override
    public List<Trainer> findAll() {
        TypedQuery<Trainer> query = entityManager.createQuery("SELECT t FROM Trainer t", Trainer.class);
        return query.getResultList();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        TypedQuery<Trainer> query = entityManager.createQuery(
                "SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class
        );

        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }

    @Override
    public void delete(Trainer trainer) {
        if (!entityManager.contains(trainer)) {
            trainer = entityManager.merge(trainer);
        }
        entityManager.remove(trainer);
    }
}
