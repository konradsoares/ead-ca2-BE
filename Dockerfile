FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN mvn clean install -DskipTests
CMD ["mvn", "spring-boot:run"]
