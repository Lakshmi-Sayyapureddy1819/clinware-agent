# Multi-stage Dockerfile for production

# 1) Builder - compile and create shaded jar
FROM maven:3.9.5-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# 2) Runtime
FROM eclipse-temurin:21-jre

# Install Node.js (for tavily-server.js)
RUN apt-get update && \
    apt-get install -y curl gnupg && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# Copy shaded jar produced by the builder
COPY --from=builder /app/target/*-shaded.jar /app/app.jar
# Copy tavily server
COPY tavily-server.js /app/

EXPOSE 7000
ENV PORT=7000

HEALTHCHECK --interval=30s --timeout=5s CMD curl -f http://localhost:$PORT/health || exit 1

CMD ["java", "-jar", "/app/app.jar"]