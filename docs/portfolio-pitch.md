# Portfolio-Texte: PharmaIndex

Für LinkedIn, Lebenslauf und Fachgespräch (z. B. Java-Entwicklung im Apotheken-/Arzneimitteldatenmarkt). Keine Verbindung zu Avoxa oder ABDATA – synthetische Demodaten.

---

## 1. LinkedIn-Post

**Neues Portfolio-Projekt: PharmaIndex**

Eine Stammdatenplattform für Fertigarzneimittel: REST-Katalog, Tippfehler-tolerantes Matching und Qualitätssicherung – plus JavaFX-Arbeitsplatz für Fachredaktion.

Spannend war nicht das CRUD, sondern die Fachregeln, die Warenwirtschaft voraussetzen: die deutsche **PZN-Prüfziffer** (Gewichte 2–8, mod 11), ein **Trigramm-Index** statt Full-Scan, und QA-Findings als Datensätze statt Logzeilen. Partnerimporte sind **idempotent** (Upsert per PZN) – ein zweiter Feed überschreibt, statt mit 409 zu scheitern.

**Stack:** Java 21 · Spring Boot 3 · JPA/Hibernate · Flyway · OpenAPI · JavaFX · GitHub Actions

🔗 Code: https://github.com/mdacoding/pharma-index

#Java #SpringBoot #JavaFX #HealthTech #SoftwareEngineering

---

## 2. Kurzbeschreibung (CV / Projektliste)

**PharmaIndex** — Java-Plattform für Arzneimittelstammdaten (PZN, ATC, Wirkstoff): REST/B2B-Import mit API-Key, Fuzzy-Matching (Trigramme + Levenshtein, nachvollziehbarer Score), Qualitätsregelwerk mit persistierten Findings, Stammdaten-Revisionen und JavaFX-QA-Workstation. Tests, Flyway, Actuator/Prometheus, Docker/Render-Blueprint. [GitHub](https://github.com/mdacoding/pharma-index)

**Stack:** Java 21 · Spring Boot 3 · Hibernate · Flyway · OpenAPI · JavaFX · H2/MySQL

---

## 3. Case Study fürs Fachgespräch

### Problem

Apotheken und Arztsoftware brauchen verlässliche Fertigarzneimittel-Stammdaten: eindeutige PZN, ATC, Packung, Preis. Tippfehler in der Warenwirtschaft und unsaubere Partnerfeeds sind der Normalfall – nicht die Ausnahme. Qualitätssicherung darf deshalb kein Anhang an Logs sein.

### Lösung

Ein Spring-Boot-Katalog mit drei sichtbaren Arbeitsweisen:

1. **Lookup & Historie** – PZN-Cache, ATC-Kapitel, Revisionen bei jeder Änderung.
2. **Matching** – invertierter Trigramm-Index selektiert Kandidaten, Feinscoring erklärt den Treffer (Dice, Levenshtein, Token, ATC-/Wirkstoff-Boost).
3. **QA** – Regelwerk schreibt Findings (ungültige PZN, ATC, Stärke, Preis, Dubletten). Die JavaFX-Workstation ist der Redaktionsarbeitsplatz.

B2B-CSV-Import ist ein Upsert: derselbe Partnerfeed darf wiederholt laufen.

### Bewusste Grenze

Synthetische Demodaten, H2 für die Demo, API-Key statt OAuth. In Produktion: Oracle/MySQL, gehashte Keys, kein öffentliches Write.

### Ergebnis

- Öffentliches Repo mit OpenAPI, Tests und nachvollziehbarer Domain-Logik
- Recruiter können Matching und QA im Browser sehen, ohne JavaFX starten zu müssen
- Dieselben Fachobjekte (PZN, ATC, AVP), die in Apotheken-IT üblich sind
