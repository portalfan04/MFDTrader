MFDTrader
=========

Overview
- - - - -
MFDTrader is a desktop Java application (Swing) for planning and simulating interplanetary missions. It models solar systems from JSON resource files, advances a simple simulation of a universe composed of celestial bodies and organisations, and provides several GUI tools:
- A main window that renders the current solar system and ships
- A Solar System viewer
- A Transfer Planner GUI (porkchop plots / Hohmann transfers)
- Flight plan designer and related components

The application loads solar system definitions from `resources/systems/main` on startup and renders a live simulation with a Swing `Timer`.

Stack
- - - -
- Language: Java (plain Java/Swing)
- GUI: Swing (javax.swing)
- Data: JSON files in `resources/systems/main` (e.g., `sol.json`, `alpha-centauri.json`)
- Package/Build tool: none present (no Maven/Gradle files found; IntelliJ IDEA project files are included)
  - TODO: Add a build tool (Gradle or Maven) for reproducible builds

Entry point
- - - - - -
- `src/Main.java` — application entry point. It:
  - Loads solar systems via `util.loaders.DirectoryLoader.loadDefaultDirectory()`
  - Creates a `simulation.Universe` and adds loaded systems
  - Starts the Swing GUI via `GUIManager`
  - Ticks the universe using a Swing `Timer`

Requirements
- - - - - -
- Java Development Kit (JDK). Version is not pinned in the repo.
  - Recommended: JDK 8+ (Swing; lambda expressions are used)
  - TODO: Confirm exact JDK version used to develop/run this project
- IntelliJ IDEA (recommended) or any Java IDE
- OS: Windows, macOS, or Linux (paths in code use forward slashes within resources; ensure you run from the project root so relative paths resolve)

Getting started
- - - - - - - -
1) Clone the repo
```
git clone <your-fork-or-origin-url>
cd MFDTrader
```

2) Run from IDE (recommended)
- Open the project in IntelliJ IDEA
- Ensure the project SDK is set (e.g., JDK 8+)
- Mark `resources` as a Resources Folder (or keep the working directory as project root so relative paths work)
- Create a Run Configuration with Main class: `Main`
- Working directory: project root (so `resources/systems/main` can be found)
- Press Run

3) Run from command line (no build tool)
Note: This repository does not include Maven/Gradle. You can compile with `javac` and run with `java` as below. Ensure you are at the project root.

Compile sources to `out/production` (create it if missing):
```
mkdir -p out/production/MFDTrader
javac -d out/production/MFDTrader ^
  src\Main.java ^
  src\GUIManager.java ^
  src\GameLoop.java ^
  src\**\*.java
```
On Windows PowerShell, you can use:
```
New-Item -ItemType Directory -Force -Path out\production\MFDTrader | Out-Null
$srcFiles = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out\production\MFDTrader $srcFiles
```

Run (from project root):
```
# Keep working directory as project root so DirectoryLoader can find resources/systems/main
java -cp out/production/MFDTrader Main
```
If you prefer, you can also include the `resources` folder on the classpath, but the current code reads files via relative paths.

Configuration and data
- - - - - - - - - - - -
- Solar system definitions live under `resources/systems/main/` (JSON):
  - `sol.json` — Solar system with the Sun and planets
  - `alpha-centauri.json` — Another example system
- Default loader: `util.loaders.DirectoryLoader.loadDefaultDirectory()` points to `resources/systems/main`
- You can add more `.json` files to that directory. Each file becomes one `simulation.SolarSystem` (root body per file).

Scripts
- - - - -
No automation scripts are included.
- TODO: Add Gradle/Maven build scripts
- TODO: Add run scripts for Windows/Linux/macOS

Environment variables
- - - - - - - - - - -
None required.
- Optional: `JAVA_HOME` for tooling convenience

How it works (high level)
- - - - - - - - - - - -
- `simulation` package: core entities like `Universe`, `SolarSystem`, `Celestial`, orbital elements
- `util.loaders` package: JSON loading (`SolarSystemLoader`, `DirectoryLoader`)
- `flight` package: ships, flight plans, procedures, steps
- `gui` package: Swing windows/components (e.g., `SolarSystemViewer`, `TransferPlannerGUI`, `FlightPlanDesigner`)
- The main loop is a Swing `Timer` in `Main`/`GUIManager` that calls `universe.tick()` and `guiManager.render()`

Tests
- - - -
There are currently no unit tests in the repository.
- TODO: Add tests for loaders (JSON parsing), celestial mechanics utilities, and GUI-less logic

Project structure (selected)
- - - - - - - - - - - - -
```
MFDTrader/
├─ src/
│  ├─ Main.java                          # Entry point
│  ├─ GUIManager.java                    # Main Swing window
│  ├─ flight/                            # Flight simulation domain
│  │  ├─ construction/                   # Ship construction
│  │  ├─ procedure/                      # Procedures (e.g., Hohmann, Wait)
│  │  └─ step/                           # Steps used by procedures
│  ├─ gui/                               # Additional Swing GUIs
│  ├─ simulation/                        # Universe, SolarSystem, Celestial, etc.
│  └─ util/                              # Helpers and JSON loaders
├─ resources/
│  └─ systems/
│     └─ main/
│        ├─ sol.json
│        └─ alpha-centauri.json
└─ out/production/                       # Compiled classes (generated locally; may be IDE-managed)
```

Troubleshooting
- - - - - - - - -
- If the app starts but no solar systems appear, ensure you run from the project root so `resources/systems/main` is a valid relative path
- If compilation fails, confirm your JDK is installed and the project SDK is set in your IDE

License
- - - - -
No license file was found in the repository.
- TODO: Add a `LICENSE` file (e.g., MIT/Apache-2.0) and state the license here

Contributing
- - - - - - -
- Open issues/PRs with proposed changes
- Consider adding a build tool and tests first for easier collaboration
