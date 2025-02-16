package com.filetransfer.sftp.config;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.Collections;

@Configuration
public class SftpServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SftpServerConfig.class);

    @Bean
    public SshServer sshServer() {
        logger.info("Starting SFTP server on port 2222...");

        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(2222);

        try {
            // Set up host key provider
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));
            logger.debug("Host key provider configured successfully.");
        } catch (Exception e) {
            logger.error("Failed to configure host key provider: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure host key provider", e);
        }

        // Set up password authenticator
        sshServer.setPasswordAuthenticator((username, password, session) -> {
            try {
                boolean authenticated = "user".equals(username) && "password".equals(password);
                if (!authenticated) {
                    logger.warn("Authentication failed for user: {}", username);
                }
                return authenticated;
            } catch (Exception e) {
                logger.error("Error during authentication for user {}: {}", username, e.getMessage(), e);
                throw new RuntimeException("Authentication error", e);
            }
        });

        // Set up SFTP subsystem
        try {
            sshServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
            logger.debug("SFTP subsystem factory configured successfully.");
        } catch (Exception e) {
            logger.error("Failed to configure SFTP subsystem factory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure SFTP subsystem factory", e);
        }

        // Start the SFTP server
        try {
            sshServer.start();
            logger.info("SFTP server started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start SFTP server: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start SFTP server", e);
        }

        return sshServer;
    }
}