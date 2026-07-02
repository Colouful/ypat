# backend-v2

YPAT modular monolith. Lives next to the legacy `backend/` folder
during the upgrade.

## Status

| PR | State |
|---|---|
| PR-04 (this PR) | Skeleton. Empty Spring Boot app boots on port 8082. |
| PR-05 | Verify legacy `backend/` still compiles under Java 17. |
| PR-06 | 11 module packages + ArchUnit rule set (no Modulith verify yet). |
| PR-11 | First read-only module migration (work / content). |
| PR-12 | Parent upgrade to Spring Boot 3.5.x + Java 21 + Modulith on. |
| PR-22 | Legacy folder deleted, v2 owns 100% of traffic. |

## Why two folders

The legacy `backend/` keeps running untouched for as long as it
takes v2 to migrate every module. Removing the legacy folder too
early means a single bad migration takes the whole site down; the
side-by-side layout means we can switch traffic per-prefix in Nginx
and roll back in seconds.

## Why Spring Boot 2.7.18 first

Spring Boot 1.5.9 → 3.5.x is a 13-version jump. Going through 2.7.18
keeps us on `javax.*` for the first stretch, defers the jakarta
migration to one PR, and gives us a release that's still receiving
security backports from the Spring team.

## Build

```bash
cd backend-v2
mvn -B -ntp clean package
java -jar target/backend-v2-1.0.0-SNAPSHOT.jar
```

Boots on `http://localhost:8082`. No endpoints yet — that's the
point of this skeleton PR.

## Local profile

Currently there's only the default `application.yml`. Profile
overrides will be added when the first real configuration arrives
(real datasource, real Redis, real JWT keys).