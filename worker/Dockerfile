FROM openjdk:8u111-jdk-alpine
VOLUME /tmp
ADD build/libs/worker.jar app.jar
ADD libs/dd-java-agent.jar dd-java-agent.jar
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -javaagent:/dd-java-agent.jar -Djava.security.egd=file:/dev/./urandom -Duser.timezone=UTC -jar /app.jar" ]
