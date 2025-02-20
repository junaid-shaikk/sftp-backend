# SFTP File Transfer Application

This project is a secure file transfer application built using Spring Boot and SFTP (Secure File Transfer Protocol). It allows users to upload, download, and manage files securely with authentication and authorization. The application supports chunked file uploads for large files and provides a REST API for seamless integration with other systems.

---

## 🚧 Project Status: Under Development 🚧

**Note**: This project is still under active development and is not yet deployed. New features, improvements, and bug fixes are being added regularly. The deployment process will be documented here once the project is ready for production use. Stay tuned for updates!

---

## Features

1. **User Authentication**:
    - JWT-based authentication for secure access.
    - User registration and login endpoints.
    - Password encryption using BCrypt.

2. **File Management**:
    - Upload files (supports single and chunked uploads).
    - Download files securely.
    - List files with pagination.
    - Delete files.

3. **Chunked Uploads**:
    - Upload large files in smaller chunks.
    - Merge chunks into a single file after upload.
    - Check uploaded chunks for resumable uploads.

4. **SFTP Server**:
    - Embedded SFTP server for secure file transfers.
    - Password-based authentication for SFTP access.

5. **Database Integration**:
    - Stores file metadata (e.g., file name, size, owner, upload time) in a database.
    - Stores user credentials securely.

6. **Error Handling**:
    - Comprehensive error handling and logging for debugging.
    - Graceful degradation on failures.

7. **Security**:
    - Role-based access control (RBAC).
    - Secure file storage and retrieval.
    - JWT token validation for API access.

---

## Technologies Used

- **Backend**: Spring Boot, Spring Security, JWT, SFTP (Apache MINA SSHD)
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Logging**: SLF4J with Logback

---

## Owner

This project is maintained by **Junaid Shaik**.  
GitHub: [https://github.com/junaid-shaikk](https://github.com/junaid-shaikk)

---
### Thank you for checking out my project! 🚀