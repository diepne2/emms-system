# Backend 
cd C:\Users\ADMIN\emms-system\src\backend

.\mvnw spring-boot:run
.\mvnw clean install

cd C:\Users\ADMIN\emms-system\src\backend
.\mvnw clean package


cd C:\Users\ADMIN\emms-system\docker

docker-compose down
docker-compose up --build


http://localhost:8080/



Chạy SQL 
docker ps
docker exec -it emms-mysql mysql -u root -p emms_system




USE emms_system;  




## Tìm lỗi 
cd C:\Users\ADMIN\emms-system\src\backend

1. Clean lại  toàn bộ 
.\mvnw clean install 

2. Chạy test 
.\mvnw test

3. Nếu vẫn lỗi → liệt kê toàn bộ report

Chạy: dir target\surefire-reports


 ./mvnw clean test -e


 ./mvnw clean install -DskipTests


 Chạy auto test 
 cd C:\Users\ADMIN\emms-system\src\backend
 .\mvnw.cmd -Dtest=LoginTest test

 Kết quả test

Sau khi chạy xong:

target\surefire-reports

👉 sẽ thấy:

Tests run: 2, Failures: 0