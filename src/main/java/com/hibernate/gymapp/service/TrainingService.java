package com.hibernate.gymapp.service;

import com.hibernate.gymapp.model.Trainee;
import com.hibernate.gymapp.model.Trainer;
import com.hibernate.gymapp.model.Training;
import com.hibernate.gymapp.model.TrainingType;
import com.hibernate.gymapp.repository.TraineeRepository;
import com.hibernate.gymapp.repository.TrainerRepository;
import com.hibernate.gymapp.repository.TrainingRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private final TrainingRepository trainingRepository;
    private final AuthenticationService authenticationService;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public TrainingService(TrainingRepository trainingRepository,
                           AuthenticationService authenticationService,
                           TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository) {

        this.trainingRepository = trainingRepository;
        this.authenticationService = authenticationService;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public Training addTraining(
            String traineeUsername,
            String password,
            String trainerUsername,
            String trainingTypeName,
            LocalDate trainingDate
    ) {
        logger.info("Attempting to add training for trainee [{}] with trainer [{}] and type [{}] on [{}]",
                traineeUsername, trainerUsername, trainingTypeName, trainingDate);

        try {
            if (!authenticationService.authenticateTrainee(traineeUsername, password)) {
                logger.warn("Authentication failed for trainee: {}", traineeUsername);
                throw new SecurityException("Authentication failed for trainee: " + traineeUsername);
            }

            // Ensure trainee exists
            Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));

            // Ensure trainer exists
            Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainerUsername));


            TrainingType trainingType = trainerRepository
                    .findByUsername(trainerUsername).get()
                    .getSpecialization();

            if (trainingType.getTrainingTypeName() != trainingTypeName || trainingType == null) {
                throw new IllegalArgumentException("This trainer does not offer that service");
            }


            // Validate date
            if (trainingDate == null || trainingDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Training date must be in the future");
            }

            Training training = new Training();
            training.setTrainee(trainee);
            training.setTrainer(trainer);
            training.setTrainingType(trainingType);
            training.setTrainingDate(trainingDate);

            Training savedTraining = trainingRepository.save(training);

            logger.info("Training successfully created with ID [{}] for trainee [{}]",
                    savedTraining.getId(), traineeUsername);

            return savedTraining;

        } catch (Exception e) {
            logger.error("Failed to add training for trainee [{}]", traineeUsername, e);
            throw new RuntimeException("Error while adding training", e);
        }
    }

}
