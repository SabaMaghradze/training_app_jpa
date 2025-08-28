package com.hibernate.gymapp.repository.impl;

import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.model.Training;
import com.hibernate.gymapp.repository.TrainerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Collections;
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

    @Override
    public List<Training> findTrainingsByTrainerUsernameWithCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName, String trainingTypeName) {
        if (trainerUsername == null || trainerUsername.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT tr FROM Training tr ")
                .append("JOIN tr.trainer trn JOIN trn.user trun ")
                .append("JOIN tr.trainee t JOIN t.user tu ")
                .append("JOIN tr.trainingType tt ")
                .append("WHERE trun.username = :trainerUsername");

        if (fromDate != null) jpql.append(" AND tr.trainingDate >= :fromDate");
        if (toDate != null)   jpql.append(" AND tr.trainingDate <= :toDate");
        if (traineeName != null && !traineeName.isEmpty()) {
            jpql.append(" AND (LOWER(tu.firstName) LIKE :traineeName")
                    .append(" OR LOWER(tu.lastName) LIKE :traineeName")
                    .append(" OR LOWER(CONCAT(tu.firstName, ' ', tu.lastName)) LIKE :traineeName)");
        }
        if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
            jpql.append(" AND LOWER(tt.trainingTypeName) = :trainingTypeName");
        }

        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class);
        query.setParameter("trainerUsername", trainerUsername);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null)   query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.isEmpty()) {
            query.setParameter("traineeName", "%" + traineeName.trim().toLowerCase() + "%");
        }
        if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
            query.setParameter("trainingTypeName", trainingTypeName.trim().toLowerCase());
        }

        return query.getResultList();
    }
}
