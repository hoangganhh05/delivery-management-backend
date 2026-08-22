# ===================================================
# Stage 1: Build JAR application using Maven & Java 17
# ===================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy file cấu hình maven pom.xml
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy mã nguồn và đóng gói file JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ===================================================
# Stage 2: Runtime image nhẹ nhất với JRE 17 Alpine
# ===================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo non-root user để tăng tính bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy file .jar từ Stage 1
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Expose port mặc định 8080
EXPOSE 8080

# Khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
