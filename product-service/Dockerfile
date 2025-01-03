FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/product-service-0.0.1-SNAPSHOT.jar product-service.jar
ENTRYPOINT ["java", "-jar", "product-service.jar"]