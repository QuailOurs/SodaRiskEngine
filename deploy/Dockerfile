FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY server ./server
RUN mvn -f server/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/server/web/target/soda-web-*.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
