FROM gradle:6.8-jdk11 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar

FROM openjdk:11.0.10-jre-slim
COPY --from=builder /home/gradle/src/build/libs/SeMoDe-0.4.jar /usr/jar/semode.jar
WORKDIR /usr/jar
EXPOSE 9999
CMD ["java", "-jar","-Dspring.main.web-application-type=NONE", "semode.jar"]