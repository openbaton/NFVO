FROM openjdk:8-jdk as builder
COPY . /project
WORKDIR /project
RUN ./gradlew clean build -x test

FROM openjdk:8-jre-alpine
COPY --from=builder /project/build/libs/*.jar /nfvo.jar
ENTRYPOINT ["java", "-jar", "/nfvo.jar"]
EXPOSE 8080
