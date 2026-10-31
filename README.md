# Flight Enchantment — Fabric 26.3, version 1.1.0

Boots-only flight with server-owned permission, hunger exhaustion and controlled emergency landing. Install on the client and the server for multiplayer; single-player includes its own integrated server.

Movement costs 0.1 exhaustion per metre, matching sprinting. Hovering has no extra movement cost. At six hunger points (three drumsticks), flight switches to a slow vertical landing with steering and ascent disabled. Landing ends when the player reaches the ground or water. Food above six points restores flight eligibility. Damage and burning preserve active flight; lava contact still causes normal damage.

Emergency descent is vertical and does not find safe terrain. Lava directly below remains dangerous.

Build with Java 25 and Gradle 9.7.1: `./gradlew build` (Windows: `gradlew.bat build`). Requirements: Minecraft 26.3, Fabric Loader 0.19.5+, and Fabric API for 26.3. See the update bundle's verification report for validation details.

Code copyright and original MIT notice are retained in LICENSE-SOURCE.
