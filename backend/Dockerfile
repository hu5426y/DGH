FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -DskipTests package && cp target/*.jar target/app.jar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/target/app.jar /app/app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
