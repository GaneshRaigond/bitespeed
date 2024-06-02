FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=my-app.jar
COPY build/libs/my-app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]