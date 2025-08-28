package com.hibernate.gymapp.repository.impl;

import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.model.Training;
import com.hibernate.gymapp.repository.TraineeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TraineeRepositoryImpl implements TraineeRepository {

    private final EntityManager entityManager;

    public TraineeRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Trainee save(Trainee trainee) {
        if (trainee.getId() == null) {
            entityManager.persist(trainee);
            return trainee;
        }
        return entityManager.merge(trainee);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        TypedQuery<Trainee> query = entityManager.createQuery(
                "SELECT tr FROM Trainee tr WHERE tr.user.username = :username", Trainee.class
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
    public void delete(Trainee trainee) {
        if (!entityManager.contains(trainee)) {
            trainee = entityManager.merge(trainee);
        }
        entityManager.remove(trainee);
    }

    @Override
    public List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername) {
        if (traineeUsername == null || traineeUsername.isEmpty()) {
            TypedQuery<Trainer> all = entityManager.createQuery("SELECT t FROM Trainer t", Trainer.class);
            return all.getResultList();
        }

        String jpql = "SELECT t FROM Trainer t WHERE NOT EXISTS (" +
                " SELECT 1 FROM Trainee tr JOIN tr.trainers trn WHERE trn = t AND tr.user.username = :username" +
                ")";

        TypedQuery<Trainer> query = entityManager.createQuery(jpql, Trainer.class);
        query.setParameter("username", traineeUsername);
        return query.getResultList();
    }

    @Override
    public List<Training> findTrainingsByTraineeUsernameWithCriteria(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerName, String trainingTypeName) {
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
}
