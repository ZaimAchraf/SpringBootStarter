# Étape 1 : Builder Maven complet
FROM eclipse-temurin:21-jdk AS builder

# Installer Maven
RUN apt-get update && apt-get install -y maven

WORKDIR /app

# Copier le code source complet
COPY . .

# Builder le jar du module principal app-api
RUN mvn -B -DskipTests clean install -Pproduction

# Étape 2 : Image runtime légère
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copier le jar buildé
COPY --from=builder /app/app-api/target/app-api-1.0-SNAPSHOT.jar app.jar

# Exposer le port (Render ou local)
EXPOSE 8180

# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
