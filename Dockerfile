FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN ./mvnw clean install -DskipTests
CMD ["./mvnw", "spring-boot:run"]
