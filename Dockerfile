# ---------- Etapa 1: compila o projeto com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Etapa 2: imagem final, so com o .jar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/redlink-api.jar app.jar
EXPOSE 8080
# DB_URL, DB_USER e DB_PASSWORD sao informadas como variaveis de ambiente no Render
ENTRYPOINT ["java", "-jar", "app.jar"]
