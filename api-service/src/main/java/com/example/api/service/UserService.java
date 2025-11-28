package com.example.api.service;

import com.example.common.entity.User;
import com.example.common.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        // In a real app, hash the password here!
        return userRepository.save(User.builder()
                .username(username)
                .password(password)
                .build());
    }

    public boolean login(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        // In a real app, check hashed password
        return user.map(u -> u.getPassword().equals(password)).orElse(false);
    }
}
