FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY src/backend/target/*.jar app.jar

ENV TZ=Asia/Ho_Chi_Minh

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]