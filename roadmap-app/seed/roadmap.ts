import type { Seed, SeedSystem } from "../src/db/seed-types";

/**
 * Removes the common leading indentation from a template literal, so specs can be
 * written indented in this file and render as clean markdown.
 */
function md(strings: TemplateStringsArray, ...values: unknown[]): string {
  const raw = strings.reduce((acc, s, i) => acc + s + (i < values.length ? String(values[i]) : ""), "");
  const lines = raw.replace(/^\n/, "").replace(/\s+$/, "").split("\n");
  const indent = Math.min(...lines.filter((l) => l.trim()).map((l) => l.match(/^ */)![0].length));
  return lines.map((l) => l.slice(indent)).join("\n");
}

/** Every gamemode system, grouped by phase in delivery order. */
const systems: SeedSystem[] = [
  // ───────────────────────────── P0 Foundations ─────────────────────────────
  {
    id: "identity-data-layer",
    domain: "character",
    phase: "p0",
    title: "Identity data layer",
    summary: "Users, identities, ranks, qualifications and licenses persisted in the microservice and synced to Paper.",
    priority: "MVP",
    status: "Done",
    spec: md`
      Implemented by plan 0001.

      - A user holds at most one identity per type: civilian, police, SAR (ADR-0004).
      - The active identity is not persisted; after every login no identity is active (ADR-0006).
      - Licenses are referenced by key (ADR-0003); grants enforce requirements unless forced (ADR-0009).
      - Paper loads a player's data on login and evicts it on quit.

      Follow-up from later decisions: bank accounts move out of identity creation (ADR-0016),
      ranks/qualifications/licenses move to config or DB (ADR-0015), dashboard events reload data (ADR-0018).
    `,
    tasks: [
      "Migrate ranks, qualifications and licenses from sealed types to config/DB (ADR-0015)",
      "Remove automatic account creation from identity creation (ADR-0016)",
      "Subscribe to dashboard Redis events and reload affected users (ADR-0018)",
    ],
  },
  {
    id: "client-mod",
    domain: "platform",
    phase: "p0",
    title: "Client mod skeleton",
    summary: "Required Fabric mod that renders all UI, vehicles, appearance and effects.",
    priority: "MVP",
    spec: md`
      - Fabric mod, built in this repository as a Gradle module (ADR-0012).
      - Required: the server refuses clients without a completed mod handshake.
      - Owns: all screens and menus, forms, phone, tablet, HUD, minimap, custom inventory, vehicles,
        layered skins, 3D attachments, furniture models, animations, screen effects, keybinds.
      - Keybinds are user-configurable in a settings screen.
      - Vanilla UI changes: hide coordinates in F3, hide the Tab player list, replace chat with RP chat,
        disable vanilla crafting.
      - Server is authoritative; the mod sends inputs and requests only (ADR-0020).
    `,
    tasks: [
      "Create the Fabric mod Gradle module",
      "Settings screen with rebindable keybinds",
      "Hide F3 coordinates and the Tab player list",
      "Disable vanilla crafting UI",
      "Screen framework for server-driven mod screens",
    ],
  },
  {
    id: "protocol",
    domain: "platform",
    phase: "p0",
    title: "Protocol module and handshake",
    summary: "Typed kotlinx.serialization packets over roleplay:* payload channels, shared by mod and server.",
    priority: "MVP",
    spec: md`
      - Custom payload channels in the \`roleplay\` namespace (ADR-0013).
      - Every packet is a Kotlin class serialized with kotlinx.serialization in a shared protocol module.
      - Handshake on join: the mod sends its protocol version; mismatch kicks with "Update über den Launcher".
      - The launcher also forces updates and the server checks a hash (both layers).
      - The mod reports its loaded mods; the server rejects non-whitelisted ones.
    `,
    tasks: [
      "Shared protocol Gradle module",
      "Packet registry on Paper and on Fabric",
      "Version handshake with kick message",
      "Mod list report and whitelist check",
      "Rules for bumping the protocol version",
    ],
  },
  {
    id: "launcher",
    domain: "platform",
    phase: "p0",
    title: "Launcher",
    summary: "Electron launcher with Microsoft login, CDN manifest sync and mod whitelist enforcement.",
    priority: "MVP",
    spec: md`
      - Electron app in this repository (ADR-0022).
      - Microsoft (Xbox) login, tokens stored securely.
      - Versioned manifest (files + hashes) on the project's own CDN; the launcher diffs and downloads.
      - Ships Fabric, the roleplay mod, Simple Voice Chat, performance mods (Sodium, Lithium, ...),
        optional Iris shaders.
      - Nothing else is allowed. Players ask on Discord for a mod to be whitelisted; staff add the mod id
        in the admin dashboard; the launcher picks it up on next start. The game does not start with
        non-whitelisted mods.
      - Java runtime management.
    `,
    tasks: [
      "Electron project scaffold",
      "Microsoft authentication flow",
      "Manifest format and CDN upload pipeline",
      "Diff-based file sync",
      "Mod whitelist sync from the dashboard",
      "Java runtime download and management",
      "Release signing and auto-update",
    ],
  },
  {
    id: "content-registries",
    domain: "platform",
    phase: "p0",
    title: "Content registries (config/DB)",
    summary: "All content and tuning defined in config or DB with validation and live reload.",
    priority: "MVP",
    spec: md`
      - Everything controllable via config or DB, whichever is easier per kind (ADR-0015).
      - Items fully config/DB, behaviours via generic components (food, tool, container, weapon, ...).
      - Licenses, ranks and qualifications move to config/DB.
      - Per-module config files (prices live with their module).
      - Stable string keys; validation at load; reload without restart.
    `,
    tasks: [
      "Registry framework with validation and reload",
      "Item definitions with behaviour components",
      "License, rank and qualification definitions",
      "Per-module config loading",
    ],
  },
  {
    id: "custom-inventory",
    domain: "items",
    phase: "p0",
    title: "Custom player inventory",
    summary: "Server-side inventory model rendered by the mod: base slots, backpacks, clothing and gear layers, weight.",
    priority: "MVP",
    spec: md`
      - Fully custom inventory; the vanilla inventory is unused.
      - Base slots + backpack tiers unlock more slots. The backpack is a container item: unequipping keeps
        its contents inside; dropping or stealing it moves the loot with it. Full backpacks can be stored
        in stashes/trunks and count their full weight.
      - Weight overlay: soft cap (slower, no sprint) and hard cap (cannot pick up more).
      - Clothing layer slots: top, jacket, pants, shoes, hat, glasses, mask, gloves, jewellery, watch, bag.
      - Gear layer slots (on top of clothing): vest/body armor, helmet, belt/holster, utility (radio, phone,
        keys, wallet).
      - Hotbar = quick slots bound from the inventory (qbox-style).
      - Give/drop only (no trade screen).
      - Items have durability (tools), spoilage (food, slowed in fridges), quality (resources) and
        serial numbers (weapons, phones).
      - Inventories are per identity.
    `,
    tasks: [
      "Inventory data model and persistence (microservice)",
      "Weight calculation and soft/hard caps",
      "Backpack container items",
      "Clothing and gear slot layers",
      "Quick slots / hotbar binding",
      "Mod inventory screen",
      "Give and drop actions",
      "Durability, spoilage, quality and serial numbers",
    ],
  },
  {
    id: "interaction",
    domain: "platform",
    phase: "p0",
    title: "Interaction: target eye and radial menu",
    summary: "Target eye for world objects plus a radial menu for self, vehicle and job actions.",
    priority: "MVP",
    spec: md`
      - Target eye (qb-target style): hold a key, look at an object, NPC, player or vehicle, pick from a context list.
      - Radial menu (qbox radial) for self, vehicle and job actions.
      - Actions are registered server-side with conditions; the mod only renders and sends the choice.
    `,
    tasks: ["Server-side action registry with conditions", "Target eye rendering and raycast", "Radial menu", "Door/terminal/keypad binding to actions"],
  },
  {
    id: "zones",
    domain: "platform",
    phase: "p0",
    title: "Zone system and in-game editor",
    summary: "Polygon zones with height, typed flags and region-aware lookup, placed in an in-game editor.",
    priority: "MVP",
    spec: md`
      - Own zone system replacing WorldGuard (ADR-0019).
      - Polygon + min/max Y zones; flags: no-violence, speed limit, radio dead zone, protected reserve, no-build, ...
      - Region-thread aware lookup (Canvas / Folia-based fork).
      - The map is not started: every location (shops, fields, zones, doors, garages, NPCs) is placed by
        staff in an in-game editor (mod UI), saved to the DB and live-reloaded.
      - Drawn road graph (nodes/edges with speed limit, one-way, name) for GPS and speed limits.
      - Builder tools: interior template tool, furniture/model preview, door/interaction binding.
    `,
    tasks: [
      "Zone model and spatial index",
      "Flag registry",
      "In-game zone editor (mod)",
      "Point/location editor for shops, NPCs, garages",
      "Road graph editor",
      "Interior template save/paste tool",
      "Model preview for builders",
    ],
  },
  {
    id: "data-sync",
    domain: "platform",
    phase: "p0",
    title: "Data ownership and dashboard sync",
    summary: "Microservice owns all persistent data; the dashboard writes the DB and publishes Redis events.",
    priority: "MVP",
    spec: md`
      - The microservice stores all persistent game data (ADR-0017).
      - The dashboard writes the DB directly and publishes Redis change events; Paper reloads affected data (ADR-0018).
      - Staff and organisation management lives in the external dashboard (built later by the owner).
    `,
    tasks: ["Redis change event schema", "Paper subscribers that reload affected data", "Schema and invariants documentation for the dashboard"],
  },
  {
    id: "audit-log",
    domain: "staff",
    phase: "p0",
    title: "Audit events",
    summary: "Structured events for money, item, license and arrest actions, consumed by the dashboard.",
    priority: "MVP",
    spec: md`
      - Every money, item, license and arrest action emits a structured audit event.
      - Money flows use surf-transaction transaction data for source and sink.
      - Physical cash moves outside surf-transaction; item-level audit events cover it.
    `,
    tasks: ["Audit event schema", "Emit events from economy, items, licenses, police"],
  },
  {
    id: "rp-chat",
    domain: "platform",
    phase: "p0",
    title: "Roleplay chat",
    summary: "Local /me and /do, OOC channel, staff announcements, text chat range.",
    priority: "MVP",
    spec: md`
      - Vanilla chat replaced by RP chat.
      - /me and /do shown above the head and in local chat.
      - Local /ooc, rate-limited.
      - Staff broadcasts and government announcements.
      - Local text only within a configurable range.
      - No name tags above players.
    `,
    tasks: ["Chat channels and range", "/me /do overhead text", "OOC rate limit", "Announcements"],
  },

  // ───────────────────────────── P1 Civil core ─────────────────────────────
  {
    id: "character-creator",
    domain: "character",
    phase: "p1",
    title: "Character creator and appearance",
    summary: "Layered HD skins with 3D attachments, rendered by the mod.",
    priority: "MVP",
    spec: md`
      - Creator parts: body type (slim/wide), skin tone, eye color, eyebrows, hair + color, beard + color,
        freckles, scars, makeup, tattoos, clothing layers.
      - The mod composites layers locally; no MineSkin needed.
      - Vanilla model + 3D attachments (hair, hats, glasses, vests, holsters, backpacks) + higher-resolution
        texture overlays (tattoos, etc.).
      - No fallback for unmodded clients.
      - Appearance is stored per identity.
    `,
    tasks: ["Appearance data model", "Layer compositor in the mod", "3D attachment rendering", "Creator screen", "Clothing layer sync to other players"],
  },
  {
    id: "identity-data",
    domain: "character",
    phase: "p1",
    title: "Identity personal data",
    summary: "Name, date of birth, gender, nationality, height per identity; name rules.",
    priority: "MVP",
    spec: md`
      - Each identity carries first/last name, DOB, gender, nationality, height.
      - Realistic names only: letters, sensible length, blocklist of famous names.
      - Name changes only at the Bürgeramt for a fee.
      - No name tags; you learn names through roleplay.
    `,
    tasks: ["Personal data fields on identities", "Name validation and blocklist", "Paid name change"],
  },
  {
    id: "character-select",
    domain: "character",
    phase: "p1",
    title: "Character select and identity separation",
    summary: "Pick civilian, police or SAR on join; strict separation between own identities.",
    priority: "MVP",
    spec: md`
      - Character select screen on join (qbox multichar style). Switching means returning to select.
      - Stored per identity: position, health, hunger, thirst, injuries, inventory, appearance, phones,
        keys, reputation and skills.
      - Strict separation: no transfers between own identities, a switch cooldown, no switching while
        wanted, injured or in restricted zones.
      - Police and SAR remain separate identities with their own money.
    `,
    tasks: ["Character select screen", "Per-identity state save/restore", "Switch cooldown and restrictions", "Block transfers between own identities"],
  },
  {
    id: "onboarding",
    domain: "character",
    phase: "p1",
    title: "Arrival and Bürgeramt onboarding",
    summary: "Arrival scene, starter phone, ID card and welcome money issued at the Bürgeramt.",
    priority: "MVP",
    spec: md`
      - New players arrive by ferry/plane with a short cinematic and a tutorial path.
      - Starter phone (no SIM contract).
      - The ID card is obtained at the Bürgeramt, not on join.
      - Welcome money (configurable amount) is paid when the ID card is issued.
      - Registration unlocks the Jobcenter and benefits.
      - The bank account is opened separately at a bank; the phone contract is bought separately.
    `,
    tasks: ["Arrival cinematic", "Tutorial path", "Bürgeramt registration flow", "Welcome money payout"],
  },
  {
    id: "id-card",
    domain: "character",
    phase: "p1",
    title: "Personalausweis",
    summary: "ID card item with personal data; shown to others; losable, stealable and forgeable.",
    priority: "MVP",
    spec: md`
      - Item with name, DOB, nationality, height, face render, ID number, expiry.
      - Show or hand to a nearby player (opens a card screen).
      - Can be lost or stolen; replacement fee at the Bürgeramt.
      - Forgeable (see forgery); MDT lookup reveals mismatches.
    `,
    tasks: ["ID card item data", "Show-ID screen", "Replacement at Bürgeramt"],
  },
  {
    id: "banking",
    domain: "economy",
    phase: "p1",
    title: "Banks, cards and ATMs",
    summary: "Accounts opened at a bank, debit cards with PIN, transfers, shared accounts, loans and savings.",
    priority: "MVP",
    spec: md`
      - Accounts are opened by the player at a bank (ADR-0016), creating a surf-transaction account and an IBAN.
      - Debit card item with PIN; ATMs and card payments.
      - IBAN transfers, statements, standing orders.
      - Shared accounts: joint, company, gang, with permissions.
      - Loans with interest and repayment; savings with interest.
    `,
    tasks: ["Account opening at a bank", "IBAN issuing", "Card items and PIN", "ATM screens", "Transfers and statements", "Standing orders", "Shared accounts with permissions", "Loans", "Savings"],
  },
  {
    id: "cash",
    domain: "economy",
    phase: "p1",
    title: "Bargeld cash item",
    summary: "One cash bundle item carrying a whole-euro amount, splittable, with a counterfeit share.",
    priority: "MVP",
    spec: md`
      - Physical cash as a single "Bargeld" bundle item holding a whole-euro amount (recommendation, accepted).
      - Split and merge in the inventory screen.
      - Carries a counterfeit share for fake money.
      - Lost on death (body bag), robbable.
    `,
    tasks: ["Bargeld item with amount", "Split/merge UI", "Counterfeit share field"],
  },
  {
    id: "state-money",
    domain: "economy",
    phase: "p1",
    title: "Paychecks, benefits, taxes and invoices",
    summary: "State money flows with per-flow configurable intervals.",
    priority: "MVP",
    spec: md`
      - Paychecks for police, SAR and state jobs by rank; AFK players are not paid.
      - Unemployment benefit for registered civilians.
      - Taxes: income tax, sales tax (MwSt.), property and vehicle tax, business tax.
      - Invoices/billing from businesses, SAR and police, paid in the phone bank app.
      - Every flow has its own configurable interval.
      - Money sinks: taxes and fees, upkeep and repairs, item wear and food, fines and bail.
    `,
    tasks: ["Payout scheduler with per-flow intervals", "Paychecks by rank", "Unemployment benefit", "Tax engine", "Invoice system"],
  },
  {
    id: "phone",
    domain: "comms",
    phase: "p1",
    title: "Phone, SIM and cloud account",
    summary: "Phone item required; SIM contracts bought by players; data in a cloud account, PIN-locked.",
    priority: "MVP",
    spec: md`
      - A phone item is required; multiple phones possible.
      - Phone contracts (SIM) are bought by the player.
      - Data lives in a cloud account; police need the PIN or a warrant, and can analyse seized phones.
      - PIN lock, crackable by police forensics or criminals with a tool (minigame/time).
      - Apps: calls (voice via SVC group), SMS 1:1, group chats, location sharing, contacts, bank and
        invoices, services (police/SAR/taxi/mechanic calls, dispatch), social feed, yellow pages/ads,
        dark web (special app), marketplace, maps/GPS, garage app, jobs & business app.
      - No camera app.
    `,
    tasks: ["Phone and SIM items", "Cloud account storage", "PIN lock and cracking", "Phone screen shell", "Calls via SVC", "SMS and group chats", "Contacts and location sharing", "Bank app", "Services app", "Social feed", "Yellow pages", "Dark web app", "Marketplace", "Garage app", "Jobs & business app"],
  },
  {
    id: "hud-needs",
    domain: "platform",
    phase: "p1",
    title: "HUD and needs",
    summary: "Mod-rendered HUD; hunger, thirst, stamina, alcohol level.",
    priority: "MVP",
    spec: md`
      - The mod renders the HUD (no BetterHud).
      - Always shown: health, armor, hunger, thirst; cash, identity/job, radio channel, voice range;
        vehicle HUD (speed, fuel, damage, seatbelt) when driving; compass, street name, minimap.
      - Custom hunger and thirst replace vanilla hunger.
      - Stamina for sprinting and swimming, reduced by weight and injuries.
      - Alcohol level (drunk effects, breathalyser).
      - Empty hunger or thirst leads to the downed state.
      - No temperature system.
    `,
    tasks: ["HUD renderer", "Hunger and thirst", "Stamina", "Alcohol level", "Starvation to downed"],
  },
  {
    id: "map-gps",
    domain: "comms",
    phase: "p1",
    title: "Minimap and GPS",
    summary: "Rendered city map, waypoints and road-graph GPS, only with a phone or GPS item.",
    priority: "MVP",
    spec: md`
      - Mod minimap and full-screen map with street names and blips.
      - Waypoints and GPS routing on the drawn road graph.
      - Requires a phone or GPS device.
      - Hidden locations (dealers, hideouts, illegal spots) never show as blips.
    `,
    tasks: ["Map rendering", "Blips", "Waypoints", "GPS routing on the road graph"],
  },
  {
    id: "voice",
    domain: "comms",
    phase: "p1",
    title: "Voice (Simple Voice Chat)",
    summary: "Mandatory voice with proximity ranges, radio, calls, muffling and megaphone.",
    priority: "MVP",
    spec: md`
      - Simple Voice Chat is mandatory and shipped in the modpack.
      - Proximity ranges (whisper/normal/shout) shown in the HUD.
      - Radio requires an item and a frequency; BOS channels locked to registered radios.
      - Phone calls connect both parties in a private SVC group.
      - Muffled when downed or gagged; megaphone for police vehicles.
    `,
    tasks: ["Proximity ranges", "SVC group management API", "Muffling", "Megaphone"],
  },
  {
    id: "jobs",
    domain: "jobs",
    phase: "p1",
    title: "Free-roam jobs",
    summary: "Trucker, bus, garbage, postal, construction, delivery via job NPCs.",
    priority: "MVP",
    spec: md`
      - No sign-up: grab tasks from job NPCs.
      - Jobs: delivery, trucker, bus, garbage, postal, construction.
      - The Jobcenter handles benefits.
    `,
    tasks: ["Job NPC task framework", "Delivery/trucker routes", "Bus routes", "Garbage, postal, construction tasks"],
  },
  {
    id: "resources",
    domain: "jobs",
    phase: "p1",
    title: "Resource gathering and processing",
    summary: "Farming, mining, oil, fishing and wood via block/entity interaction and NPC processors.",
    priority: "MVP",
    spec: md`
      - Gathering by block/entity interaction.
      - Farming: public fields only, regrowing on timers.
      - Mining: regenerating ore nodes that reset after a timer (not per-player cooldowns).
      - Oil from offshore rigs by boat (tanker transport).
      - Fishing by rod (with Angelschein) and boat nets; species by location/time/weather.
      - Wood and logistics.
      - Processing at fixed NPC processors (sawmill, refinery, mill) with time per batch and a fee.
      - Resources have quality affecting price.
    `,
    tasks: ["Farm fields with regrowth", "Ore nodes with reset timers", "Oil rigs", "Fishing (rod, nets, species)", "Logging", "NPC processors"],
  },
  {
    id: "market",
    domain: "economy",
    phase: "p1",
    title: "Dynamic market and NPC shops",
    summary: "Per-good price elasticity with a period 'top product'; infinite NPC shop stock.",
    priority: "MVP",
    spec: md`
      - Each sale drops the price by X%, recovering Y%/hour toward the base; configurable per good and trader.
      - A "top product" per period cannot drop, then drops hard after the period.
      - NPC shops have infinite stock.
      - Uses surf-npc for shopkeepers.
    `,
    tasks: ["Price elasticity engine", "Top product rotation", "NPC shop screens", "Trader NPCs via surf-npc"],
  },
  {
    id: "death",
    domain: "sar",
    phase: "p1",
    title: "Downed state, bleed-out and NLR",
    summary: "Injury-based downed timer, full item loss into a body bag, new-life rule timer.",
    priority: "MVP",
    spec: md`
      - Downed state with an injury-based, configurable timer.
      - After bleed-out: respawn at the hospital with full health.
      - Everything carried drops into a lootable body bag for N minutes (others and police can loot/seize).
      - New-life rule: rules + timer with a HUD hint and a zone ban around the death location.
      - Disconnect mid-roleplay: instant logout and a staff log entry.
    `,
    tasks: ["Downed state", "Bleed-out timer by injury", "Body bag drops", "Hospital respawn", "NLR timer and zone ban"],
  },

  // ───────────────────────────── P2 Vehicles ─────────────────────────────
  {
    id: "vehicle-engine",
    domain: "vehicles",
    phase: "p2",
    title: "Vehicle engine and netcode",
    summary: "Own arcade-realistic physics with client prediction and server reconciliation.",
    priority: "MVP",
    spec: md`
      - Own vehicle engine in the mod and server, no ModelEngine.
      - Arcade-realistic (GTA-like) physics.
      - Client prediction + server reconciliation; others interpolate (ADR-0021).
      - Blockbench models in an own format with bones for wheels, doors, hood, trunk, steering (ADR-0014).
      - Types: cars, trucks, motorcycles, bicycles, emergency vehicles, boats, aircraft (helicopters, planes).
      - Multiple seats.
    `,
    tasks: ["Shared physics module", "Input and state packets", "Reconciliation and interpolation", "Model format and renderer", "Seats", "Boats", "Aircraft"],
  },
  {
    id: "vehicle-features",
    domain: "vehicles",
    phase: "p2",
    title: "Vehicle interactions",
    summary: "Doors, trunk, hood, seatbelt, lights, indicators, horn, sirens, crashes.",
    priority: "MVP",
    spec: md`
      - Open/close doors, trunk, hood; trunk access only when open.
      - Seat switching, seatbelt (ejection without one).
      - Headlights, indicators, hazards, horn; sirens and blue lights for emergency vehicles.
      - Speed-based vehicle damage and occupant injuries.
      - Trunk storage per vehicle.
    `,
    tasks: ["Door/trunk/hood animation", "Seatbelt and ejection", "Lights and horn", "Sirens and blue lights", "Crash damage and injuries", "Trunk inventory"],
  },
  {
    id: "vehicle-ownership",
    domain: "vehicles",
    phase: "p2",
    title: "Keys, fuel, garages and insurance",
    summary: "Keys and lockpicking, fuel types, garages only, insurance, German plates.",
    priority: "MVP",
    spec: md`
      - Key items, lockpicking, handing keys to others.
      - Fuel types: petrol, diesel, electric (charging), kerosene; jerry cans; siphoning.
      - Garages only: vehicles return to a garage; police impound and towing.
      - Destroyed vehicles: insurance with deductible, otherwise gone.
      - German plates (e.g. "ISL-AB 123") assigned at registration; wish plates for a fee.
      - No TÜV.
    `,
    tasks: ["Vehicle keys", "Lockpicking", "Fuel and stations", "Jerry cans and siphoning", "Garages", "Impound", "Insurance", "Registration and plates"],
  },
  {
    id: "dealership",
    domain: "vehicles",
    phase: "p2",
    title: "Dealership and used market",
    summary: "NPC showroom with license checks and limited stock; transfers at the Zulassungsstelle.",
    priority: "MVP",
    spec: md`
      - NPC dealership showroom (3D preview), no test drives.
      - Only vehicles you hold the license for.
      - Limited stock for rare vehicles per period.
      - Player sales transfer at the registration office (Zulassungsstelle).
    `,
    tasks: ["Showroom screen", "License check", "Stock limits", "Ownership transfer at Zulassungsstelle"],
  },

  // ───────────────────────────── P3 Emergency basics ─────────────────────────────
  {
    id: "police-core",
    domain: "police",
    phase: "p3",
    title: "Police field tools",
    summary: "Cuff, escort, search, taser/pepper/baton, radar, breathalyser, spike strips.",
    priority: "MVP",
    spec: md`
      - Cuffs, escort/drag, put in vehicle, frisk and seize.
      - Non-lethal: taser, pepper spray, baton.
      - Radar, breathalyser, spike strips, road blocks.
      - Police buy their own equipment by rank at any station; heavy equipment only at headquarters.
      - Units: Streife, SEK, Verkehrspolizei/Autobahn, Wasserschutz/Hubschrauber (qualifications).
      - Callsigns chosen per shift. Being in the police identity means on duty.
    `,
    tasks: ["Cuff/escort/search", "Taser, pepper spray, baton", "Radar and breathalyser", "Spike strips", "Equipment shop by rank", "Callsign selection"],
  },
  {
    id: "mdt",
    domain: "police",
    phase: "p3",
    title: "MDT, records and evidence",
    summary: "Person/vehicle lookup, reports, evidence, BOLO, warrants, criminal record.",
    priority: "MVP",
    spec: md`
      - Persons: ID data, licenses, records, warrants, known vehicles/phones.
      - Vehicles: plate to owner, registration, insurance, stolen flag.
      - Reports and evidence locker (seized items, phone dumps) with chain of custody.
      - Phone forensics; shop/street cameras recording who was near a robbery.
      - BOLO and warrants (search warrants needed for hideout raids).
      - Permanent criminal record.
    `,
    tasks: ["Person lookup", "Vehicle lookup", "Reports", "Evidence locker", "Phone forensics", "Camera footage", "BOLO and warrants", "Criminal record"],
  },
  {
    id: "justice",
    domain: "police",
    phase: "p3",
    title: "Fines, points, prison",
    summary: "Fine catalogue within staff bounds, Flensburg points, prison with work and escapes.",
    priority: "MVP",
    spec: md`
      - Bußgeldkatalog edited by the government within staff bounds.
      - Invoices with deadline: unpaid fines double, then a warrant.
      - Points: 8 points suspend the license, retest at the driving school.
      - License suspension/revocation by police.
      - Prison (JVA): transport and intake, work reduces time, escapes possible, time runs online only.
      - No courts.
    `,
    tasks: ["Fine catalogue", "Fine invoices and escalation", "Points and suspension", "Prison intake", "Prison jobs", "Escape alerts"],
  },
  {
    id: "radio",
    domain: "emergency",
    phase: "p3",
    title: "Digital radio and radio groups",
    summary: "Radio groups with undeletable system groups, Sicherheitskarte tracking, jammers.",
    priority: "MVP",
    spec: md`
      - System groups (undeletable): Polizei 1-3, SAR 1-3, BOS, civilian channels.
      - Dispatch can create/delete groups and move units between them; temporary Einsatz groups.
      - Radios are registered (like the Digitalfunk Sicherheitskarte). The position follows the radio item, so
        a seized radio is trackable. Gangs can destroy it. An Einsatzleiter can unregister a radio, which cuts
        its output.
      - BOS coverage is island-wide; civilian radios have limited range; jammers block nearby radios;
        dead zones (tunnels, basements, underwater).
      - Notrufknopf on the radio: FMS 0 alert with position to Leitstelle and the group, priority talk.
    `,
    tasks: ["Radio item and registration", "System groups", "Dynamic groups", "Radio tracking", "Unregister/cut radio", "Range, jammers, dead zones", "Notrufknopf"],
  },
  {
    id: "leitstelle",
    domain: "emergency",
    phase: "p3",
    title: "Leitstelle and dispatch",
    summary: "Shared police/SAR control center with Einsätze, FMS statuses and unit assignment.",
    priority: "MVP",
    spec: md`
      - Physical Leitstelle building; dispatch works only there.
      - Staffed by police or SAR members with the dispatch qualification; shared for police and SAR.
      - Einsätze are created from phone calls and automatic alerts (robberies, shots, Blitzer, ...).
      - Dispatch assigns units (waypoint + status), can spawn an Einsatz radio group and move units in.
      - Full FMS 0-9.
      - Closing an Einsatz writes a report linked to the MDT.
      - Dispatchers accept emergency calls and get connected via an SVC group.
    `,
    tasks: ["Einsatz model", "Call intake", "Automatic alerts", "Unit assignment and waypoints", "FMS statuses", "Einsatz reports", "Leitstelle screen"],
  },
  {
    id: "einsatzleiter-tablet",
    domain: "emergency",
    phase: "p3",
    title: "Einsatzleiter tablet",
    summary: "Field tablet showing unit positions via their radios.",
    priority: "MVP",
    spec: md`
      - Requires the Einsatzleiter qualification.
      - Shows unit positions (from radios), statuses and Einsätze in the field.
      - Can unregister radios (cuts output).
    `,
    tasks: ["Tablet item and screen", "Live unit map", "Radio unregister action"],
  },
  {
    id: "injuries",
    domain: "sar",
    phase: "p3",
    title: "Body-part injuries and treatment",
    summary: "Wounds, fractures, burns, concussion per body part; field items and clinic treatment.",
    priority: "MVP",
    spec: md`
      - Per body part (head, torso, arms, legs): wounds and bleeding, fractures, burns and smoke, concussion.
      - Field treatment with items (bandage, splint, burn gel); heavy injuries need the clinic.
      - Medic screen shows the diagnosis.
      - Armor protects per body part and has durability.
    `,
    tasks: ["Injury model", "Injury effects", "Treatment items", "Diagnosis screen", "Armor per body part"],
  },
  {
    id: "hospital",
    domain: "sar",
    phase: "p3",
    title: "Hospital, NPC medic, pharmacy",
    summary: "Surgery and X-ray, NPC fallback medic, billable special treatment, pharmacy.",
    priority: "MVP",
    spec: md`
      - Surgery minigame (Notarzt qualification), X-ray/diagnosis.
      - NPC medic: callable after a timer; free if no SAR is on duty, expensive if SAR is online.
      - Treatment by player SAR is free; SAR bills only special treatment: cosmetic/tattoo removal,
        medical certificates, private room/fast track.
      - Painkillers, pharmacy with prescriptions, illegal meds (EULA check pending).
    `,
    tasks: ["Surgery minigame", "X-ray", "NPC medic call", "Billable services", "Pharmacy and prescriptions"],
  },
  {
    id: "sar-org",
    domain: "sar",
    phase: "p3",
    title: "SAR organisation",
    summary: "One SAR ladder with branch qualifications; equipment bought by rank.",
    priority: "MVP",
    spec: md`
      - One SAR rank ladder; qualifications for Rettungsdienst/Notarzt, Feuerwehr, Technische Hilfe,
        Wasser-/Luftrettung, dispatch.
      - Equipment bought by rank like police; heavy gear at the main station.
      - Qualification-gated vehicles: RTW/NEF, LF/DLK, tow/recovery truck, rescue boat, helicopter.
    `,
    tasks: ["SAR qualifications in config", "Equipment shop", "Vehicle gating"],
  },
  {
    id: "weapons",
    domain: "police",
    phase: "p3",
    title: "Weapons (bows, crossbows, melee)",
    summary: "No firearms; bows/crossbows with custom stats, melee, licenses and serials.",
    priority: "MVP",
    spec: md`
      - No firearms (EULA).
      - Bows and crossbows with custom attributes (accuracy, ...).
      - Melee: batons, knives, bats with body-part damage.
      - Waffenschein required for legal weapons; serials registered to the owner.
    `,
    tasks: ["Ranged weapon stats", "Melee weapons", "Weapon license", "Serial registration"],
  },

  // ───────────────────────────── P4 Crime ─────────────────────────────
  {
    id: "minigames",
    domain: "crime",
    phase: "p4",
    title: "Minigames",
    summary: "Lockpick, hacking, drilling/thermite, safe cracking.",
    priority: "Later",
    spec: md`
      - Lockpick (doors, cars, cuffs escape).
      - Hacking (pattern, sequence, wire matching).
      - Drilling and thermite.
      - Safe cracking.
    `,
    tasks: ["Lockpick", "Hacking", "Drilling/thermite", "Safe cracking"],
  },
  {
    id: "robberies",
    domain: "crime",
    phase: "p4",
    title: "Robberies and heists",
    summary: "Shops, gas stations, ATMs, houses, banks, jewellery, federal reserve, armoured trucks.",
    priority: "Later",
    spec: md`
      - Gating: minimum on-duty police identities online, cooldowns, required gear, crime reputation.
      - Shop robbery: register then safe; silent alarm chance; camera footage (masks hide faces).
      - Big heists are multi-stage (prep, entry, vault, escape; 3-8 players).
      - Mix of GUI minigames and timers.
      - Player hostages only.
      - Loot is normal cash.
    `,
    tasks: ["Robbery framework with gating", "Shop and gas station", "ATM robbery", "House break-ins", "Bank and jewellery", "Federal reserve", "Armoured truck"],
  },
  {
    id: "crime-rep",
    domain: "crime",
    phase: "p4",
    title: "Crime reputation",
    summary: "Personal and gang reputation unlocking heists, dealers and upgrades.",
    priority: "Later",
    spec: md`
      - Both personal (per identity) and gang reputation, each unlocking different things.
      - Grows with successful crimes; decays when caught.
    `,
    tasks: ["Personal reputation", "Gang reputation", "Unlock rules"],
  },
  {
    id: "contraband",
    domain: "crime",
    phase: "p4",
    title: "Contraband and smuggling",
    summary: "EULA-safe illegal trade: moonshine, counterfeit money, chop shops, smuggling, weapons.",
    priority: "Later",
    spec: md`
      - No drugs (EULA).
      - Alcohol is illegal except in licensed venues; moonshine distilling.
      - Counterfeit money mixed into cash bundles; shops/banks may detect, police scanners detect reliably.
      - Stolen goods and chop shops.
      - Smuggling: boat runs, air drops, truck smuggling through the port, port containers.
      - Illegal weapons: parts crafted at hideout workbenches (no serial), scratched serials, black market,
        special arrows/bolts.
      - Rotating hidden NPC dealers with dynamic, rep-gated prices.
      - No gambling.
    `,
    tasks: ["Moonshine", "Counterfeit money", "Chop shop", "Boat smuggling", "Air drops", "Truck and container smuggling", "Illegal weapon crafting", "Rotating dealers"],
  },
  {
    id: "other-crime",
    domain: "crime",
    phase: "p4",
    title: "Forgery, poaching, artifacts, hacking",
    summary: "Forged documents, poaching, artifact theft, terminal hacks and skimming.",
    priority: "Later",
    spec: md`
      - Forgery: ID cards, licenses, vehicle papers and plates; needs printer/laminator and stolen blanks.
      - Poaching: legal hunting with license and seasons; protected animals; illegal fishing/logging.
      - Artifacts: shipwreck diving, museum heist, excavation sites.
      - Hacking: terminal hacks, phone PIN hacking, camera disabling, ATM skimming.
      - Skimming and hacks yield sellable data items only; no money ever flows into the criminal's account.
    `,
    tasks: ["Document forgery", "Hunting and poaching", "Shipwreck diving", "Museum heist", "Excavation", "Terminal hacks", "Camera disabling", "ATM skimming data items"],
  },
  {
    id: "gangs",
    domain: "gangs",
    phase: "p4",
    title: "Gangs",
    summary: "Player-created gangs with custom ranks, bank, colors and member caps.",
    priority: "Later",
    spec: md`
      - Player-created for a founding fee; member cap raised by hideout upgrades.
      - Leader + custom ranks with permissions (invite, kick, bank, stash, hideout, capture).
      - Gang colors and tag; gang bank.
      - Civilian identities only.
      - Gang wars are roleplay only.
    `,
    tasks: ["Gang creation", "Ranks and permissions", "Gang bank", "Colors and tag"],
  },
  {
    id: "hideouts",
    domain: "gangs",
    phase: "p4",
    title: "Gang hideouts",
    summary: "Captured once, kept while rent is paid, raidable, upgradeable; one per gang.",
    priority: "Later",
    spec: md`
      - Capture: hold the zone, then hacking and other minigames; hard to get.
      - One hideout per gang.
      - Rent is paid manually at the hideout; unpaid rent makes it contestable.
      - Police raids with a warrant, breach tools, stash seizure; a successful raid makes it contestable.
      - Rivals may contest only while rent is overdue or after a raid.
      - Upgrades: black market shop (lockpicks, zip ties, masks, drills; heist gear at higher tiers), stash
        and garage, production (still, counterfeit press, workbench), defense and intel (cameras, alarms,
        fence/laundering).
    `,
    tasks: ["Hideout capture flow", "Rent payment", "Raid flow", "Contest rules", "Upgrade tree", "Black market shop"],
  },
  {
    id: "territory",
    domain: "gangs",
    phase: "p4",
    title: "Territory capture points",
    summary: "Flag/terminal capture then hold; random announced windows; special zone access.",
    priority: "Later",
    spec: md`
      - Interact with all flags/terminals, then hold with a progress bar.
      - Random windows announced via the dark web; police are not notified.
      - Rewards: better dealer prices; access to smuggler cove, protected reserve, chop shop, forger's workshop.
    `,
    tasks: ["Capture point model", "Capture flow", "Random windows", "Reward unlocks"],
  },

  // ───────────────────────────── P5 City ─────────────────────────────
  {
    id: "housing",
    domain: "housing",
    phase: "p5",
    title: "Houses and apartments",
    summary: "Map houses and instanced bedrock-box apartments, buy or rent, keypads, tiers.",
    priority: "Later",
    spec: md`
      - Real houses on the map (interior editable, exterior locked) and instanced apartments.
      - Apartments live in a separate world, each inside a bedrock box. Owners can fully modify walls with
        vanilla blocks from the furniture store; renters can only place furniture; fixtures (toilets, ...)
        are unbreakable in rentals.
      - Buy or rent; unpaid rent/tax: grace period, then eviction to a storage lot, retrievable for a fee.
      - Digital keypads: share the PIN; upgrade the terminal to make cracking harder.
      - Break-ins by lockpick with alarm upgrades.
      - Tiers affect stash size and garage slots; upgrades purchasable.
      - Configurable property limit per identity.
      - Storage, wardrobe, garage, spawn point.
    `,
    tasks: ["Map house regions", "Apartment world and bedrock boxes", "Buy and rent", "Eviction and storage lot", "Keypads", "Tiers and upgrades", "Wardrobe and spawn point"],
  },
  {
    id: "furniture",
    domain: "housing",
    phase: "p5",
    title: "Furniture",
    summary: "Mod-rendered furniture on a grid, functional pieces, sitting and lying.",
    priority: "Later",
    spec: md`
      - Mod-rendered Blockbench models, grid (block) placement only.
      - Functional: safe (PIN stash), fridge (slows spoilage), wardrobe, bed (spawn), workbench, TV/radio.
      - Sitting and lying animations.
    `,
    tasks: ["Furniture placement", "Functional furniture", "Sit/lie"],
  },
  {
    id: "businesses",
    domain: "business",
    phase: "p5",
    title: "Player businesses",
    summary: "Government-approved businesses with production, payroll, registers and invoices.",
    priority: "Later",
    spec: md`
      - Applications via phone or Rathaus; the government approves.
      - Types at launch: food & drink, car dealer & mechanic, retail, security/real estate/nightclub.
      - Recipes at workstations; quality depends on inputs.
      - Hourly wages on duty from the company account.
      - Register/card terminal for goods, invoices for services; sales tax deducted.
      - Sales only while staff are present.
      - Company vehicles and stashes.
      - Licensed venues may serve alcohol.
    `,
    tasks: ["Business application flow", "Employees and ranks", "Payroll", "Workstations and recipes", "Register and card terminal", "Company vehicles and stash"],
  },
  {
    id: "service-jobs",
    domain: "business",
    phase: "p5",
    title: "Service jobs",
    summary: "Taxi meter, news, lawyer, mechanic mechanics.",
    priority: "Later",
    spec: md`
      - Taxi meter by distance.
      - News: publish articles to the phone news app.
      - Lawyer: prison visits and fine negotiation.
      - Mechanic: repair minigame, tuning (performance, visual, illegal), parts as items, towing.
    `,
    tasks: ["Taxi meter", "News app publishing", "Lawyer actions", "Mechanic repair and tuning"],
  },
  {
    id: "government",
    domain: "government",
    phase: "p5",
    title: "Government and Rathaus PC",
    summary: "Elected mayor and council, Rathaus PC, recall votes, emergency powers.",
    priority: "Later",
    spec: md`
      - Mayor + city council, elected. Registered citizens with minimum playtime; one vote per player.
      - Rathaus PC: laws and fine catalogue (within staff bounds), taxes and budget, business and special
        permits, emergency powers.
      - Council votes on mayor proposals; immediate actions like lockdowns are allowed for catastrophes.
      - Recall votes by petition or council, plus an automatic trigger on long inactivity.
      - Council size and terms are configurable.
    `,
    tasks: ["Elections", "Council voting", "Rathaus PC screen", "Law and fine editor", "Tax and budget editor", "Recall votes", "Emergency powers"],
  },
  {
    id: "civic-offices",
    domain: "government",
    phase: "p5",
    title: "Civic offices",
    summary: "Bürgeramt, driving school, Zulassungsstelle, Jobcenter.",
    priority: "Later",
    spec: md`
      - Bürgeramt: ID card, name change, business registration.
      - Driving school: theory test and practical test.
      - Vehicle registration: plates, ownership transfers.
      - Jobcenter: benefits.
    `,
    tasks: ["Driving school tests", "Zulassungsstelle", "Jobcenter"],
  },
  {
    id: "traffic",
    domain: "government",
    phase: "p5",
    title: "Traffic law",
    summary: "Speed zones, Blitzer, parking tickets, license checks.",
    priority: "Later",
    spec: md`
      - Speed limits per road from the road graph, shown in the vehicle HUD.
      - Fixed and mobile speed cameras send tickets to the owner.
      - Driving without a license detected on traffic stops.
      - Parking tickets.
    `,
    tasks: ["Speed zones", "Blitzer", "Parking tickets"],
  },
  {
    id: "fire",
    domain: "sar",
    phase: "p5",
    title: "Fire system",
    summary: "Event-triggered fires that spread in a limited zone.",
    priority: "Later",
    spec: md`
      - Events trigger mod-rendered fires that spread within a limited zone.
      - Extinguished with hoses and extinguishers; cause burns and smoke inhalation.
    `,
    tasks: ["Fire simulation", "Extinguishing", "Fire events"],
  },

  // ───────────────────────────── P6 Live content ─────────────────────────────
  {
    id: "events",
    domain: "world",
    phase: "p6",
    title: "Events and world",
    summary: "Air drops, staff event tools, seasonal events, configurable time and weather.",
    priority: "Nice to have",
    spec: md`
      - Air drops and smuggling events.
      - Staff event tools (scenarios).
      - Seasonal events.
      - Configurable time and weather.
    `,
    tasks: ["Event scheduler", "Staff scenario tools", "Seasonal content", "Time and weather control"],
  },
  {
    id: "animations",
    domain: "platform",
    phase: "p6",
    title: "Animations and effects",
    summary: "RP poses, emotes, job animations, walk styles, screen effects.",
    priority: "Nice to have",
    spec: md`
      - Roleplay poses (cuffed, hands up, carried, downed), emote menu, job/action animations, walk styles.
      - Screen effects: injury/downed, drunk, pepper spray/taser/flash, cinematic letterbox and fades.
    `,
    tasks: ["Animation system", "Emote menu", "Walk styles", "Screen effects"],
  },
  {
    id: "staff-tools",
    domain: "staff",
    phase: "p6",
    title: "In-game staff tools",
    summary: "Admin mode, spectate, revive/heal/fix, report system.",
    priority: "Nice to have",
    spec: md`
      - Invisible admin mode, teleport, spectate.
      - Revive, heal, fix commands.
      - In-game reports claimed by staff.
      - AFK players stop receiving paychecks, don't count for police minimums, and are kicked after N minutes.
    `,
    tasks: ["Admin mode", "Support commands", "Report system", "AFK handling"],
  },
];

/** The versioned seed content of the surf-roleplay roadmap. */
export const roadmapSeed: Seed = {
  version: 1,
  domains: [
    { id: "platform", name: "Platform & tooling", description: "Mod, protocol, launcher, content, zones, UI foundations" },
    { id: "character", name: "Characters", description: "Identities, appearance, onboarding, ID card" },
    { id: "items", name: "Items & inventory", description: "Custom inventory, item behaviours" },
    { id: "economy", name: "Economy", description: "Banks, cash, market, state money flows" },
    { id: "comms", name: "Phone & communication", description: "Phone, map, voice" },
    { id: "jobs", name: "Jobs & resources", description: "Free-roam jobs, gathering, processing" },
    { id: "vehicles", name: "Vehicles", description: "Engine, interactions, ownership, dealership" },
    { id: "police", name: "Police", description: "Field tools, MDT, justice, weapons" },
    { id: "emergency", name: "Leitstelle & radio", description: "Dispatch, radio groups, Einsatzleiter tablet" },
    { id: "sar", name: "Rescue & medical", description: "Death, injuries, hospital, SAR, fire" },
    { id: "crime", name: "Crime", description: "Robberies, contraband, other crimes" },
    { id: "gangs", name: "Gangs & territory", description: "Gangs, hideouts, capture points" },
    { id: "housing", name: "Housing", description: "Houses, apartments, furniture" },
    { id: "business", name: "Businesses", description: "Player businesses and service jobs" },
    { id: "government", name: "Government & law", description: "Elections, Rathaus, civic offices, traffic law" },
    { id: "world", name: "World & events", description: "Events, time, weather" },
    { id: "staff", name: "Staff", description: "Audit and staff tools" },
  ],
  phases: [
    { id: "p0", name: "P0 Foundations", goal: "Everything later phases build on: mod, protocol, launcher, content, inventory, interaction, zones.", dependsOn: [] },
    { id: "p1", name: "P1 Civil core", goal: "A civilian can create a character, register, bank, work, gather and shop.", dependsOn: ["p0"] },
    { id: "p2", name: "P2 Vehicles", goal: "Owned, drivable vehicles with keys, fuel, garages and a dealership.", dependsOn: ["p0"] },
    { id: "p3", name: "P3 Emergency basics (closed beta)", goal: "Police and SAR basics, radio and Leitstelle. Closed beta starts here.", dependsOn: ["p1", "p2"] },
    { id: "p4", name: "P4 Crime", goal: "Robberies, contraband, gangs, hideouts and territory.", dependsOn: ["p3"] },
    { id: "p5", name: "P5 City", goal: "Housing, businesses, government and traffic law.", dependsOn: ["p3"] },
    { id: "p6", name: "P6 Live content", goal: "Events, animations polish and staff tooling.", dependsOn: ["p4", "p5"] },
  ],
  systems,
  decisions: [
    { id: "adr-0003", title: "User licenses reference licenses by key", text: "Held licenses store the license definition's key.", adr: "ADR-0003", date: "2026-09-25" },
    { id: "adr-0004", title: "A user has at most one identity per type", text: "One civilian, one police, one SAR identity per player.", adr: "ADR-0004", date: "2026-09-25" },
    { id: "adr-0006", title: "The active identity is not persisted", text: "After login no identity is active until chosen at character select.", adr: "ADR-0006", date: "2026-09-25" },
    { id: "adr-0008", title: "Player-facing text is German", text: "In-game text is German; code and docs are English.", adr: "ADR-0008", date: "2026-09-25" },
    { id: "adr-0009", title: "License grants enforce requirements unless forced", text: "Grants check requirements; staff can force.", adr: "ADR-0009", date: "2026-09-25" },
    { id: "adr-0012", title: "Players must run the roleplay client mod", text: "A required Fabric mod renders all UI, vehicles and appearance.", adr: "ADR-0012", date: "2026-09-26" },
    { id: "adr-0013", title: "Server and mod talk over typed payload channels", text: "kotlinx.serialization packets in a shared protocol module with a version handshake.", adr: "ADR-0013", date: "2026-09-26" },
    { id: "adr-0014", title: "The mod renders models from Blockbench files", text: "Nexo, BetterHud and ModelEngine are dropped.", adr: "ADR-0014", date: "2026-09-26" },
    { id: "adr-0015", title: "Game content is defined in config or the database", text: "Items, licenses, ranks, qualifications, prices and locations are data.", adr: "ADR-0015", date: "2026-09-26" },
    { id: "adr-0016", title: "Bank accounts are opened at a bank", text: "Identities have no account until the player opens one.", adr: "ADR-0016", date: "2026-09-26" },
    { id: "adr-0017", title: "The microservice owns all persistent game data", text: "Paper caches and writes through RPCs.", adr: "ADR-0017", date: "2026-09-26" },
    { id: "adr-0018", title: "The dashboard writes the database and publishes Redis events", text: "Paper reloads affected data on events.", adr: "ADR-0018", date: "2026-09-26" },
    { id: "adr-0019", title: "An own zone system replaces WorldGuard", text: "Polygon zones with flags and an in-game editor.", adr: "ADR-0019", date: "2026-09-26" },
    { id: "adr-0020", title: "The server is authoritative", text: "The mod sends inputs and requests only.", adr: "ADR-0020", date: "2026-09-26" },
    { id: "adr-0021", title: "Vehicles use client prediction with server reconciliation", text: "Responsive driving with server authority.", adr: "ADR-0021", date: "2026-09-26" },
    { id: "adr-0022", title: "The launcher is an Electron app", text: "Microsoft login, CDN manifest, mod whitelist.", adr: "ADR-0022", date: "2026-09-26" },
    { id: "adr-0023", title: "The roadmap app is a Next.js app with SQLite", text: "This tool.", adr: "ADR-0023", date: "2026-09-26" },
    { id: "adr-0024", title: "The roadmap app uses one shared login token", text: "ROADMAP_TOKEN plus a self-declared display name.", adr: "ADR-0024", date: "2026-09-26" },
    { id: "setting", title: "Fictional German city on an island", text: "German institutions, € currency, 150+ players on one Canvas city server behind Velocity.", date: "2026-09-26" },
    { id: "no-drugs", title: "No drugs, no firearms, no gambling", text: "EULA-safe crime: moonshine, counterfeit, smuggling, forgery, poaching, artifacts, hacking.", date: "2026-09-26" },
    { id: "beta-cut", title: "Closed beta after P3", text: "Civil life plus police and SAR basics.", date: "2026-09-26" },
  ],
  questions: [
    { id: "q-illegal-meds", title: "Are illegal meds EULA-safe?", text: "Black-market painkillers/adrenaline may be too close to drugs. Check the Minecraft EULA and usage guidelines.", system: "hospital" },
    { id: "q-cash-tracking", title: "How to monitor physical cash flows?", text: "Bargeld moves outside surf-transaction; decide on item-level audit events for cash handovers.", system: "audit-log" },
    { id: "q-map", title: "Who builds the island map and when?", text: "The map is not started; systems are placed via the in-game editor, but builders need a timeline.", system: "zones" },
  ],
};
