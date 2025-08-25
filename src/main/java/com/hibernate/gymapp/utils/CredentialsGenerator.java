package com.hibernate.gymapp.utils;

import com.hibernate.gymapp.repository.UserRepository;

import java.security.SecureRandom;

public class CredentialsGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }

        return password.toString();
    }

    public String generateUsername(String firstName, String lastName, UserRepository userRepository) {

        String baseUsername = (firstName + "." + lastName).toLowerCase();
        String username = baseUsername;
        int c = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + c;
            c++;
        }

        return username;
    }
}
