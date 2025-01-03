FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/order-service-0.0.1-SNAPSHOT.jar order-service.jar
ENTRYPOINT ["java", "-jar", "order-service.jar"]