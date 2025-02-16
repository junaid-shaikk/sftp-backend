package com.filetransfer.sftp.security;

import com.filetransfer.sftp.model.User;
import com.filetransfer.sftp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            logger.debug("Loading user by username: {}", username);

            Optional<User> user = userRepository.findByUsername(username);

            if (user.isEmpty()) {
                logger.warn("User not found: {}", username);
                throw new UsernameNotFoundException("User not found: " + username);
            }

            logger.debug("User found: {}", username);
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.get().getUsername())
                    .password(user.get().getPassword()) // Password is already encoded in the DB
                    .roles("USER") // Default role
                    .build();
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while loading user by username: {}", username, e);
            throw new RuntimeException("Unexpected error while loading user by username: " + username, e);
        }
    }
}