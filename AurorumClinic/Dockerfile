FROM maven:3.9.9-eclipse-temurin-24 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-jre
COPY --from=build /target/AurorumClinic-0.0.1-SNAPSHOT.jar AurorumClinic.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "AurorumClinic.jar"]