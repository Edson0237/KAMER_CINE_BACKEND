# ============================================================
# Dockerfile — KCT Manager API (Spring Boot 4.x / JDK 17)
# Déploiement pilote — KAMER CINÉ TALENTS MANAGER
# ============================================================

# --- Étape 1 : Build Maven ---
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copier d'abord le pom.xml pour mettre en cache les dépendances
COPY pom.xml .

# Télécharger les dépendances (couche cachée si pom.xml inchangé)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Build : compile, tests skipés (tests exécutés dans CI/CD)
RUN mvn clean package -DskipTests -B

# --- Étape 2 : Runtime léger ---
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN groupadd -r kctm && useradd -r -g kctm kctm

# Copier le JAR depuis l'étape de build
COPY --from=builder /build/target/kct-manager-api-*.jar /app/app.jar

# Répertoire pour les uploads temporaires
RUN mkdir -p /app/uploads && chown -R kctm:kctm /app

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

USER kctm

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
