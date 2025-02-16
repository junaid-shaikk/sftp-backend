package com.filetransfer.sftp.controller;

import com.filetransfer.sftp.dto.FileMetadataDTO;
import com.filetransfer.sftp.model.FileMetadata;
import com.filetransfer.sftp.service.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
public class FileTransferController {

    private static final Logger logger = LoggerFactory.getLogger(FileTransferController.class);

    private final FileStorageService fileStorageService;

    public FileTransferController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Upload request received from user: {}", username);

        try {
            fileStorageService.saveFile(file, username);
            logger.info("File uploaded successfully by user: {}", username);
            return ResponseEntity.ok("File uploaded successfully!");
        } catch (IOException e) {
            logger.error("File upload failed for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file upload for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed due to an unexpected error");
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName, Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Download request received for file: {} from user: {}", fileName, username);

        try {
            Optional<FileMetadata> fileMetadata = fileStorageService.getFile(fileName, username);

            if (fileMetadata.isEmpty()) {
                logger.warn("File not found: {} for user: {}", fileName, username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            FileMetadata file = fileMetadata.get();
            logger.info("File downloaded successfully: {} by user: {}", fileName, username);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getFileData());
        } catch (Exception e) {
            logger.error("Error downloading file {} for user {}: {}", fileName, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileMetadataDTO>> listUserFiles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String username = authentication.getName();
        logger.debug("File list request received from user: {}", username);

        try {
            Pageable pageable = PageRequest.of(page, size);
            List<FileMetadataDTO> files = fileStorageService.listUserFiles(username, pageable);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            logger.error("Error listing files for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PostMapping("/upload-chunk")
    public ResponseEntity<String> uploadChunk(
            @RequestParam("file") MultipartFile chunk,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkIndex") int chunkIndex,
            Authentication authentication) {

        String username = authentication.getName();
        logger.debug("Chunk upload request received for file: {}, chunk: {}, user: {}", fileName, chunkIndex, username);

        try {
            fileStorageService.saveChunk(chunk, fileName, chunkIndex, username);
            logger.info("Chunk {} uploaded successfully for file: {} by user: {}", chunkIndex, fileName, username);
            return ResponseEntity.ok("Chunk " + chunkIndex + " uploaded successfully!");
        } catch (IOException e) {
            logger.error("Chunk upload failed for file: {}, chunk: {}, user: {}: {}", fileName, chunkIndex, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Chunk upload failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during chunk upload for file: {}, chunk: {}, user: {}: {}", fileName, chunkIndex, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Chunk upload failed due to unexpected error");
        }
    }

    @PostMapping("/merge-chunks")
    public ResponseEntity<String> mergeChunks(
            @RequestParam("fileName") String fileName,
            Authentication authentication) {

        String username = authentication.getName();
        logger.debug("Merge request received for file: {} from user: {}", fileName, username);

        try {
            boolean merged = fileStorageService.mergeChunks(fileName, username);

            if (merged) {
                logger.info("File merged successfully: {} by user: {}", fileName, username);
                return ResponseEntity.ok("File merged successfully!");
            } else {
                logger.warn("Unauthorized merge attempt for file: {} by user: {}", fileName, username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to merge this file.");
            }
        } catch (IOException e) {
            logger.error("Merge failed for file: {} by user: {}: {}", fileName, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Merging failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error merging file {} for user {}: {}", fileName, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Merging failed due to unexpected error");
        }
    }

    @GetMapping("/check-chunks/{fileName}")
    public ResponseEntity<Set<Integer>> checkUploadedChunks(
            @PathVariable String fileName,
            Authentication authentication) {

        String username = authentication.getName();
        logger.debug("Check chunks request received for file: {} from user: {}", fileName, username);

        try {
            Set<Integer> uploadedChunks = fileStorageService.getUploadedChunks(fileName, username);
            return ResponseEntity.ok(uploadedChunks);
        } catch (Exception e) {
            logger.error("Error checking chunks for file {} (user {}): {}", fileName, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Set.of());
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName, Authentication authentication) {
        String username = authentication.getName();
        logger.debug("Delete request received for file: {} from user: {}", fileName, username);

        try {
            boolean deleted = fileStorageService.deleteFile(fileName, username);

            if (deleted) {
                logger.info("File deleted successfully: {} by user: {}", fileName, username);
                return ResponseEntity.ok("File deleted successfully.");
            } else {
                logger.warn("Unauthorized delete attempt for file: {} by user: {}", fileName, username);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized or file not found.");
            }
        } catch (Exception e) {
            logger.error("Error deleting file {} for user {}: {}", fileName, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File deletion failed due to unexpected error");
        }
    }
}