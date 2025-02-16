package com.filetransfer.sftp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role = "USER";  // Default role

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

