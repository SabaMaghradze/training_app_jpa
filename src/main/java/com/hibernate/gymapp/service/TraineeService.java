package com.hibernate.gymapp.service;

import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.User;
import com.hibernate.gymapp.repository.TraineeRepository;
import com.hibernate.gymapp.repository.UserRepository;
import com.hibernate.gymapp.utils.CredentialsGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Optional;

@Transactional
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeRepository traineeRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final CredentialsGenerator credentialsGenerator;

    public TraineeService(TraineeRepository traineeRepository, UserRepository userRepository,
                          AuthenticationService authenticationService, CredentialsGenerator credentialsGenerator) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.credentialsGenerator = credentialsGenerator;
    }

    @Transactional
    public Optional<Trainee> createTraineeProfile(String firstName, String lastName,
                                                  LocalDate dateOfBirth, String address) {
        logger.info("Creating trainee profile for: {} {}", firstName, lastName);

        try {
            if (firstName == null || firstName.trim().isEmpty() ||
                    lastName == null || lastName.trim().isEmpty()) {
                logger.warn("Validation failed: First name and last name are required");
                return Optional.empty();
            }

            String username = credentialsGenerator.generateUsername(firstName, lastName, userRepository);
            String password = credentialsGenerator.generatePassword();

            User user = new User();
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setUsername(username);
            user.setPassword(password);
            user.setIsActive(true);

            User savedUser = userRepository.save(user);

            Trainee trainee = new Trainee();
            trainee.setDateOfBirth(dateOfBirth);
            trainee.setAddress(address);
            trainee.setUser(savedUser);

            Trainee savedTrainee = traineeRepository.save(trainee);

            logger.info("Successfully created trainee profile for username: {}", username);
            return Optional.of(savedTrainee);
        } catch (Exception e) {
            logger.error("Error creating trainee profile for {} {}", firstName, lastName, e);
            throw new RuntimeException("Failed to create trainee profile", e);
        }
    }

    public Optional<Trainee> getTraineeProfileByUsername(String username, String password) {
        logger.debug("Getting trainee profile for username: {}", username);

        if (!authenticationService.authenticateTrainee(username, password)) {
            logger.warn("Authentication failed for trainee username: {}", username);
            return Optional.empty();
        }

        return traineeRepository.findByUsername(username);
    }

    @Transactional
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

            userRepository.save(user);
            Trainee updatedTrainee = traineeRepository.save(trainee);

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
