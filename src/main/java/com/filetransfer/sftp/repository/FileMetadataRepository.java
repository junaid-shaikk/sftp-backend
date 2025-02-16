package com.filetransfer.sftp.repository;

import com.filetransfer.sftp.model.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByFileNameAndOwner(String fileName, String owner);
    void deleteByFileNameAndOwner(String fileName, String owner);
    Page<FileMetadata> findByOwner(String owner, Pageable pageable);
}
