FROM openjdk:8u111-jdk-alpine
VOLUME /tmp
ADD target/worker.jar app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=production", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]