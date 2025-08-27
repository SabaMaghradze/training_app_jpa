package com.hibernate.gymapp.service;

import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.model.User;
import com.hibernate.gymapp.repository.TraineeRepository;
import com.hibernate.gymapp.repository.TrainerRepository;
import com.hibernate.gymapp.repository.TrainingRepository;
import com.hibernate.gymapp.repository.UserRepository;
import com.hibernate.gymapp.utils.CredentialsGenerator;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private final EntityManagerFactory factory;
    private final CredentialsGenerator credentialsGenerator;

    public TraineeService(EntityManagerFactory factory, CredentialsGenerator credentialsGenerator) {
        this.factory = factory;
        this.credentialsGenerator = credentialsGenerator;
    }

    public Optional<Trainee> createTraineeProfile(String firstName, String lastName,
                                                  LocalDate dateOfBirth, String address) {
        logger.info("Creating trainee profile for: {} {}", firstName, lastName);

        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            logger.warn("Validation failed: First name and last name are required");
            return Optional.empty();
        }

        EntityManager entityManager = factory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

    }

    public Optional<Trainee> getTraineeProfileByUsername(String username, String password) {
        logger.debug("Getting trainee profile for username: {}", username);

        if (!authenticationService.authenticateTrainee(username, password)) {
            logger.warn("Authentication failed for trainee username: {}", username);
            return Optional.empty();
        }

        return traineeRepository.findByUsername(username);
    }

    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        logger.info("Changing password for trainer username: {}", username);

        try {
            Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);

            if (!traineeOpt.isPresent()) {
                logger.warn("Trainer not found for username: {}", username);
                return false;
            }

            Trainee trainee = traineeOpt.get();

            // Verify old password
            if (!authenticationService.authenticateTrainer(username, oldPassword)) {
                logger.warn("Old password verification failed for trainer username: {}", username);
                return false;
            }

            // Validate new password
            if (newPassword == null || newPassword.trim().isEmpty()) {
                logger.warn("New password cannot be empty");
                return false;
            }

            trainee.getUser().setPassword(newPassword.trim());
            userRepository.save(trainee.getUser());

            logger.info("Successfully changed password for trainer username: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Error changing password for trainer username: {}", username, e);
            throw new RuntimeException("Failed to change password", e);
        }
    }

    @Transactional
    public Optional<Trainee> updateTraineeProfile(String username, String password,
                                        String newFirstName, String newLastName,
                                        LocalDate newDateOfBirth, String newAddress,
                                        Boolean isActive) {
        logger.info("Updating trainer profile for username: {}", username);

        try {
            // Authenticate first
            if (!authenticationService.authenticateTrainer(username, password)) {
                logger.warn("Authentication failed for trainer username: {}", username);
                return Optional.empty();
            }

            Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);
            if (!traineeOpt.isPresent()) {
                logger.warn("Trainer not found for username: {}", username);
                return Optional.empty();
            }

            Trainee trainee = traineeOpt.get();
            User user = trainee.getUser();

            // Update fields if provided
            if (newFirstName != null && !newFirstName.trim().isEmpty()) {
                user.setFirstName(newFirstName.trim());
            }

            if (newLastName != null && !newLastName.trim().isEmpty()) {
                user.setLastName(newLastName.trim());
            }

            if (isActive != null) {
                user.setIsActive(isActive);
            }

            if (newSpecialization != null && !newSpecialization.trim().isEmpty()) {
                trainer.setSpecialization(newSpecialization);
            }

            userRepository.save(user);
            Trainer updatedTrainee = traineeRepository.save(trainee)
                    .orElseThrow(() -> new RuntimeException("Failed to update trainer"));

            logger.info("Successfully updated trainer profile for username: {}", username);
            return Optional.of(updatedTrainee);

        } catch (Exception e) {
            logger.error("Error updating trainer profile for username: {}", username, e);
            throw new RuntimeException("Failed to update trainer profile", e);
        }
    }

    @Transactional
    public boolean activateDeactivateTrainee(String username, String password, boolean active) {
        logger.info("{} trainer with username: {}", active ? "Activating" : "Deactivating", username);

        try {
            // Authenticate first
            if (!authenticationService.authenticateTrainer(username, password)) {
                logger.warn("Authentication failed for trainer username: {}", username);
                return false;
            }

            Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);
            if (!traineeOpt.isPresent()) {
                logger.warn("Trainer not found for username: {}", username);
                return false;
            }

            Trainee trainee = traineeOpt.get();

            // Check if already in desired state (non-idempotent check)
            if (trainee.getUser().getIsActive() == active) {
                logger.warn("Trainer is already {}", active ? "active" : "inactive");
                return false;
            }

            trainee.getUser().setIsActive(active);
            userRepository.save(trainee.getUser());

            logger.info("Successfully {} trainer with username: {}",
                    active ? "activated" : "deactivated", username);
            return true;

        } catch (Exception e) {
            logger.error("Error {} trainer with username: {}",
                    active ? "activating" : "deactivating", username, e);
            throw new RuntimeException("Failed to update trainer status", e);
        }
    }

    @Transactional
    public boolean deleteTraineeProfile(String username, String password) {
        logger.info("Deleting trainee profile for username: {}", username);

        try {
            if (!authenticationService.authenticateTrainee(username, password)) {
                logger.warn("Authentication failed for trainee username: {}", username);
                return false;
            }

            Optional<Trainee> traineeOpt = traineeRepository.findByUsername(username);
            if (!traineeOpt.isPresent()) {
                logger.warn("Trainee not found for username: {}", username);
                return false;
            }

            Trainee trainee = traineeOpt.get();

            traineeRepository.delete(trainee);

            logger.info("Successfully deleted trainee profile and associated trainings for username: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Error deleting trainee profile for username: {}", username, e);
            throw new RuntimeException("Failed to delete trainee profile", e);
        }
    }
}
