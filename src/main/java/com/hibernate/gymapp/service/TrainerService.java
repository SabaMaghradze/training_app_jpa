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

@Transactional
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

    public Optional<Trainer> createTrainerProfile(String firstName, String lastName,
                                                  String specialization) {

        logger.info("Creating trainer profile for: {} {}", firstName, lastName);

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

            Trainer trainer = new Trainer();
            trainer.setSpecialization(specialization);
            trainer.setUser(savedUser.get());

            return trainerRepository.save(trainer);
        } catch (Exception e) {
            logger.error("Error persisting the trainer profile", e);
            return Optional.empty();
        }
    }

}
