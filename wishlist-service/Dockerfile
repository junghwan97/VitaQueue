FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/wishlist-service-0.0.1-SNAPSHOT.jar wishList-service.jar
ENTRYPOINT ["java", "-jar", "wishList-service.jar"]