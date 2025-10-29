FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy project descriptor files to leverage layer caching for dependencies
COPY pom.xml pom.xml
COPY ewm-stats-service/pom.xml ewm-stats-service/pom.xml
COPY ewm-stats-service/ewm-stats-service-dto/pom.xml ewm-stats-service/ewm-stats-service-dto/pom.xml
COPY ewm-stats-service/ewm-stats-service-client/pom.xml ewm-stats-service/ewm-stats-service-client/pom.xml
COPY ewm-stats-service/ewm-stats-service-server/pom.xml ewm-stats-service/ewm-stats-service-server/pom.xml
COPY ewm-main-service/pom.xml ewm-main-service/pom.xml

RUN mvn -pl ewm-stats-service/ewm-stats-service-server -am dependency:go-offline

COPY . .
RUN mvn -pl ewm-stats-service/ewm-stats-service-server -am package -DskipTests

FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /opt/app

COPY --from=build /workspace/ewm-stats-service/ewm-stats-service-server/target/ewm-stats-service-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java","-jar","app.jar"]
