---
name: surf-npc
description: How to create and manage non-player characters (NPCs) using the surf-npc API and DSL - creating shopkeepers, quest givers, dealers, job NPCs with the DSL or API, event handling, skins, poses, rotation, equipment, custom properties, and persistence. Use whenever creating, editing, or debugging NPCs in roleplay scenarios.
---

# surf-npc library

Source of truth: `S:\Workspaces\surf-npc` repo, **current default branch is `version/26.1`** (not the local checkout). Read from `origin/version/26.1` without checking out.

The library is packet-based (via PacketEvents). NPCs are fake entities with no physics. They support viewers (per-player visibility), properties (custom per-NPC data), persistence (save to disk), events (interact, collision, show/hide), skins, poses, rotation, scale, and equipment.

## Mental model

NPCs are managed through `SurfNpcApi`. The API creates, stores, and broadcasts entities to viewers. When `viewers` is `null`, the NPC is shown to everyone; when it is an `ObjectSet<UUID>`, only those players see it. Persistent NPCs survive restarts (saved to `plugins/surf-npc/npcs/<uniqueName>.yml`).

```
SurfNpcApi.createNpc(
    displayName: Component,
    uniqueName: String,
    type: EntityType,                    // PLAYER, MANNEQUIN, ARMOR_STAND, etc.
    useTransparentBackground: Boolean = false,
    location: Location,
    viewers: ObjectSet<UUID>? = null,   // null = all players
    rotationType: PER_PLAYER | FIXED,
    persistent: Boolean = false,
    skin: NpcSkin
) -> Npc
```

The `Npc` object is a handle with convenience methods (`show()`, `hide()`, `delete()`, `teleport(player)`, property/equipment getters/setters).

## Dependency

In `build.gradle.kts` for a Paper plugin:

```kotlin
dependencies {
    api("dev.slne.surf:surf-npc-api") // or compileOnly if you shade
}
```

Repository: `https://reposilite.slne.dev/releases` (already configured in SLNE parent Gradle).

Depend on the Paper plugin in your plugin.yml or `build.gradle.kts` with:
```kotlin
serverDependencies { registerRequired("surf-npc") }
```

## Creating NPCs: DSL (preferred)

The DSL is the primary interface. All fields shown are optional except `displayName`, `uniqueName`, `type`, and `location`.

```kotlin
import dev.slne.surf.npc.api.dsl.*
import org.bukkit.entity.EntityType

val shopkeeper = npc {
    displayName { text("Shopkeeper") }  // SurfComponentBuilder
    uniqueName = "merchant_01"
    type = EntityType.PLAYER

    skin {
        ownerName = "PlayerName"
        value = "eyJ0aW1..."  // Mojang texture value (base64)
        signature = "abc..."   // Mojang texture signature
        // parts = objectSetOf(NpcSkinPart.CAPE, NpcSkinPart.HAT)  // optional
    }

    location {
        world = "world"
        x = 100.0; y = 64.0; z = 200.0
    }

    scale(1.5)                         // default 1.0
    rotationType = PER_PLAYER          // or FIXED (default PER_PLAYER)
    persistent = true                  // save to disk (default false)
    useTransparentNametagBackground()  // default false

    withEventHandler<NpcInteractEvent> { event ->
        event.player.openShop()        // your logic
    }

    // Per-NPC properties for custom data
    npcProperty {
        key = "shop_id"
        value = 42
        type = NpcPropertyType.Types.INT_TYPE
    }
}

// shopkeeper is an Npc instance, ready to use
shopkeeper.setEquipment(EquipmentSlot.HAND, ItemType.DIAMOND_SWORD.createItemStack())
```

## Creating NPCs: API (lower-level)

```kotlin
import dev.slne.surf.npc.api.SurfNpcApi
import org.bukkit.Location
import org.bukkit.entity.EntityType
import net.kyori.adventure.text.Component

val npc = SurfNpcApi.createNpc(
    displayName = Component.text("Guard"),
    uniqueName = "guard_01",
    type = EntityType.ARMOR_STAND,
    useTransparentBackground = false,
    location = Location(world, 100.0, 64.0, 200.0),
    viewers = null,  // shown to all
    rotationType = NpcRotationType.PER_PLAYER,
    persistent = false,
    skin = NpcSkin.empty()
)
```

## Fetching skins

Skins are signed Mojang texture data (value + signature). Fetch them from Mojang by username:

```kotlin
suspend fun fetchSkin(username: String): NpcSkin = SurfNpcApi.fetchSkin(username)

// Or in the DSL:
npc {
    skin = fetchedSkin("YourPlayerName")  // suspend function
    // ...
}
```

## Events and event handlers

Two ways to listen for NPC events:

**Per-NPC handlers (in DSL or via `npc.addEventHandler`)**:
```kotlin
val npc = npc {
    withEventHandler<NpcInteractEvent> { event ->
        event.player.sendMessage("You clicked me!")
    }
    withEventHandler<NpcCollisionEvent> { event ->
        event.npc.refreshRotation()
    }
}
```

**Bukkit listeners (global)**:
```kotlin
class MyNpcListener : Listener {
    @EventHandler
    fun onNpcInteract(event: NpcInteractEvent) {
        // event.npc, event.player
    }

    @EventHandler
    fun onNpcCollision(event: NpcCollisionEvent) {
        // player walked into the NPC
    }

    @EventHandler
    fun onNpcShow(event: NpcShowEvent) { }
    
    @EventHandler
    fun onNpcHide(event: NpcHideEvent) { }

    @EventHandler
    fun onNpcCreate(event: NpcCreateEvent) { }

    @EventHandler
    fun onNpcDelete(event: NpcDeleteEvent) { }
}
```

Events are both Bukkit events AND per-NPC handlers. Per-NPC handlers fire first.

## Poses, rotation, scale, equipment

```kotlin
// Set pose (only usable poses work: STANDING, FALL_FLYING, SLEEPING, SWIMMING, SNEAKING, SITTING)
npc.setPose(NpcPose.SITTING)

// Get current pose
val currentPose = npc.getPose()

// Rotation type: PER_PLAYER = faces each player; FIXED = one direction
npc.setRotationType(NpcRotationType.FIXED)

// Scale (default 1.0, usually 0.5 to 2.0)
npc.setScale(1.5)

// Equipment (armor, hand items, etc.)
npc.setEquipment(EquipmentSlot.HAND, ItemType.DIAMOND_SWORD.createItemStack())
npc.setEquipment(EquipmentSlot.HEAD, ItemType.IRON_HELMET.createItemStack())

// Display name (Adventure Component)
npc.setDisplayName(Component.text("New Name"))
```

## Properties (custom per-NPC data)

Properties are arbitrary key-value pairs with typed values. Built-in types: STRING, BOOLEAN, INT, LONG, FLOAT, DOUBLE, COMPONENT, LOCATION, UUID, NAMED_TEXT_COLOR, SKIN_DATA, ROTATION_TYPE, NPC_POSE.

```kotlin
// Add properties (DSL)
npc {
    // ...
    npcProperty {
        key = "merchant_type"
        value = "armor_dealer"
        type = NpcPropertyType.Types.STRING_TYPE
    }
}

// Or via API
npc.addProperty(NpcProperty("merchant_type", "armor_dealer", NpcPropertyType.Types.STRING_TYPE))
npc.addProperties(
    Triple("level", 5, NpcPropertyType.Types.INT_TYPE),
    Triple("price_multiplier", 1.5, NpcPropertyType.Types.DOUBLE_TYPE)
)

// Read properties
val type = npc.getProperty("merchant_type")?.value
val level = npc.getPropertyValue("level", Int::class)
npc.hasProperty("merchant_type")
npc.removeProperty("merchant_type")
npc.clearProperties()
```

Persistent NPCs always carry certain internal properties: `DISPLAYNAME`, `SKIN_DATA`, `LOCATION`, `ROTATION_TYPE`, `PERSISTENCE`, `SCALE`, `POSE`.

## Viewer management (per-player visibility)

```kotlin
val viewers = objectSetOf(uuid1, uuid2)  // only these players see the NPC
val npc = npc {
    // ... with `viewers = viewers` to restrict visibility
}

// Dynamically manage viewers
npc.addViewer(player.uniqueId)
npc.removeViewer(player.uniqueId)
npc.hasViewer(player.uniqueId)
npc.clearViewers()  // hide from all
npc.retrieveViewers()  // returns the set, or all online players if viewers is null
```

## Persistence and commands

**To persist an NPC**, set `persistent = true` when creating it. The NPC is saved to `plugins/surf-npc/npcs/<uniqueName>.yml` on server shutdown and loaded on startup.

```kotlin
npc {
    uniqueName = "shopkeeper_main"
    persistent = true
    // ...
}
```

Call `npc.save()` to write it to disk immediately.

**In-game commands** (`/npc`):
- `/npc list` - list all NPCs with their IDs
- `/npc info <npc>` - show details (properties, skin, location, pose, scale, rotation)
- `/npc create <name> <type>` - create a new NPC at your location
- `/npc delete <npc>` - delete an NPC (persistent or not)
- `/npc teleport-here <npc>` - teleport NPC to you
- `/npc teleport-to <npc> <player>` - teleport NPC to a player
- `/npc refresh <npc>` - refresh the NPC for all viewers
- `/npc edit display-name <npc> <new-name>` - set display name
- `/npc edit rotation <npc> FIXED|PER_PLAYER` - change rotation type
- `/npc edit pose <npc> <pose>` - set pose
- `/npc edit skin <npc> <player-name>` - fetch and set skin by player name
- `/npc save-to-disk` - save all persistent NPCs (usually automatic)
- `/npc version` - show version info

## Gotchas

- **Local checkout is old (`version/1.21.7`).** Always read from `origin/version/26.1`.
- **Unique names must be globally unique.** Two NPCs cannot share the same `uniqueName`.
- **Poses**: Only entries marked `usable=true` work. Don't use DEPRECATED poses; they have no effect.
- **Property keys are strings.** Store them as constants (`NpcProperty.Internal.DISPLAYNAME`, etc.).
- **Thread safety (Folia/Paper).** The plugin uses mccoroutine-folia. Use `plugin.launch { }` for async code. NPC modifications are sync.
- **Skin parts** default to all parts. Specify `parts = objectSetOf(NpcSkinPart.CAPE)` to hide others.
- **`viewers` semantics**: `null` = shown to all online players (dynamic); `ObjectSet` = static list of UUIDs (offline players in the set won't see the NPC).
- **Glowing fields** in the DSL (`glowing`, `glowingColor`) exist but are **not currently applied** to the created NPC. Ignore them or file an issue.
- **Persistent NPCs need `npc.save()` after edits** if you modify them at runtime. The DSL automatically persists on creation.
- **Reach by ID or unique name**: `SurfNpcApi.getNpc(123)` or `SurfNpcApi.getNpc("shopkeeper_01")`. Both work.
