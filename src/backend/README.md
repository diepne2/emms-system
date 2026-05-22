# Backend 
## 1. Các công nghệ sử dụng Backend (BE)

- **Java 17**: Ngôn ngữ lập trình chính của Backend.
- **Spring Boot**: Framework xây dựng RESTful API và xử lý nghiệp vụ.
- **Spring Security**: Framework bảo mật và phân quyền người dùng.
- **JWT (JSON Web Token)**: Xác thực người dùng theo cơ chế stateless.
- **Spring Data JPA**: Hỗ trợ thao tác dữ liệu với Database.
- **Hibernate**: ORM mapping giữa Java Object và Database.
- **PostgreSQL**: Hệ quản trị cơ sở dữ liệu.
- **Maven**: Quản lý dependencies và build project.
- **Docker**: Container hóa và triển khai hệ thống.
- **Railway**: Nền tảng triển khai hệ thống.
- **RESTful API**: Kết nối giữa Frontend và Backend.
- **RBAC**: Mô hình phân quyền theo vai trò người dùng.

## 2. Cách chạy hệ thống

### Chạy trên Railway

Hệ thống được triển khai trên nền tảng Railway.

1. Kết nối repository GitHub với Railway
2. Tạo project mới trên Railway
3. Tạo và cấu hình các service:
   - Backend (Spring Boot)
   - Frontend (ReactJS + Vite)
   - PostgreSQL Database
4. Cấu hình biến môi trường cho Backend và Frontend
5. Railway tự động build và deploy hệ thống

Link triển khai hệ thống:

https://emms.up.railway.app


### Chạy trên local 

#### Backend
Bước 1: Di chuyển tới thư mục Backend

```bash
cd C:\Users\ADMIN\emms-system\src\backend

Bước 2: Build project Backend
- Thực hiện build source code bằng Maven Wrapper:
./mvnw clean package

Bước 3: Chạy Backend bằng Docker
cd C:\Users\ADMIN\emms\docker

- Dừng các container cũ (nếu có): docker-compose down
- Build và khởi động hệ thống:
docker-compose up --build

** Backend sẽ chạy tại: http://localhost:8080