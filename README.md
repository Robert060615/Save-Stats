# Save Stats

A console application for people who play Pokémon Red, Blue or Yellow. You give it one or more
save files and it tells you what is in them, with the goal of turning them into an HTML report full
of charts.

> **Status: unfinished.** The application currently loads and validates save files and reports
> problems with them. The charts and the HTML report (R2–R6) are not implemented yet. See
> [Requirements](#requirements) for what works.

## Vision

Players of the first Pokémon games should be able to see how strong their team is, how far their
Pokédex has come and how their game has progressed between saves – as charts, without reading
numbers off the game's menus.

## Requirements

| ID | Requirement | Status |
|---|---|---|
| R1 | The user can start the application with one or more save files and see which were loaded, with the trainer's name. | ✅ Done |
| R2 | The user can add a bar chart of the party's levels to a report. | ❌ Not done |
| R3 | The user can add a bar chart of one chosen Pokémon's stats or IVs. | ❌ Not done |
| R4 | The user can add a chart of the Pokédex progress (caught, seen, not seen of 151). | ❌ Not done |
| R5 | With several saves, the user can add line charts of Pokémon caught and badges per save. | ❌ Not done |
| R6 | The user can choose the chart color and save the report as an HTML file. | ❌ Not done |
| R7 | A missing, unreadable or invalid file gives a clear error message; the other files are still loaded. | ✅ Done |

## Running the application

Requirements: **JDK 25** and Git.

```bash
git clone https://github.com/Robert060615/app-lab3.git
cd app-lab3
./gradlew :app:run --args="../vendor/savechecker/src/test/resources/saves/Yellow/Yellow_Randomizer_02.srm"
```

On Windows, use `gradlew.bat` instead of `./gradlew`. Paths given to `--args` are relative to the
`app/` folder. Several files can be given, separated by spaces, oldest first.

Example output:

```text
Save 1: Yellow_Randomizer_02.srm (trainer YELLOW)
```

Test save files are included in `vendor/savechecker/src/test/resources/saves/`.

## Modules used

| Module | Role | How it is included |
|---|---|---|
| [svg-charts-module](https://github.com/Robert060615/svg-charts-module) (my own) | Draws the bar and line charts as SVG | Gradle dependency from JitPack, `v1.1.0` |
| [Pokemon-Red-Blue-Savechecker-Module](https://github.com/Akistolol88/Pokemon-Red-Blue-Savechecker-Module) (Akistolol88) | Reads and validates the save files | Copied in unchanged to `vendor/savechecker`, see [vendor/README.md](vendor/README.md) |

Neither module depends on this application.

## For developers

| Class | Responsibility |
|---|---|
| `App` | Entry point. Only creates the objects and starts `SaveStatsApp`. |
| `SaveStatsApp` | Runs one session: loads the save files given on the command line. |
| `ConsoleView` | Everything the user sees in the terminal. |
| `SaveLoader` | Reads a file and turns every kind of failure into one `SaveLoadException`. |
| `GameSave` | Wraps the savechecker module, so no other class depends on it. |
| `Report`, `ReportSection` | The HTML report (not yet used). |

```bash
./gradlew build   # compile, test, Checkstyle and PMD
```

## Testing

See [TEST_REPORT.md](TEST_REPORT.md).

## License

Released into the public domain under [the Unlicense](LICENSE).
