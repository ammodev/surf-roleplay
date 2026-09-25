# ADR-0010: Ranks and qualifications use English identifiers

- **Status:** Accepted
- **Date:** 2026-09-25
- **Supersedes:** none
- **Superseded by:** none

## Context

Police and SAR ranks and qualifications are modelled on German titles
(ADR-0007), and their display names are German (ADR-0008). The repository rule
requires English identifiers. Each entry also has a key that is stored in the
database, so the naming chosen now becomes part of the on-disk format.

## Decision

Rank and qualification objects and their keys use English translations of the
German titles. Keys have the form `roleplay:<organisation>_<kind>_<snake_case_name>`,
for example `roleplay:police_rank_chief_inspector`. The German title is only the
display name. The mapping is:

| Organisation | Kind | Identifier | Display name |
| --- | --- | --- | --- |
| police | rank | Cadet | Polizeimeisteranwärter |
| police | rank | PoliceOfficer | Polizeimeister |
| police | rank | SeniorPoliceOfficer | Polizeiobermeister |
| police | rank | MasterPoliceOfficer | Polizeihauptmeister |
| police | rank | Inspector | Polizeikommissar |
| police | rank | SeniorInspector | Polizeioberkommissar |
| police | rank | ChiefInspector | Polizeihauptkommissar |
| police | rank | FirstChiefInspector | Erster Polizeihauptkommissar |
| police | rank | Superintendent | Polizeirat |
| police | rank | SeniorSuperintendent | Polizeioberrat |
| police | rank | PoliceDirector | Polizeidirektor |
| police | rank | LeadingPoliceDirector | Leitender Polizeidirektor |
| police | rank | PolicePresident | Polizeipräsident |
| police | qualification | SpecialForces | Spezialeinsatzkommando (SEK) |
| police | qualification | DogHandler | Diensthundeführer |
| police | qualification | CriminalInvestigation | Kriminalpolizei |
| police | qualification | TrafficPolice | Verkehrspolizei |
| police | qualification | HelicopterPilot | Hubschrauberpilot |
| sar | rank | RescueAssistant | Rettungshelfer |
| sar | rank | EmergencyMedicalTechnician | Rettungssanitäter |
| sar | rank | Paramedic | Notfallsanitäter |
| sar | rank | EmergencyPhysician | Notarzt |
| sar | rank | LeadEmergencyPhysician | Leitender Notarzt |
| sar | rank | MedicalDirector | Ärztlicher Leiter Rettungsdienst |
| sar | qualification | Rescue | Technische Rettung |
| sar | qualification | WaterRescue | Wasserrettung |
| sar | qualification | MountainRescue | Bergrettung |
| sar | qualification | AirRescue | Luftrettung |

Ranks are listed in ascending order.

## Alternatives considered

### German titles as identifiers

Its advantage: a one-to-one match with the real titles, with no translation
step and no ambiguity.

It was rejected because it puts German identifiers into the code, against the
repository's English-identifier rule.

### Official abbreviations

Its advantage: compact and unambiguous for police ranks (PHK, EPHK).

It was rejected because SAR titles have no standard abbreviations, and the
abbreviations are unreadable without knowing the German originals.

## Consequences

### What this gives us

English identifiers throughout, and readable stored keys.

### What this costs

The translations are approximate. Readers must use the display names or this
table to know the real German title.

### Follow-on work

Implement the entries exactly as listed.

### What this forecloses

Renaming an identifier's key after release, which would need a data migration
of every stored row that references it.
