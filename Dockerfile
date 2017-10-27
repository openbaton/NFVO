FROM openjdk:8-jdk as builder
COPY . /project
WORKDIR /project
RUN ./gradlew build -x test

FROM openjdk:8-jre-alpine
COPY --from=builder /project/build/libs/*.jar /nfvo.jar
RUN apk add -u --no-cache wget &&mkdir -p /usr/lib/openbaton/plugins/vim-drivers &&wget -qnH --cut-dirs 2 -r --no-parent  --reject "index.html*" "http://get.openbaton.org/plugins/stable/" -P "/usr/lib/openbaton/plugins/vim-drivers"&&mkdir -p /var/log/openbaton
COPY --from=builder /project/main/src/main/resources/application.properties /etc/openbaton/openbaton-nfvo.properties
ENV NFVO_PLUGIN_INSTALLATION-DIR /usr/lib/openbaton/plugins
ENTRYPOINT ["java", "-jar", "/nfvo.jar", "--spring.config.location=file:/etc/openbaton/openbaton-nfvo.properties"]
EXPOSE 8080
EXPOSE 8443
