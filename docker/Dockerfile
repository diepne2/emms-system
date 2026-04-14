FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY src/backend/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]