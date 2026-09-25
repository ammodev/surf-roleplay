# Plan 0001: Roleplay data layer

- **Status:** In progress
- **Date:** 2026-09-25
- **Accepted proposal:** Finish the roleplay data layer: users, identities, ranks, qualifications, and licenses persisted in the microservice, synced to Paper on join and quit
- **Decision records:** ADR-0001, ADR-0002, ADR-0003, ADR-0004, ADR-0005, ADR-0006, ADR-0007, ADR-0008, ADR-0009

## Goal

`./gradlew build` succeeds for every module, including unit tests. A Paper server
loads a joining player's roleplay user, with identities, ranks, qualifications and
licenses, from the microservice, and evicts it on quit. Through the API a caller
can create, activate, clear and delete an identity (at most one per type, each
with its own surf-transaction account named `rp-<type>-<prefix>`), grant a license
(requirements enforced unless forced) and revoke it, set a police or SAR rank, and
add or remove qualifications. Every write is persisted by the microservice. No
`TODO(` or `NotImplementedError` remains in main sources, and every class,
interface, function and public property has a doc comment.

## Out of scope

- Commands of any kind (identity switching, license administration).
- GUIs or menus.
- Cross-server cache invalidation or change events (ADR-0005).
- Loading users on Velocity; Velocity keeps its bootstrap only.
- Database integration tests or Testcontainers.
- Content in the empty `api-client-*` modules.
- Localisation or translation of player-facing text (ADR-0008).
- Migration of existing data (the only existing table holds user UUIDs and is kept).

## Steps

Each step ends in a commit with a Conventional Commits message and no attribution
line. Commits name their paths explicitly, so work that was already staged
before this plan never ends up in a plan commit by accident.

### Step 1 — Fix the standalone defects

**Does:** `VelocityClientInstance` is registered with
`@AutoService(ClientInstance::class)`. `TruckLicense` requires `CarLicense`
instead of itself. The `surf-roleplay-minestom-*-all.jar` pattern is removed from
`.github/workflows/publish.yml`.

**Ends in:** The three defects are gone from the tree. The build is still expected
to fail only at `CoreClientUserManager.kt` (fixed in Step 7).

**Verified by:** `git grep -n "AutoService(VelocityClientInstance" ; git grep -n "minestom" .github ; git grep -n "HasOtherLicenseRequirement(TruckLicense)"`
all print nothing, and `./gradlew :surf-roleplay-api:surf-roleplay-api-common:compileKotlin` succeeds.

**Pushes:** no

### Step 2 — Add unit test infrastructure

**Does:** `kotlin-test` with the JUnit 5 runner is added as a test-only dependency
to `api-common`, `core-common` and `core-client-common`, and `useJUnitPlatform()`
is configured for them.

**Ends in:** The three modules have a `test` task that runs JUnit Platform tests.

**Verified by:** `./gradlew :surf-roleplay-api:surf-roleplay-api-common:test --dry-run`
lists the `test` task for each of the three modules, and
`./gradlew :surf-roleplay-api:surf-roleplay-api-common:dependencies --configuration testRuntimeClasspath`
shows `kotlin-test-junit5`.

**Pushes:** no

### Step 3 — Reference licenses by key and implement the registry

**Does:** `UserLicense.licenseUuid` becomes `licenseKey: Key`. `LicenseRegistry`
offers `getByKey(key)` in place of `getByUuid` and `getByName`. `CoreLicenseRegistry`
(`@AutoService(LicenseRegistry::class)`, in `core-common`) registers `CarLicense`
and `TruckLicense`. `RoleplayIdentity.hasLicense` ignores revoked licenses. A
`LicenseGrantResult` type is added (success, already owned, requirements not met
with the requirement breakdown).

**Ends in:** Licenses resolve by key, and revoked licenses no longer count as held.

**Verified by:** `./gradlew :surf-roleplay-api:surf-roleplay-api-common:test :surf-roleplay-core:surf-roleplay-core-common:test`
passes with tests for: registry lookup by key, `hasLicense` false for a revoked
license, `TruckLicense` requirement met and not met.

**Pushes:** no

### Step 4 — Add organisation-typed ranks and qualifications

**Does:** `IdentityType` (`CIVILIAN`, `POLICE`, `SAR`) replaces the hard-coded
`name` strings on identities. Sealed hierarchies `PoliceRank`, `SarRank`,
`PoliceQualification` and `SarQualification` are added, each with `entries` and
`byKey`, and ranks carry a `level`. The entries, with German display names, are:
police ranks Polizeimeisteranwärter, Polizeimeister, Polizeiobermeister,
Polizeihauptmeister, Polizeikommissar, Polizeioberkommissar, Polizeihauptkommissar,
Erster Polizeihauptkommissar, Polizeirat, Polizeioberrat, Polizeidirektor,
Leitender Polizeidirektor, Polizeipräsident; police qualifications SpecialForces
(SEK), Diensthundeführer, Kriminalpolizei, Verkehrspolizei, Hubschrauberpilot;
SAR ranks Rettungshelfer, Rettungssanitäter, Notfallsanitäter, Notarzt, Leitender
Notarzt, Ärztlicher Leiter Rettungsdienst; SAR qualifications Rescue (Technische
Rettung), Wasserrettung, Bergrettung, Luftrettung. `PoliceIdentity.rank` is a
`PoliceRank` and its qualifications are `PoliceQualification`s. SAR works the
same way. The generic `IdentityRank` and `IdentityQualification` stay as the
common supertypes.

**Ends in:** Assigning a SAR qualification to a police identity does not compile.

**Verified by:** `./gradlew :surf-roleplay-api:surf-roleplay-api-common:test` passes
with tests for `byKey` round-trips of every entry, unique keys per hierarchy, and
strictly increasing rank levels.

**Pushes:** no

### Step 5 — Declare the write API

**Does:** `RoleplayUser` offers `createIdentity(type): RoleplayIdentity`,
`setActiveIdentity(identity)`, `clearActiveIdentity()` and
`deleteIdentity(identity)`. `RoleplayIdentity` gains `accountId`, the account-scoped
convenience money operations, `grantLicense(license, grantedBy, force = false)`
and `revokeLicense(license, revokedBy, reason)`. `PoliceIdentity` and `SarIdentity`
gain `setRank`, `addQualification` and `removeQualification`. Failure types for a
duplicate identity type and an unknown identity are added.

**Ends in:** `api-common` compiles with the full write API declared.

**Verified by:** `./gradlew :surf-roleplay-api:surf-roleplay-api-common:compileKotlin` succeeds.

**Pushes:** no

### Step 6 — Define the DTOs and the RPC contract

**Does:** `RoleplayUserDto` carries a list of `RoleplayIdentityDto` (uuid, type,
accountId, rank key, qualification keys, list of `UserLicenseDto`). `UserService`
gains `createIdentity`, `deleteIdentity`, `grantLicense`, `revokeLicense`,
`setRank`, `addQualification` and `removeQualification`, each returning the updated
DTO. A mapper in `core-common` turns DTOs into domain values.

**Ends in:** The wire contract and its mapping exist in `core-common`.

**Verified by:** `./gradlew :surf-roleplay-core:surf-roleplay-core-common:test` passes
with mapping tests for every identity type, for unknown rank and qualification
keys, and for revoked licenses.

**Pushes:** no

### Step 7 — Implement the client-side domain and user manager

**Does:** `CoreRoleplayUser` drops its 12 money overrides and implements the write
API through `UserService`, replacing its cached state with every returned DTO.
Identity implementations for civilian, police and SAR are added. Identity creation
generates `rp-<type>-<prefix>`, lengthens the prefix on a foreign-owned name,
reuses an own account, creates the account otherwise, then calls the RPC.
Identity deletion calls the RPC, then deletes the account via
`TransactionUser.deleteAccount`, and clears the active identity if it was the one
deleted. `CoreClientUserManager` maps DTOs, fills its cache and gains `evict(uuid)`.

**Ends in:** Every module compiles again.

**Verified by:** `./gradlew compileKotlin` succeeds, and
`./gradlew :surf-roleplay-core:surf-roleplay-core-client:surf-roleplay-core-client-common:test`
passes with tests for account-name generation, prefix lengthening, own-account
reuse, and rejection of a second identity of the same type.

**Pushes:** no

### Step 8 — Persist everything in the microservice

**Does:** Tables `roleplay_identities` (unique on user and type),
`roleplay_identity_qualifications` and `roleplay_identity_licenses` are added and
created at bootstrap. `UserServiceImpl` implements every RPC from Step 6 and
returns the full user DTO.

**Ends in:** The microservice implements the whole `UserService` contract.

**Verified by:** `./gradlew :surf-roleplay-microservice:compileKotlin` succeeds, and
reading `UserServiceImpl` shows no unimplemented member.

**Pushes:** no

### Step 9 — Load on join, evict on quit

**Does:** A Paper listener loads the user with `findOrCreateByUuid` on
`AsyncPlayerPreLoginEvent` (the login is refused with a message if the RPC fails)
and evicts it on `PlayerQuitEvent`. The generated `paper-plugin.yml` is checked
for a dependency on surf-transaction.

**Ends in:** The listener is registered in `PaperMain`, and the plugin descriptor
declares the surf-transaction dependency.

**Verified by:** `./gradlew :surf-roleplay-paper:build` succeeds, and the built
jar's `paper-plugin.yml` lists surf-transaction. If it does not and the Gradle
plugin offers no way to declare it, the agent stops and asks.

**Pushes:** no

### Step 10 — Add doc comments everywhere

**Does:** Every class, interface, object, function and public property in main
sources gets a KDoc that says what it does, following the documentation rules in
`CLAUDE.md`.

**Ends in:** No undocumented declaration remains.

**Verified by:** a review pass over every `.kt` file under `src/main`, listing each
file checked, and `git grep -nE "ADR-|plan-|as requested" -- "*.kt"` prints nothing.

**Pushes:** no

### Step 11 — Verification

**Does:** Runs the full build and the goal checks.

**Ends in:** Every clause of the Goal is confirmed or reported as not met.

**Verified by:**
- `./gradlew build` succeeds, and all tests pass.
- `git grep -nE "TODO\(|NotImplementedError" -- "*/src/main/*"` prints nothing.
- Step 10's doc comment review found no gaps.
- `git log --format=%B` over the plan's commits contains no `Co-Authored-By` or
  other attribution line.

**Pushes:** no

## Push points

none

## Risk

Step 7 is the most likely to go wrong. It is the only step that depends on
surf-transaction behaviour at runtime (account lookup by name, ownership, deletion
through `TransactionUser`), which can only be read from the API sources and not
exercised by unit tests. If the API does not support a required operation as the
plan assumes, for example if there's no way to read an account's owner, the agent
stops and asks instead of changing the account scheme from ADR-0002.

## If reality contradicts this plan

The agent stops and asks. It does not silently rewrite this plan to match what
actually happened, and it does not continue past a step whose stated end state was
not reached.
