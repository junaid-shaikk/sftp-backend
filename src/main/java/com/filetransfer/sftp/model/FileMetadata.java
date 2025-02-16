package com.filetransfer.sftp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private Long fileSize; // Store file size in KB

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] fileData;

    public FileMetadata() {}

    public FileMetadata(String fileName, String owner, Long fileSize, LocalDateTime uploadedAt, byte[] fileData) {
        this.fileName = fileName;
        this.owner = owner;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.fileData = fileData;
    }
}