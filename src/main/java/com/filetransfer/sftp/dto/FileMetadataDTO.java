package com.filetransfer.sftp.dto;

import java.time.LocalDateTime;

public class FileMetadataDTO {
    private String fileName;
    private Long fileSizeKB;
    private LocalDateTime uploadedAt;

    public FileMetadataDTO(String fileName, Long fileSizeKB, LocalDateTime uploadedAt) {
        this.fileName = fileName;
        this.fileSizeKB = fileSizeKB;
        this.uploadedAt = uploadedAt;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSizeKB() {
        return fileSizeKB;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
