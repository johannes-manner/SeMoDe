FROM gradle:6.8-jdk11 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src/src/main/resources/static/js
RUN apt-get update
RUN apt-get -y install curl gnupg
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash
RUN apt-get -y install nodejs
RUN npm install
WORKDIR /home/gradle/src
RUN gradle bootJar

FROM openjdk:11.0.10-jre-slim
COPY --from=builder /home/gradle/src/build/libs/SeMoDe-*.jar /usr/jar/semode.jar
WORKDIR /usr/jar
EXPOSE 8080
CMD ["java", "-jar", "semode.jar"]