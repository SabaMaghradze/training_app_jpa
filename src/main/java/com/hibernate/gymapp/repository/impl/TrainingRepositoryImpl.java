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

    @Override
    public List<Training> findByTraineeUsernameWithCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
        if (traineeUsername == null || traineeUsername.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT tr FROM Training tr ")
                .append("JOIN tr.trainee t JOIN t.user tu ")
                .append("JOIN tr.trainer trn JOIN trn.user trun ")
                .append("JOIN tr.trainingType tt ")
                .append("WHERE tu.username = :traineeUsername");


        if (fromDate != null) jpql.append(" AND tr.trainingDate >= :fromDate");
        if (toDate != null)   jpql.append(" AND tr.trainingDate <= :toDate");
        if (trainerName != null && !trainerName.isEmpty()) {
            // case-insensitive partial match on firstName OR lastName OR "first last"
            jpql.append(" AND (LOWER(trun.firstName) LIKE :trainerName")
                    .append(" OR LOWER(trun.lastName) LIKE :trainerName")
                    .append(" OR LOWER(CONCAT(trun.firstName, ' ', trun.lastName)) LIKE :trainerName)");
        }
        if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
            jpql.append(" AND LOWER(tt.trainingTypeName) = :trainingTypeName");
        }

        TypedQuery<Training> query = entityManager.createQuery(jpql.toString(), Training.class);
        query.setParameter("traineeUsername", traineeUsername);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null)   query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isEmpty()) {
            query.setParameter("trainerName", "%" + trainerName.trim().toLowerCase() + "%");
        }
        if (trainingTypeName != null && !trainingTypeName.isEmpty()) {
            query.setParameter("trainingTypeName", trainingTypeName.trim().toLowerCase());
        }

        return query.getResultList();
    }

    @Override
    public List<Training> findByTrainerUsernameWithCriteria(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeName, String trainingTypeName) {
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
