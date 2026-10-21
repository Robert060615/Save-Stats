# Test Report

## Summary

The application was tested manually through its command-line interface on macOS with JDK 25.
Each test was run from the repository root with

```bash
./gradlew :app:run -q --args="<files>"
```

where `<files>` is given in the Input column. Paths are relative to the `app/` folder, and the test
save files come from the savechecker module (`vendor/savechecker/src/test/resources/saves/`,
shortened to `saves/` below). The output in the terminal was compared with the expected outcome.

Requirements R2–R6 (charts and the HTML report) are not implemented yet, so their test cases fail.
The hardest part to test will be the charts, since a person has to look at the HTML report to judge
whether they are right.

## Test Results

| Test case | Requirement | Input | Expected outcome | Actual outcome |
| --------- | ----------- | ----- | ---------------- | -------------- |
| Load one valid save | R1 | `../vendor/savechecker/src/test/resources/saves/Yellow/Yellow_Randomizer_02.srm` | `Save 1: Yellow_Randomizer_02.srm (trainer YELLOW)` | ✅ As expected. |
| Load two valid saves | R1 | `saves/Yellow/Yellow_Randomizer_01.srm` and `saves/Yellow/Yellow_Randomizer_02.srm` | Two lines, `Save 1: …_01.srm (trainer A)` and `Save 2: …_02.srm (trainer YELLOW)` | ✅ As expected. |
| No arguments | R1 | (nothing) | A usage text that shows how to start the application | ✅ As expected. |
| File does not exist | R7 | `nope.sav` | `Error: File not found: nope.sav` | ✅ As expected. |
| File is not a save | R7 | `../README.md` | `Error: Not a valid save file: README.md: Expected 32768 bytes but got …` | ✅ As expected. |
| One bad file among good ones | R7 | `saves/Yellow/Yellow_Randomizer_02.srm` and `nope.sav` | The valid save is loaded, then an error for `nope.sav` | ✅ As expected. |
| No file can be loaded | R7 | `nope.sav` | The error for the file, then `Error: None of the files could be loaded.` | ✅ As expected. |
| Party levels chart | R2 | A valid save, then choose the party levels chart | A bar chart with one bar per party Pokémon | ❌ Not implemented. |
| Stats and IVs of a Pokémon | R3 | A valid save, then choose a Pokémon | Bar charts of its five stats and five IVs | ❌ Not implemented. |
| Pokédex progress | R4 | A valid save | A bar chart of caught, seen and not seen | ❌ Not implemented. |
| Progress between saves | R5 | Two valid saves | Line charts of Pokémon caught and badges per save | ❌ Not implemented. |
| Color and saved report | R6 | Choose the color `#e4572e` and save | An HTML file with charts in that color | ❌ Not implemented. |

## Known Issues in Third-Party Modules

None found. The savechecker module was easy to use: `new SaveFile(bytes)` either gives a valid
save or throws `InvalidSaveFileException` with a clear message, which the application shows to the
user.
