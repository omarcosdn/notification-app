FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY notification-webhook/build.gradle ./notification-webhook/
RUN ./gradlew dependencies

COPY notification-webhook/src ./notification-webhook/src
RUN ./gradlew :notification-webhook:bootJar

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/notification-webhook/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"] 