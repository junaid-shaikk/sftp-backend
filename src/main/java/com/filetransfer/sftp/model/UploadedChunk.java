package com.filetransfer.sftp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "uploaded_chunks")
public class UploadedChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private int chunkIndex;

    @Column(nullable = false)
    private String username;

    public UploadedChunk() {}

    public UploadedChunk(String fileName, int chunkIndex, String username) {
        this.fileName = fileName;
        this.chunkIndex = chunkIndex;
        this.username = username;
    }
}
