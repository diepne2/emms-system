# Emms-management-system
## 1. Giới thiệu hệ thống
Hệ thống quản lý thiết bị và bảo trì doanh nghiệp (Enterprise Maintenance Management System - EMMS) là ứng dụng web hỗ trợ quản lý thiết bị, kế hoạch bảo trì, Work Order, yêu cầu sửa chữa, kho vật tư và phân tích dữ liệu vận hành trong doanh nghiệp. Hệ thống được xây dựng theo mô hình kiến trúc 3 tầng gồm Frontend, Backend và Database.
### Chức năng chính
Quản lý thiết bị
Quản lý Work Order
Quản lý kế hoạch bảo trì
Quản lý yêu cầu sửa chữa
Quản lý kho vật tư
Quản lý người dùng & phân quyền RBAC
Dashboard & báo cáo thống kê
AI hỗ trợ phân tích bảo trì và cảnh báo rủi ro

## 2. Technology Stack

### Frontend
- ReactJS
- Vite
- Ant Design
- CoreUI
- Axios
- React Router DOM

### Backend
- Java 17
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA / Hibernate
- Maven

### Database
- PostgreSQL

### Deployment & DevOps
- Docker
- Railway

### Version Control
- GitHub

## 3. Cách chạy hệ thống

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

#### Frontend 
Bước 1: Di chuyển tới thư mục Frontend
cd C:\Users\ADMIN\emms-system\src\frontend
Bước 2: Cài dependencies
- Cài đặt các thư viện cần thiết cho dự án:
npm install
Bước 3: Chạy Frontend
- Khởi động ứng dụng ReactJS bằng Vite:
npm run dev

*** Truy cập vào : http://localhost:5173/

=> Sau khi hệ thống khởi động thành công, người dùng có thể truy cập địa chỉ trên bằng trình duyệt để sử dụng hệ thống.


