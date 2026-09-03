# KAMER CINÉ TALENTS MANAGER API

Plateforme nationale de pilotage du programme de formation aux métiers du cinéma — 360 communes du Cameroun.

## Stack

- **Spring Boot 4.x** + Java 17
- **Spring Data JPA** (PostgreSQL)
- **Spring Security** (JWT stateless)
- **Flyway** (migrations SQL)
- **springdoc-openapi** (Swagger UI sur `/swagger-ui.html`)
- **Maven**

## Structure — Package-by-feature

```
com.kamercinetalents.manager
├── KctManagerApiApplication.java
├── common/
│   ├── config/          # SecurityConfig, OpenApiConfig, TransactionConfig
│   └── security/        # JwtTokenProvider, JwtAuthenticationFilter
├── iam/                 # Module M1 — IAM
│   ├── controller/      # REST controllers
│   ├── service/         # Interface + Impl
│   ├── repository/      # Spring Data JPA
│   ├── domain/          # Entités JPA
│   └── dto/             # DTOs (jamais d'entité exposée)
├── territoire/          # Module M2 — Territoire (à créer)
├── formation/           # Module M3 — Formation (à créer)
├── sync/                # Module M5 — Synchronisation (à créer)
├── notification/        # Module M6 — Notifications (à créer)
└── admin/               # Module M0 — Administration (à créer)
```

## Règles ACID

**Chaque opération multi-tables = `@Transactional` avec limites explicites.**

- Les transactions sont déclarées au niveau des méthodes de **service**, jamais au niveau contrôleur.
- Niveau d'isolation par défaut : `READ_COMMITTED`.
- Les opérations de lecture utilisent `@Transactional(readOnly = true)`.
- Aucune transaction ne doit englober un appel réseau ou un traitement long.
- Timeout par défaut : 30 secondes.

## Base de données

- **Nom fixe** : `kctm_db` (identique en dev et prod, en dur dans `application.yml`).
- Seuls l'hôte, le port et les identifiants sont en variables d'environnement.
- Un `docker-compose.yml` à la racine du projet backend lance un PostgreSQL 16 avec la base `kctm_db`.

## Démarrage

```bash
# Lancer PostgreSQL local via Docker (depuis kct-manager-api/)
docker compose up -d

# Profil dev (base locale)
./mvnw spring-boot:run

# Profil prod
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Swagger UI

Disponible sur `http://localhost:8080/swagger-ui.html` après démarrage.
