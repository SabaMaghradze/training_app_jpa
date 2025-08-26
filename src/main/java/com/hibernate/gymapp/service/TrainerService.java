package com.hibernate.gymapp.service;

import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.model.User;
import com.hibernate.gymapp.repository.TrainerRepository;
import com.hibernate.gymapp.repository.UserRepository;
import com.hibernate.gymapp.utils.CredentialsGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final CredentialsGenerator credentialsGenerator;

    public TrainerService(TrainerRepository trainerRepository, UserRepository userRepository,
                          AuthenticationService authenticationService, CredentialsGenerator credentialsGenerator) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.credentialsGenerator = credentialsGenerator;
    }

    @Transactional
    public Optional<Trainer> createTrainerProfile(String firstName, String lastName,
                                                  String specialization) {

        logger.info("Creating trainer profile for: {} {}", firstName, lastName);

        try {
            if (firstName == null || firstName.trim().isEmpty() ||
                    lastName == null || lastName.trim().isEmpty() ||
                    specialization == null || specialization.trim().isEmpty()) {
                logger.warn("Validation failed: First name, last name, and specialization are required");
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

            User savedUser = userRepository.save(user)
                    .orElseThrow(() -> new RuntimeException("Failed to save user"));

            Trainer trainer = new Trainer();
            trainer.setSpecialization(specialization);
            trainer.setUser(savedUser);

            Trainer savedTrainer = trainerRepository.save(trainer)
                    .orElseThrow(() -> new RuntimeException("Failed to save trainer"));

            logger.info("Successfully created trainer profile for username: {}", username);
            return Optional.of(savedTrainer);
        } catch (Exception e) {
            logger.error("Error creating trainer profile for {} {}", firstName, lastName, e);
            throw new RuntimeException("Failed to create trainer profile", e);
        }
    }

    public Optional<Trainer> getTrainerProfileByUsername(String username, String password) {
        logger.debug("Getting trainer profile for username: {}", username);

        if (!authenticationService.authenticateTrainer(username, password)) {
            logger.warn("Authentication failed for trainer username: {}", username);
            return Optional.empty();
        }

        return trainerRepository.findByUsername(username);
    }

    @Transactional
    public boolean changeTrainerPassword(String username, String oldPassword, String newPassword) {
        logger.info("Changing password for trainer username: {}", username);

        try {
            Optional<Trainer> trainerOpt = trainerRepository.findByUsername(username);

            if (!trainerOpt.isPresent()) {
                logger.warn("Trainer not found for username: {}", username);
                return false;
            }

            Trainer trainer = trainerOpt.get();

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

            trainer.getUser().setPassword(newPassword.trim());
            userRepository.save(trainer.getUser());

            logger.info("Successfully changed password for trainer username: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Error changing password for trainer username: {}", username, e);
            throw new RuntimeException("Failed to change password", e);
        }
    }

    @Transactional
    public Optional<Trainer> updateTrainerProfile(String username, String password,
                                                  String newFirstName, String newLastName,
                                                  String newSpecialization, Boolean isActive)
    {
        logger.info("Updating trainer profile for username: {}", username);

        try {
            // Authenticate first
            if (!authenticationService.authenticateTrainer(username, password)) {
                logger.warn("Authentication failed for trainer username: {}", username);
                return Optional.empty();
            }

            Optional<Trainer> trainerOpt = trainerRepository.findByUsername(username);
            if (!trainerOpt.isPresent()) {
                logger.warn("Trainer not found for username: {}", username);
                return Optional.empty();
            }

            Trainer trainer = trainerOpt.get();
            User user = trainer.getUser();

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
            Trainer updatedTrainer = trainerRepository.save(trainer)
                    .orElseThrow(() -> new RuntimeException("Failed to update trainer"));

            logger.info("Successfully updated trainer profile for username: {}", username);
            return Optional.of(updatedTrainer);

        } catch (Exception e) {
            logger.error("Error updating trainer profile for username: {}", username, e);
            throw new RuntimeException("Failed to update trainer profile", e);
        }
    }

    @Transactional
    public boolean activateDeactivateTrainer(String username, String password, boolean active) {
        logger.info("{} trainer with username: {}", active ? "Activating" : "Deactivating", username);

        try {
            // Authenticate first
            if (!authenticationService.authenticateTrainer(username, password)) {
                logger.warn("Authentication failed for trainer username: {}", username);
                return false;
            }

            Optional<Trainer> trainerOpt = trainerRepository.findByUsername(username);
            if (!trainerOpt.isPresent()) {
                logger.warn("Trainer not found for username: {}", username);
                return false;
            }

            Trainer trainer = trainerOpt.get();

            // Check if already in desired state (non-idempotent check)
            if (trainer.getUser().getIsActive() == active) {
                logger.warn("Trainer is already {}", active ? "active" : "inactive");
                return false;
            }

            trainer.getUser().setIsActive(active);
            userRepository.save(trainer.getUser());

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
    public boolean deleteTrainerProfile(String username, String password) {
        logger.info("Deleting trainer profile for username: {}", username);

        try {
            // Authenticate first
            if (!authenticationService.authenticateTrainer(username, password)) {
                logger.warn("Authentication failed for trainer username: {}", username);
                return false;
            }

            Optional<Trainer> trainerOpt = trainerRepository.findByUsername(username);
            if (!trainerOpt.isPresent()) {
                logger.warn("Trainer not found for username: {}", username);
                return false;
            }

            Trainer trainer = trainerOpt.get();
            trainerRepository.delete(trainer);

            logger.info("Successfully deleted trainer profile for username: {}", username);
            return true;

        } catch (Exception e) {
            logger.error("Error deleting trainer profile for username: {}", username, e);
            throw new RuntimeException("Failed to delete trainer profile", e);
        }
    }
}
