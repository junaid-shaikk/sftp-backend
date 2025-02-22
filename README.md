# 🌐 SFTP File Transfer Application

A secure file transfer application built with **Spring Boot** and **SFTP (Secure File Transfer Protocol)**.\
It allows **authenticated users** and **guest users** to **upload, download, and manage files** securely.

🔹 **Supports Chunked File Uploads for Large Files**\
🔹 **Built-in JWT Authentication for Security**\
🔹 **Role-Based Access Control (RBAC)**\
🔹 **Database-Backed File Storage (PostgreSQL on NeonDB)**

🚀 **Live Backend:** 👉 [https://sftp-deployment-sftp.onrender.com](https://sftp-deployment-sftp.onrender.com)

---

## 📌 Project Status

🚧 **Under Active Development** 🚧\
🔹 **Current Stage:** Backend is fully functional & deployed.\
🔹 **Next Step:** Frontend development (Angular).

---

## 🔥 Features

### ✅ 1. User Authentication (JWT)

- Secure **User Signup & Login**
- **Guest Mode** (Use the service without an account)
- Password encryption using **BCrypt**

### 📂 2. File Management

- **Upload & Download** files securely
- **List files with pagination**
- **Delete files (only for registered users)**

### 🚀 3. Chunked Uploads (For Large Files)

- Upload large files **in chunks**
- Merge chunks after upload
- Supports **resumable uploads**

### 🔐 4. Security Features

- **Role-Based Access Control (RBAC)**
- **JWT token validation** for API access
- **File ownership verification before deletion**

### 🗄 5. Database Integration

- Stores **file metadata** (file name, size, owner, upload time)
- Uses **PostgreSQL (NeonDB)** for user & file storage

---

## ⚙️ Technologies Used

| Technology                | Purpose               |
| ------------------------- | --------------------- |
| **Spring Boot**           | Backend API           |
| **Spring Security + JWT** | User Authentication   |
| **Apache MINA SSHD**      | Embedded SFTP Server  |
| **PostgreSQL (NeonDB)**   | File Metadata Storage |
| **Maven**                 | Build Tool            |
| **Logback + SLF4J**       | Logging               |

---

## 📌 Usage Guide (API Endpoints & `curl` Commands. Try these directly from postman)

👉 **Base URL:** `https://sftp-deployment-sftp.onrender.com`

### 1️⃣ User Signup (`/api/auth/register`)

```sh
curl -X POST https://sftp-deployment-sftp.onrender.com/api/auth/register \
     --header "Content-Type: application/json" \
     --data '{"username": "testuser", "password": "password123"}'
```

✅ **Response:** `"User registered successfully!"`

---

### 2️⃣ User Login (`/api/auth/login`)

```sh
curl -X POST https://sftp-deployment-sftp.onrender.com/api/auth/login \
     --header "Content-Type: application/json" \
     --data '{"username": "testuser", "password": "password123"}'
```

✅ **Response (JWT Token):**

```json
{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

🚨 **Save this token** – you'll need it for file uploads & downloads.

---

### 3️⃣ Use as Guest (No Signup/Login)

```sh
curl -X POST https://sftp-deployment-sftp.onrender.com/api/auth/guest
```

✅ **Response (Guest JWT Token):**

```json
{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
```

**Now, use this guest token to upload & download files.**

---

### 4️⃣ Upload a File (`/api/files/upload`)

```sh
curl -X POST https://sftp-deployment-sftp.onrender.com/api/files/upload \
     --header "Authorization: Bearer YOUR_JWT_TOKEN" \
     -F "file=@/path/to/video.mp4"
```

✅ **Response:** `"File uploaded successfully!"`

---

### 5️⃣ Download a File (`/api/files/download/{fileName}`)

```sh
curl -X GET https://sftp-deployment-sftp.onrender.com/api/files/download/video.mp4 \
     --header "Authorization: Bearer YOUR_JWT_TOKEN" \
     -o downloaded_video.mp4
```

✅ **File will be saved as **``**.**

📝 **🔹 Note for Postman Users:**\
Postman **doesn't support **``** like curl**. After sending the request:

1. Click **"Save Response" > "Save to File"**
2. **Manually choose the correct file format** before saving.

---

### 6️⃣ List Files with Pagination (`/api/files/list`)

```sh
curl -X GET "https://sftp-deployment-sftp.onrender.com/api/files/list?page=0&size=5" \
     --header "Authorization: Bearer YOUR_JWT_TOKEN"
```

✅ **Response:**

```json
[
  { "fileName": "video.mp4", "fileSize": 10240, "uploadedAt": "2025-02-23T12:30:00" },
  { "fileName": "resume.pdf", "fileSize": 2048, "uploadedAt": "2025-02-23T12:35:00" }
]
```

---

## 👤 Project Owner

🔹 **Maintained by:** [Junaid Shaik](https://github.com/junaid-shaikk)\
🔹 **GitHub Repo:** [sftp-backend](https://github.com/junaid-shaikk/sftp-backend)

🚀 **Thank you for checking out my project!** 🎉\