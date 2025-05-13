FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY notification-socket/build.gradle ./notification-socket/
RUN ./gradlew dependencies

COPY notification-socket/src ./notification-socket/src
RUN ./gradlew :notification-socket:bootJar

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/notification-socket/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"] 