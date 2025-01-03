FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/discovery-service-0.0.1-SNAPSHOT.jar discovery-server.jar
ENTRYPOINT ["java", "-jar", "discovery-server.jar"]