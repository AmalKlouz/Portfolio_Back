# Étape 1 : Build avec Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY src ./src

# Build l'application (skip tests pour plus rapide)
RUN mvn clean package -DskipTests

# Étape 2 : Runtime avec Java
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier le JAR généré
COPY --from=build /app/target/*.jar app.jar

# Exposer le port 9000
EXPOSE 9000

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]