# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Cache deps
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -DskipTests dependency:go-offline

# Build
COPY src/ src/
RUN ./mvnw -q -DskipTests clean package \
  && cp target/*.jar target/app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]