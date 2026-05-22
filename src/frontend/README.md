# React + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) (or [oxc](https://oxc.rs) when used in [rolldown-vite](https://vite.dev/guide/rolldown)) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend using TypeScript with type-aware lint rules enabled. Check out the [TS template](https://github.com/vitejs/vite/tree/main/packages/create-vite/template-react-ts) for information on how to integrate TypeScript and [`typescript-eslint`](https://typescript-eslint.io) in your project.
=======

# Frontend

## 1. Các công nghệ sử dụng Frontend (FE)

- **ReactJS**: Thư viện JavaScript xây dựng giao diện người dùng.
- **Vite**: Công cụ build và phát triển frontend tốc độ cao.
- **JavaScript (ES6+)**: Ngôn ngữ lập trình chính của Frontend.
- **React Router DOM**: Điều hướng giữa các trang trong ứng dụng.
- **Axios**: Gửi HTTP request tới Backend API.
- **Ant Design**: Thư viện UI component cho giao diện quản trị.
- **CoreUI**: Template dashboard quản trị hệ thống.
- **HTML5 / CSS3**: Xây dựng giao diện và định dạng hiển thị.
- **RESTful API**: Kết nối và trao đổi dữ liệu với Backend.
- **Railway**: Nền tảng triển khai Frontend.
- **GitHub**: Quản lý source code và version control.
- **Docker**: Container hóa và triển khai hệ thống.

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

#### Link triển khai hệ thống:

https://emms.up.railway.app


### Chạy trên local 
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


