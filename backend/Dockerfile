FROM gradle:7.6-jdk17 as builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle build -x test

FROM openjdk:17-slim

RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
