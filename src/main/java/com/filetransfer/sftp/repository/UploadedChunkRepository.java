package com.filetransfer.sftp.repository;

import com.filetransfer.sftp.model.UploadedChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface UploadedChunkRepository extends JpaRepository<UploadedChunk, Long> {
    boolean existsByFileNameAndChunkIndexAndUsername(String fileName, int chunkIndex, String username);
    List<UploadedChunk> findByFileNameAndUsername(String fileName, String username);
    void deleteByFileNameAndUsername(String fileName, String username);
}
