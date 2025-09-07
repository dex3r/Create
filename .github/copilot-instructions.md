# Create Fabric Mod

Create is a Minecraft mod for Fabric and Quilt that offers tools and blocks for Building, Decoration, and Aesthetic Automation. It's a port of the popular Forge mod to the Fabric ecosystem, built with Java and Gradle.

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Prerequisites and Setup
- Install Java 17 (OpenJDK 17 recommended): `apt-get update && apt-get install -y openjdk-17-jdk`
- Verify Java version: `java -version` (should show Java 17)
- Ensure network access to maven.fabricmc.net (may be blocked in some environments)

### Building the Project
- **CRITICAL**: NEVER CANCEL builds - they may take 10-20 minutes depending on network and system performance
- Clone and build: 
  ```bash
  git clone https://github.com/Fabricators-of-Create/Create.git
  cd Create
  chmod +x gradlew
  ./gradlew build --no-daemon
  ```
- **Build timeout**: Set timeout to 60+ minutes. Build typically takes 10-20 minutes on first run
- **Network dependency**: Build requires access to maven.fabricmc.net for fabric-loom plugin
- **Known limitation**: If maven.fabricmc.net is blocked, build will fail with "Plugin [id: 'fabric-loom'] was not found"

### Development Environment
- **Client development**: `./gradlew runClient --no-daemon` (timeout: 30+ minutes)
- **Server development**: `./gradlew runServer --no-daemon` (timeout: 30+ minutes) 
- **Data generation**: `./gradlew runDatagen --no-daemon` (timeout: 30+ minutes)
- Run configurations are pre-configured in the `run/` directory for client, server, and gametest

### Testing
- **Unit tests**: `./gradlew test --no-daemon` (timeout: 30+ minutes)
- **Game tests**: Currently disabled in CI but available via `./gradlew runGametestServer --no-daemon`
- **NEVER CANCEL**: Test suites can take 15-30 minutes to complete

## Validation Requirements

### Pre-commit Validation
- **Always run these commands before committing changes:**
  - `./gradlew compileJava` - Verify code compiles (timeout: 20+ minutes)
  - Check for style compliance with existing .editorconfig settings

### Manual Testing Scenarios
- **Mod loading**: Ensure the mod loads without crashes in development environment
- **Basic functionality**: Test core Create mechanics (contraptions, kinetics, etc.)
- **Client-server compatibility**: Verify mod works in both single-player and multiplayer
- **Recipe integration**: Test JEI/REI/EMI integration if modified

### CI/CD Validation
- GitHub Actions workflow (`.github/workflows/build.yml`) must pass
- Jenkins build pipeline (configured for Java 21 in production) must pass
- Check artifact generation in `build/libs/` after successful build

## Common Tasks

### Development Dependencies
The project uses these key dependencies (versions in `build.gradle.kts`):
- Minecraft 1.21.4
- Fabric Loader 0.16.14  
- Fabric API 0.128.2+1.21.5
- Java 17 (required)
- Gradle 8.12.1 (via wrapper)

### Key Project Structure
```
Create/
├── src/main/java/           # Java source code
├── src/main/resources/      # Mod resources (textures, models, etc.)
├── src/generated/resources/ # Generated data (recipes, loot tables, etc.)
├── build.gradle.kts         # Build configuration
├── gradle.properties        # Gradle settings (3GB JVM heap)
├── run/                     # Development environment configs
├── scripts/                 # Utility scripts for porting
└── .github/workflows/       # CI/CD configurations
```

### Porting and Compatibility
- This is a Fabric port of the Forge version of Create
- Use `scripts/convert.sh` to convert Forge annotations to Fabric
- Use `scripts/revert.sh` to revert back to Forge annotations
- Keep changes minimal to maintain upstream compatibility
- Utilize Porting Lib for Forge→Fabric API bridges

### IDE Configuration
- Project includes `.editorconfig` with code style settings
- IntelliJ IDEA configurations in `.idea/` directory
- Use tab indentation for Java files, spaces for other files
- Import organization follows specific pattern (see .editorconfig)

## Build Troubleshooting

### Common Issues
1. **Plugin not found error**: Network access to maven.fabricmc.net is required
2. **Out of memory**: Increase JVM heap in gradle.properties (already set to 3GB)
3. **Long build times**: First build downloads dependencies, subsequent builds are faster
4. **Configuration cache**: Disabled due to Ponder compatibility

### Alternative Commands
If standard build fails due to network issues:
- Try with `--refresh-dependencies` flag
- Use `--offline` mode if dependencies are cached (will likely fail on fresh clone)
- Check network connectivity: `curl -v https://maven.fabricmc.net/`

## Key Files Reference

### build.gradle.kts (build configuration)
Contains all dependency versions, repositories, and build tasks. Key sections:
- Plugin versions (fabric-loom, etc.)
- Minecraft and Fabric versions
- Repository definitions
- Run configurations for client/server/datagen

### fabric.mod.json (mod metadata)
Defines mod entrypoints, dependencies, and compatibility:
- Main entrypoint: `com.simibubi.create.Create`
- Client entrypoint: `com.simibubi.create.CreateClient`
- Integration with JEI, REI, EMI, ModMenu

### gradle.properties (gradle settings)
- JVM args: `-Xmx3G` (3GB heap)
- Parallel builds enabled
- Configuration cache disabled for Ponder compatibility

## Additional Notes

- **Ponder integration**: Optional submodule for in-game documentation
- **Recipe viewer support**: Compatible with JEI, REI, and EMI
- **Mod compatibility**: Check `fabric.mod.json` for known incompatible mods
- **Localization**: Translations managed via Crowdin, submit to Forge repo first
- **Development focus**: Minimize changes to maintain upstream compatibility with Forge version

Always ensure you have adequate timeout settings and never cancel long-running operations. The build process is network-intensive and may require patience, especially on first run.