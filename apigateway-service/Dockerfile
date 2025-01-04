FROM openjdk:17-ea-11-jdk-slim

# Install curl
RUN apt-get update && apt-get install -y curl && apt-get clean

VOLUME /tmp
COPY build/libs/apigateway-service-0.0.1-SNAPSHOT.jar apigateway-service.jar
ENTRYPOINT ["java", "-jar", "apigateway-service.jar"]