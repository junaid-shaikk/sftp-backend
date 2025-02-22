package com.filetransfer.sftp.controller;

import com.filetransfer.sftp.model.User;
import com.filetransfer.sftp.repository.UserRepository;
import com.filetransfer.sftp.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            logger.debug("Attempting to register user: {}", user.getUsername());

            Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser.isPresent()) {
                logger.warn("Registration failed: Username {} already exists", user.getUsername());
                return ResponseEntity.badRequest().body("Username already exists!");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);

            logger.info("User {} registered successfully", user.getUsername());
            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            logger.error("Error during registration for user {}: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Registration failed due to an internal error.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            logger.debug("Attempting to login user: {}", user.getUsername());

            Optional<User> dbUser = userRepository.findByUsername(user.getUsername());

            if (dbUser.isPresent() && passwordEncoder.matches(user.getPassword(), dbUser.get().getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername());
                logger.info("Login successful for user: {}", user.getUsername());
                return ResponseEntity.ok(token);
            } else {
                logger.warn("Login failed: Invalid credentials for user {}", user.getUsername());
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        } catch (Exception e) {
            logger.error("Error during login for user {}: {}", user.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Login failed due to an internal error.");
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<String> generateGuestToken() {
        String guestUsername = "guest-" + UUID.randomUUID().toString().substring(0, 8);

        // Generate a JWT token with an expiration time (e.g., 1 hour)
        String token = jwtUtil.generateGuestToken(guestUsername);
        logger.info("Guest {}, logged in", guestUsername);

        return ResponseEntity.ok(token);
    }
}