package com.artvault.artvaultbackend.service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._]{3,30}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(
            String username,
            String email,
            String password) {

        // Check missing fields
        if (username == null ||
                email == null ||
                password == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All fields are required."
            );
        }

        username = username.trim();
        email = email.trim().toLowerCase();

        // Username validation
        if (!USERNAME_PATTERN.matcher(username).matches()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username must be 3-30 characters and contain only letters, numbers, dots or underscores."
            );
        }

        // Email validation
        if (!EMAIL_PATTERN.matcher(email).matches() ||
                email.length() > 254) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please enter a valid email address."
            );
        }

        // Password validation
        if (password.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password cannot be empty."
            );
        }

        if (password.length() < 8) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters."
            );
        }

        // BCrypt supports passwords up to 72 UTF-8 bytes
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is too long. Maximum supported length is 72 UTF-8 bytes."
            );
        }

        // Check duplicate username
        if (userRepository.existsByUsernameIgnoreCase(username)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists."
            );
        }

        // Check duplicate email
        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists."
            );
        }

        // Create new user
        User user = new User();

        user.setUsername(username);
        user.setEmail(email);

        // Hash password using BCrypt
        user.setPasswordHash(
                passwordEncoder.encode(password)
        );

        // Every new account starts as a normal USER
        user.setRole("USER");

        return userRepository.save(user);
    }
}