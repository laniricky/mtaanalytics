# Build Stage
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :backend:installDist --no-daemon

# Run Stage
FROM eclipse-temurin:17-jre
EXPOSE 8080
COPY --from=build /home/gradle/src/backend/build/install/backend /app
WORKDIR /app
CMD ["./bin/backend"]
