FROM maven:3.9.11-eclipse-temurin-25 AS build

ARG APP_MODULE

WORKDIR /workspace

COPY . .

RUN test -n "$APP_MODULE" \
    && mvn -pl "${APP_MODULE}/app" -am -DskipTests package \
    && jar_file="$(find "${APP_MODULE}/app/target" -maxdepth 1 -type f -name "*.jar" ! -name "*sources.jar" | head -n 1)" \
    && test -n "$jar_file" \
    && cp "$jar_file" /workspace/app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --uid 10001 vendix

COPY --from=build /workspace/app.jar /app/app.jar

USER vendix

EXPOSE 8080 8081 8082

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
