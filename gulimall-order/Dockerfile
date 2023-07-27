FROM openjdk:11-jre-slim

ENV JAVA_OPTS="-Xms128m -Xmx256m"

EXPOSE 8080
VOLUME /tmp

ADD target/*.jar app.jar

ENTRYPOINT java -jar $JAVA_OPTS -Dspring.profiles.active=prod app.jar