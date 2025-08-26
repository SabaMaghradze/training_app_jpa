package com.hibernate.gymapp.service;

import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.User;
import com.hibernate.gymapp.repository.TraineeRepository;
import com.hibernate.gymapp.repository.TrainerRepository;
import com.hibernate.gymapp.repository.TrainingRepository;
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
    private final TrainingRepository trainingRepository;
    private final TrainerRepository trainerRepository;

    public TraineeService(TraineeRepository traineeRepository, UserRepository userRepository,
                         AuthenticationService authenticationService, CredentialsGenerator credentialsGenerator,
                         TrainingRepository trainingRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.credentialsGenerator = credentialsGenerator;
        this.trainingRepository = trainingRepository;
        this.trainerRepository = trainerRepository;
    }

    public Optional<Trainee> createTraineeProfile(String firstName, String lastName,
                                                  LocalDate dateOfBirth, String address) {
        logger.info("Creating trainee profile for: {} {}", firstName, lastName);

        try {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setIsActive(true);

            user.setUsername(credentialsGenerator.generateUsername(firstName, lastName, userRepository));
            user.setPassword(credentialsGenerator.generatePassword());

            Optional<User> savedUser = userRepository.save(user);
            if (!savedUser.isPresent()) {
                return Optional.empty();
            }

            Trainee trainee = new Trainee();
            trainee.setDateOfBirth(dateOfBirth);
            trainee.setAddress(address);
            trainee.setUser(savedUser.get());

            return traineeRepository.save(trainee);

        } catch (Exception e) {
            logger.error("Error creating trainee profile", e);
            return Optional.empty();
        }
    }

    public Optional<Trainee> getTraineeProfileByUsername(String username, String password) {
        if (!authenticationService.authenticateTrainee(username, password)) {
            return Optional.empty();
        }

        return traineeRepository.findByUsername(username);
    }

    public boolean changeTraineePassword(String username, String oldPassword, String newPassword) {
        Optional<Trainee> traineeOpt = getTraineeProfileByUsername(username, oldPassword);
        if (!traineeOpt.isPresent()) {
            return false;
        }

        Trainee trainee = traineeOpt.get();
        trainee.getUser().setPassword(newPassword);

        return userRepository.save(trainee.getUser()).isPresent();
    }

    public Optional<Trainee> updateTraineeProfile(String username, String password,
                                                  String newFirstName, String newLastName,
                                                  LocalDate newDateOfBirth, String newAddress,
                                                  Boolean isActive) {
        Optional<Trainee> traineeOpt = getTraineeProfileByUsername(username, password);
        if (!traineeOpt.isPresent()) {
            return Optional.empty();
        }

        Trainee trainee = traineeOpt.get();
        User user = trainee.getUser();

        if (newFirstName != null) user.setFirstName(newFirstName);
        if (newLastName != null) user.setLastName(newLastName);
        if (isActive != null) user.setIsActive(isActive);
        if (newDateOfBirth != null) trainee.setDateOfBirth(newDateOfBirth);
        if (newAddress != null) trainee.setAddress(newAddress);

        Optional<User> updatedUser = userRepository.save(user);
        if (!updatedUser.isPresent()) {
            return Optional.empty();
        }

        return traineeRepository.save(trainee);
    }

    public boolean activateDeactivateTrainee(String username, String password, boolean activate) {
        Optional<Trainee> traineeOpt = getTraineeProfileByUsername(username, password);
        if (!traineeOpt.isPresent()) {
            return false;
        }

        Trainee trainee = traineeOpt.get();
        trainee.getUser().setIsActive(activate);

        return userRepository.save(trainee.getUser()).isPresent();
    }

    public boolean deleteTraineeProfile(String username, String password) {
        Optional<Trainee> traineeOpt = getTraineeProfileByUsername(username, password);
        if (!traineeOpt.isPresent()) {
            return false;
        }

        Trainee trainee = traineeOpt.get();
        traineeRepository.delete(trainee);
        userRepository.delete(trainee.getUser());

        return true;
    }
}
