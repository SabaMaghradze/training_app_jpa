package com.hibernate.gymapp.service;


import com.hibernate.gymapp.model.User;
import com.hibernate.gymapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticateTrainee(String username, String password) {
        logger.info("Authenticating trainee: {}", username);
        return authenticateUser(username, password);
    }

    public boolean authenticateTrainer(String username, String password) {
        logger.info("Authenticating trainee: {}", username);
        return authenticateUser(username, password);
    }

    private boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (!userOpt.isPresent()) { // put isEmpty instead of isPresent
            logger.warn("Authentication failed: User {} not found", username);
            return false;
        }

        User user = userOpt.get();

        if (!user.getIsActive()) {
            logger.warn("Authentication failed: User {} is deactivated", username);
            return false;
        }

        boolean passwordMatches = user.getPassword().equals(password);

        if (!passwordMatches) {
            logger.warn("Authentication failed: Wrong password for user {}", username);
        } else {
            logger.info("You have been successfully authenticated!");
        }

        return passwordMatches;
    }
}





