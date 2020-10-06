FROM openjdk:11-jre-stretch

RUN apt-get update && apt-get upgrade -y && apt-get install --no-install-recommends -y
ADD build/libs/worker.jar app.jar
RUN sh -c 'touch /app.jar' && \
    mkdir config

ENV JAVA_OPTS=""

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1 -Duser.timezone=UTC -jar /app.jar
