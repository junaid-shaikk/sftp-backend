package com.filetransfer.sftp.service;

import com.filetransfer.sftp.dto.FileMetadataDTO;
import com.filetransfer.sftp.model.FileMetadata;
import com.filetransfer.sftp.model.UploadedChunk;
import com.filetransfer.sftp.repository.FileMetadataRepository;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import com.filetransfer.sftp.repository.UploadedChunkRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    @Autowired
    UploadedChunkRepository uploadedChunkRepository;

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final String CHUNK_DIR = "chunks/";

    public Optional<FileMetadata> getFile(String fileName, String username) {
        try {
            return fileMetadataRepository.findByFileNameAndOwner(fileName, username);
        } catch (Exception e) {
            logger.error("Failed to fetch file {} for user {}: {}", fileName, username, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch file", e);
        }
    }

    public void saveFile(MultipartFile file, String username) throws IOException {
        try {

            byte[] fileData = file.getBytes();

            FileMetadata fileMetadata = new FileMetadata(
                    file.getOriginalFilename(),
                    username,
                    file.getSize() / 1024, // Convert bytes to KB
                    LocalDateTime.now(),
                    fileData // Store actual binary data
            );

            fileMetadataRepository.save(fileMetadata);

            logger.info("File saved successfully by {}: {} (Size: {} KB)",
                    username, file.getOriginalFilename(), file.getSize() / 1024);
        } catch (Exception e) {
            logger.error("Failed to save file {} for user {}: {}", file.getOriginalFilename(), username, e.getMessage(), e);
            throw new IOException("Failed to save file", e);
        }
    }

    public void saveChunk(MultipartFile chunk, String fileName, int chunkIndex, String username) throws IOException {
        try {

            Path chunkDirectory = Paths.get(CHUNK_DIR, username, fileName);

            if (!Files.exists(chunkDirectory)) {
                Files.createDirectories(chunkDirectory);
            }

            Path chunkPath = chunkDirectory.resolve(chunkIndex + ".part");

            if (Files.exists(chunkPath)) {
                logger.debug("Chunk {} already exists for file {} by user {}", chunkIndex, fileName, username);
                return;
            }

            Files.write(chunkPath, chunk.getBytes());

            uploadedChunkRepository.save(new UploadedChunk(fileName, chunkIndex, username));
            logger.info("Chunk {} saved for file {} by user {}", chunkIndex, fileName, username);
        } catch (Exception e) {
            logger.error("Failed to save chunk {} for file {} by user {}: {}", chunkIndex, fileName, username, e.getMessage(), e);
            throw new IOException("Failed to save chunk", e);
        }
    }

    public boolean isChunkUploaded(String fileName, int chunkIndex, String username) {
        try {
            return uploadedChunkRepository.existsByFileNameAndChunkIndexAndUsername(fileName, chunkIndex, username);
        } catch (Exception e) {
            logger.error("Failed to check if chunk {} exists for file {} by user {}: {}", chunkIndex, fileName, username, e.getMessage(), e);
            throw new RuntimeException("Failed to check chunk existence", e);
        }
    }

    @Transactional
    public boolean mergeChunks(String fileName, String username) throws IOException {
        Path chunkDirectory = Paths.get(CHUNK_DIR, username, fileName);

        Optional<FileMetadata> fileMetadata = fileMetadataRepository.findByFileNameAndOwner(fileName, username);
        if (fileMetadata.isEmpty()) {
            logger.warn("Unauthorized merge attempt for file {} by user {}", fileName, username);
            return false;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (Stream<Path> chunkFiles = Files.list(chunkDirectory).sorted(Comparator.comparingInt(this::getChunkIndex))) {
            for (Path chunkFile : (Iterable<Path>) chunkFiles::iterator) {
                Files.copy(chunkFile, byteArrayOutputStream);
            }
        } catch (IOException e) {
            logger.error("Error reading chunk files for merging: {}", e.getMessage(), e);
            throw new IOException("Failed to merge chunks", e);
        }

        byte[] fileData = byteArrayOutputStream.toByteArray();

        // Save merged file into PostgreSQL
        FileMetadata updatedFile = fileMetadata.get();
        updatedFile.setFileData(fileData);
        fileMetadataRepository.save(updatedFile);

        try {
            // Delete all chunks in one operation
            Files.walk(chunkDirectory)
                    .sorted(Comparator.reverseOrder()) // Reverse order to delete files before directories
                    .map(Path::toFile)
                    .forEach(File::delete);

            logger.info("All chunks deleted successfully for file {} by user {}", fileName, username);
        } catch (IOException e) {
            logger.warn("Failed to delete chunk directory for file {}: {}", fileName, e.getMessage());
        }

        uploadedChunkRepository.deleteByFileNameAndUsername(fileName, username); // Clean up DB entries
        logger.info("Chunks merged & stored in DB for file {} by user {}", fileName, username);
        return true;
    }


    private int getChunkIndex(Path path) {
        try {
            String fileName = path.getFileName().toString();
            return Integer.parseInt(fileName.replace(".part", ""));
        } catch (Exception e) {
            logger.error("Failed to extract chunk index from path {}: {}", path, e.getMessage(), e);
            throw new RuntimeException("Failed to extract chunk index", e);
        }
    }

    public Set<Integer> getUploadedChunks(String fileName, String username) {
        try {
            return uploadedChunkRepository.findByFileNameAndUsername(fileName, username)
                    .stream()
                    .map(UploadedChunk::getChunkIndex)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Failed to fetch uploaded chunks for file {} by user {}: {}", fileName, username, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch uploaded chunks", e);
        }
    }

    @Transactional
    public boolean deleteFile(String fileName, String username) {
        try {
            Optional<FileMetadata> fileMetadata = fileMetadataRepository.findByFileNameAndOwner(fileName, username);

            if (fileMetadata.isEmpty()) {
                logger.warn("File not found in database: {}", fileName);
                return false;
            }

            fileMetadataRepository.delete(fileMetadata.get());
            logger.info("File record deleted successfully: {} by {}", fileName, username);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete file {} for user {}: {}", fileName, username, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public List<FileMetadataDTO> listUserFiles(String username, Pageable pageable) {
        try {
            Page<FileMetadata> filePage = fileMetadataRepository.findByOwner(username, pageable);

            return filePage.getContent().stream()
                    .map(file -> new FileMetadataDTO(file.getFileName(), file.getFileSize(), file.getUploadedAt()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to list files for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to list files", e);
        }
    }
}