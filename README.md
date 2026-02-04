# NovaUHC

NovaUHC is a comprehensive Minecraft UHC (Ultra Hardcore) plugin suite developed for Spigot 1.8.8. It is structured as a multi-module project consisting of a core API and an "Ultimate" module containing additional high-level scenarios and game modes.

It offers a rich set of features including numerous scenarios, custom UI configurations, and CloudNet integration for competitive Minecraft gameplay.

## 🚀 Features

NovaUHC is packed with features designed for both casual and competitive UHC hosting.

### 🎭 Comprehensive Scenario System
Over 50 scenarios are built-in, categorized to vary gameplay:
- **Classic**: Cutclean, HasteyBoy, Timber, Rodless, NoFall, BloodDiamonds.
- **Resource & Mining**: DoubleOre, OreSwap, OreRoulette, Magnet, LuckyOre, FastMiner.
- **Combat & PvP**: NoCleanUp, BloodLust, WeakestLink, Gladiator, Vampire, BuffKiller.
- **Game Mechanics**: TimeBombe, WebCage, AcidRain, Blizzard, Fallout.
- **Special Modes (Role-based)**: DragonFall, Werewolf (LoupGarou), FireForce, and DeathNote.

### ⚙️ Advanced Game Lifecycle
Automated management of the entire game flow:
- **Lobby**: Pre-game preparation and team selection.
- **Scattering**: Automated and optimized player distribution across the map.
- **Starting**: Invulnerability period to ensure a fair start.
- **Game Phase**: Active resource gathering with automated timers.
- **PvP Phase**: Configurable timer for PvP activation.
- **Meetup / Border Phase**: Automated world border shrinking (SimpleBorder) to force player encounters.
- **Ending**: Victory detection, firework celebrations, and automated server cleanup.

### 🖥️ Interactive Management GUIs
A set of comprehensive menus allows hosts to configure the game in real-time:
- **Main Config**: Central hub for all game settings.
- **Scenario Manager**: Toggle and configure multiple scenarios simultaneously.
- **Border Control**: Adjust initial size, final size, and shrink speed.
- **Team Management**: Configure team sizes, friendly fire, and auto-filling.
- **World Settings**: Manage map parameters and accessibility.
- **Game Rules**: Toggle spectators, whitelist, and general UHC rules.

### 📊 Robust Backend & Persistence
- **MongoDB Integration**: Persistent storage for player statistics, including Wins, Kills, Deaths, and a custom Coin reward system.
- **CloudNet 4 Support**: Seamless integration for multi-server networks, providing automated server status updates and orchestration.
- **FastBoard**: High-performance scoreboard for real-time player stats and game info without flickering.

### 🌍 Smart World Management
- **Automated Generation**: Creates Overworld, Nether, and End worlds specifically for each match.
- **Performance Optimized**: Includes chunk pre-loading tasks (`ShadowLoadingChunkTask`) and world populator adjustments.
- **Biome Manipulation**: Built-in `BiomeReplacer` to ensure consistent and fair playing fields.

### 👥 Team Coordination Tools
- **Shared Team Inventory**: Access a global inventory for your team via `/ti`.
- **Team Coordination**: Built-in tools for sharing coordinates and status with teammates.
- **Spectator Mode**: Fully managed spectator system for eliminated players and staff.

## 📋 Requirements

- **Java**: JDK 8 (Targeting Spigot 1.8.8 compatibility).
- **Server**: Spigot 1.8.8 (or compatible forks like Paper).
- **Database**: MongoDB (Required for data persistence).
- **Optional**: CloudNet 4 (For automated server scaling).

## 🛠️ Installation & Setup

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    cd API
    ```

2.  **Configuration**:
    *   **Main Config**: Edit `core/src/main/resources/config.yml` to set up your MongoDB connection:
        ```yaml
        mongodb:
          connectionString: "mongodb://username:password@host:port/?authSource=admin"
          name: "novauhc"
        ```
    *   **Advanced Configs**: Explore `core/src/main/resources/api/` for detailed settings:
        - `generalconfig.yml`: Core game rules and timers.
        - `lang.yml`: Translation and message settings.
        - `worldconfig.yml`: World generation and lobby settings.
        - `menu.yml`: Customization for the interactive GUIs.

3.  **Build the plugin**:
    ```bash
    ./gradlew build
    ```
    The compiled `.jar` files will be generated in their respective module folders:
    - Core: `core/build/libs/API.jar`
    - Ultimate: `ultimate/build/libs/Ultimate.jar`

    *Note: The `jar` task in both `core/build.gradle` and `ultimate/build.gradle` is currently configured to also copy the output to `F:\plugin\Plugin`. Update this path if needed.*

## 📜 Scripts & Commands

### Gradle Tasks
- `./gradlew build`: Compiles the project and generates the plugin JAR.
- `./gradlew clean`: Deletes the build directory.
- `./gradlew jar`: Generates the JAR and copies it to the hardcoded output directory.

### In-Game Commands
| Command | Aliases | Description |
| :--- | :--- | :--- |
| `/h` | `/host` | Main host command for managing the game. |
| `/config` | `/preconfig` | Opens the game configuration menu. |
| `/teamco` | `/tc`, `/tcoo` | Team coordination tools. |
| `/teaminventory` | `/ti`, `/tinv` | Access the shared team inventory. |
| `/helpop` | | Contact host for assistance. |
| `/arena` | `/leave` | Arena management and exit. |
| `/doc` | | View project documentation/info. |
| `/discord` | | Get the official Discord link. |

## 📁 Project Structure

```
API/
├── core/                 # Core UHC engine and API
│   ├── src/main/java/net/novaproject/novauhc/
│   │   ├── ability/      # Player abilities and special powers
│   │   ├── arena/        # Arena and lobby logic
│   │   ├── cloudnet/     # CloudNet 4 integration
│   │   ├── command/      # Command registration and handling
│   │   ├── database/     # MongoDB interactions and managers
│   │   ├── listener/     # Event listeners (Player, Entity, etc.)
│   │   ├── scenario/     # Large collection of UHC scenarios
│   │   ├── task/         # Scheduled Bukkit tasks (Scatter, Timers)
│   │   ├── uhcplayer/    # Player data and session management
│   │   ├── uhcteam/      # Team logic and management
│   │   ├── ui/           # GUI menu implementations
│   │   ├── utils/        # Utility classes (NMS, Config, Items)
│   │   └── world/        # World generation and population
│   └── src/main/resources/ # Core assets and config files
├── ultimate/             # Advanced scenarios and game modes
│   ├── src/main/java/net/novaproject/ultimate/
│   │   ├── beatthesanta/ # Beat the Santa game mode
│   │   ├── fallenkigdom/ # Fallen Kingdom game mode
│   │   ├── flowerpower/  # Flower Power scenario
│   │   ├── legend/       # Legend scenario
│   │   ├── skyhigt/      # Sky High scenario
│   │   ├── taupegun/     # Taupe Gun scenario
│   │   └── ...           # Many more unique scenarios
│   └── src/main/resources/ # Ultimate module resources
└── build.gradle          # Root build configuration
```

## ⚙️ Environment Variables
- TODO: Add support for environment variables to override `config.yml` settings (e.g., `MONGO_URI`).

## 🧪 Tests
- TODO: Implement unit and integration tests (none currently detected).

## 📄 License

---
*Maintained by Lezombie3D and Sithey.*
