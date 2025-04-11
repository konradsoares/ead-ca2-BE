FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN mvn clean install -DskipTests
RUN mvn spring-boot:run
