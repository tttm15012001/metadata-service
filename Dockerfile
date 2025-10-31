# -------- Stage 1: Build --------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace
COPY . .

# Build ứng dụng (dùng maven wrapper hoặc mvn)
RUN ./mvnw clean package -DskipTests

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jre-alpine

# Tạo user non-root để tăng bảo mật
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy file jar từ builder stage
COPY --from=builder /workspace/target/metadata-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Env vars for DB connection
ENV DB_URL=""
ENV DB_USER=""
ENV DB_PASS=""

ENTRYPOINT ["java", "-jar", "app.jar"]
