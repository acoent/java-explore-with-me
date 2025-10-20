FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy Maven descriptors first to cache dependencies
COPY pom.xml pom.xml
COPY ewm-main-service/pom.xml ewm-main-service/pom.xml

RUN mvn -f ewm-main-service/pom.xml dependency:go-offline

COPY . .
RUN mvn -f ewm-main-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /opt/app

COPY --from=build /workspace/ewm-main-service/target/ewm-main-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
