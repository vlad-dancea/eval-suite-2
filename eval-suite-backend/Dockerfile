# syntax=docker/dockerfile:1
# =========================================================================
# Build stage — compile and package the Spring Boot application
# =========================================================================
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Maven wrapper + descriptor first so dependency resolution is cached
# independently of source changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp dependency:go-offline

# Compile and package. Tests/lint are a CI concern, not a packaging one.
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -DskipTests clean package

# Normalize the artifact name, then split the fat jar into cache-friendly
# layers (Spring Boot "tools" jarmode).
RUN cp target/*.jar application.jar && \
    java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# =========================================================================
# Runtime stage — minimal JRE, non-root, layered for fast image rebuilds
# =========================================================================
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Run as an unprivileged user.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copy layers from most- to least-stable so dependency layers stay cached
# across application code changes.
COPY --chown=spring:spring --from=build /workspace/extracted/dependencies/ ./
COPY --chown=spring:spring --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --chown=spring:spring --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --chown=spring:spring --from=build /workspace/extracted/application/ ./

EXPOSE 8080

# Container-aware ergonomics + AppCDS: the shared archive is created on the
# first start and reused on every subsequent start for faster boot.
ENTRYPOINT ["java", \
    "-XX:+AutoCreateSharedArchive", \
    "-XX:SharedArchiveFile=/tmp/app.jsa", \
    "-jar", "application.jar"]
