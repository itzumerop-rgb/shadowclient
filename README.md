# Shadow Client — Minecraft 1.21.11 (Fabric, Java 21)
Created by **ItzZShad0w_**. Client-side only. Black & red PvP-style client: Mod Menu, HUD editor,
24 modules, performance tweaks, JSON config.

## STATUS — read this first
This source was written **without access to Fabric/Mojang servers**, so it has **never been compiled against
Minecraft 1.21.11 or run in-game**. It passes a syntax-level check only (all remaining javac errors were
"Minecraft/Fabric classes not found"). Expect a few compile errors from 1.21.11 API differences on the first
build; they are small and concentrated (see "Where API drift would hit").

## Build
1. Install JDK 21.
2. Open https://fabricmc.net/develop , choose **1.21.11**, and copy the versions into `gradle.properties`
   (`yarn_mappings`, `loader_version`, `fabric_version`) and the Loom version in `build.gradle`.
   The values in this repo are best guesses and MUST be verified.
3. Generate the wrapper once (needs Gradle installed):  `gradle wrapper --gradle-version 9.2.0`
   (or copy `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` from the Fabric example mod).
4. `./gradlew build`  → JAR in `build/libs/shadowclient-1.0.0.jar` (use the one without `-sources`).
5. Drop the JAR + Fabric API into `.minecraft/mods`.

## Use
- **Right Shift**: Mod Menu · **Right Ctrl**: HUD Editor (both rebindable in Controls → Shadow Client)
- Left-click card button = enable/disable, gear (or right-click card) = settings, heart = favourite.
- Hold **C** zoom · hold **Left Alt** freelook · **F6** freecam (singleplayer/LAN only) · **F8** toggle HUD.
- Config: `.minecraft/config/shadowclient/config.json` (modules, HUD positions/scales, settings, keybinds, theme).

## Where API drift would hit (fix here first)
| File | Why |
|---|---|
| `gui/ShadowScreen.java` | 1.21.9+ input types (`Click`, `KeyInput`, `CharInput`) — all input funnels through here |
| `util/Compat.java` | `InputUtil.isKeyPressed(Window, int)` |
| `util/Draw.java`, `HudManager.java` | `DrawContext.getMatrices()` is a 2D `Matrix3x2fStack` (`pushMatrix/translate/scale`) |
| `KeybindManager.java` | `KeyBinding.Category.create(...)` |
| `HudManager.java` | uses `HudRenderCallback` (deprecated, still works); migrate to `HudElementRegistry` if removed |
| `mixin/*` | targets `Entity.changeLookDirection`, `Camera.update`, `KeyboardInput.tick`, `MinecraftClient.doAttack/doItemUse`. `defaultRequire:0` means a missed injection only disables that feature (Freelook/Freecam/CPS accuracy) instead of crashing. A *signature* mismatch will still error — check the log. |

## Rules followed
No cheats, no anti-cheat bypass. Freecam refuses to run on remote servers. Freelook/Zoom/Toggle Sprint are
client-visual or use vanilla sprint rules. Performance modules only change vanilla options (and restore them).

## Build without installing anything (GitHub Actions)
1. Create a new GitHub repo and upload the contents of this project (keep the `.github` folder).
2. Open the **Actions** tab → "Build Shadow Client" → run it (it also runs on every push).
3. When it finishes, download the **shadowclient-jar** artifact. Use `shadowclient-1.0.0.jar` (not `-sources`).
If it fails, open the failed step: the compiler errors are listed there. Send them to me and I'll fix them.
