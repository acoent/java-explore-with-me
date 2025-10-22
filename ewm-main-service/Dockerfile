FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy Maven descriptors first to cache dependencies
COPY pom.xml pom.xml
COPY ewm-main-service/pom.xml ewm-main-service/pom.xml
COPY ewm-stats-service/pom.xml ewm-stats-service/pom.xml
COPY ewm-stats-service/ewm-stats-service-dto/pom.xml ewm-stats-service/ewm-stats-service-dto/pom.xml
COPY ewm-stats-service/ewm-stats-service-client/pom.xml ewm-stats-service/ewm-stats-service-client/pom.xml
COPY ewm-stats-service/ewm-stats-service-server/pom.xml ewm-stats-service/ewm-stats-service-server/pom.xml

RUN mvn dependency:go-offline -pl ewm-main-service -am

COPY . .
RUN mvn -f ewm-main-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /opt/app

COPY --from=build /workspace/ewm-main-service/target/ewm-main-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
