# Backend 
cd C:\Users\ADMIN\emms-system\src\backend

.\mvnw spring-boot:run

.\mvnw clean spring-boot:run
.\mvnw clean install

cd C:\Users\ADMIN\emms-system\src\backend
./mvnw clean package
./mvnw clean compile

mvn clean install

cd C:\Users\ADMIN\emms-system\docker
docker-compose down
docker-compose up --build


http://localhost:8080/



Chạy SQL 
docker ps
cd C:\Users\ADMIN\emms-system\docker

docker exec -it emms-postgres psql -U postgres -d emms_system


USE emms_system;  




## Tìm lỗi 


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






SELECT u.id, u.username
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.code = 'TECHNICIAN';



git add .
git commit -m "fix password reset"
git push