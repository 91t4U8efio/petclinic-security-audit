FROM maven:3.9-eclipse-temurin-17 AS builder
ADD EmailService/pom.xml /emailservice-src/pom.xml
ADD EmailService/src /emailservice-src/src
ADD EmailService/mvnw /emailservice-src/mvnw
ADD ./contrast_security.yaml /emailservice-src/contrast_security.yaml

WORKDIR /emailservice-src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests=true -Djava.version=17

FROM eclipse-temurin:17-jre

RUN mkdir -p /etc/contrast/java/
COPY --from=builder /emailservice-src/target/EmailService-1.0.0-SNAPSHOT.jar /app.jar
COPY --from=builder /emailservice-src/contrast_security.yaml /etc/contrast/java/contrast_security.yaml

EXPOSE 8081

ENTRYPOINT ["bash", "-c", "java -jar /app.jar"]
