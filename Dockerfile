FROM maven:3.9.9-eclipse-temurin-17 as builder

WORKDIR /workspace

COPY pom.xml .
COPY .mvn .mvn
COPY src src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=builder /workspace/target/jobport-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]