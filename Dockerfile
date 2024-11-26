FROM maven:3.9-eclipse-temurin-21-alpine as buildNode

WORKDIR /project-build

COPY ./ /project-build

RUN mvn clean package

FROM eclipse-temurin:21.0.2_13-jre-alpine

COPY --from=buildNode /project-build/target/report-gen-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]