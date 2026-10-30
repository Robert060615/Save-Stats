# Test Report

## Summary

The application was tested in two ways: **manually** through its console interface and its web
mode, one or more test cases per requirement, and **automatically** with 54 unit tests.

### Manual tests

Done on macOS with Java 25, using the runnable jar and the save files in [samples/](samples/).
To repeat a test, build the jar and run it from the repository root:

```bash
./gradlew :app:fatJar
java -jar app/build/libs/save-stats-1.1.0.jar <files>
```

The **Input** column gives the files after `java -jar …` and then, after an arrow, the answers
typed in the menu, separated by commas. For example *`red.sav` → 1, 7* means
`java -jar app/build/libs/save-stats-1.1.0.jar samples/red.sav`, then typing `1` and `7`. All files
are in `samples/`. Tests that save a report were checked by opening `save-stats-report.html` in a
web browser.

What the sample files contain is listed in [samples/README.md](samples/README.md). The expected
values in the charts (levels, stats, IVs, counts) were read from the files with the savechecker
module.

Tests 1–20 were done on version 1.0.0. After the web mode was added in 1.1.0, tests 1, 3, 9, 15,
16 and 17 were run again on the 1.1.0 jar, since loading files and the usage text had changed. They
gave the same result, except that the usage text in test 3 now also shows how to start the web
mode.

The web mode (tests 21–32) was tested with `java -jar app/build/libs/save-stats-1.1.0.jar --web`
in Google Chrome. Chrome ran without a window, controlled by a script that chose the files, ticked
the boxes and clicked the button like a user would, then read the status code and the page that
came back. The **Input** column gives the files and the ticked charts. Two tests send a request
with `curl` instead, since the start page cannot produce it.

### Unit tests

`./gradlew :app:test` runs 54 unit tests of `ConsoleView`, `SaveLoader`, `ChartBuilder`, `Report`,
`ReportRequest` and `WebReportBuilder`. All pass. The chart tests compare each chart with one drawn
directly with svg-charts from the known values, so a wrong value or order is caught, not only a
wrong heading. The `ReportRequest` tests build form data the way the start page does, including
bytes whose Base64 contains `+`, `/` and `=`.

To check that the tests can actually fail, deliberate bugs were put into the code one at a time.
For version 1.0.0 there were 21, for example rejecting the last menu number, drawing current HP
instead of max HP, forgetting to escape headings, or storing an invalid color. Every one of them
made at least one test fail. For the web mode there were 18 more, for example not URL decoding the
form data, reversing the order of the files, leaving out the IV charts, not skipping an empty
party, or not escaping `&`. 17 made a test fail. The last one removed a check that skips empty
fields between `&` signs. No test can notice it, because an empty field does no harm without the
check either. The check had no effect, so it was taken out of the code.

### What was hard to test

- **An empty party** (R2, R3) cannot be tested manually: none of the sample files has one, and
  making one would mean editing a save file by hand. In the web mode it is covered by a unit test
  that makes such a copy of a sample save in code (party size 0, checksum computed again). In the
  terminal, the check is a single `if` in `SaveStatsApp.chooseSaveWithParty`, which has no unit
  test. It is the least tested part of the application.
- **The order of files in a real file dialog** (R8) was not tested, since the script gave Chrome
  the files directly. The order the browser hands over decides *Save 1*, *Save 2* …, and it may
  not be the order the user clicked the files in.
- **Other browsers** than Chrome were not tested with the web mode.
- **Real progress** (R5): the sample files come from different games, so the progress charts were
  tested for correct values and order, not for a game that actually progresses.
- **How the charts look** can only be judged by a person. Values and colors were checked in the SVG
  and by the unit tests; the layout was checked by eye in a web browser.
- Only macOS was tested. Windows (`gradlew.bat`, Ctrl+Z for end of input) was not.

## Test Results

| # | Test case | Req. | Input | Expected outcome | Actual outcome |
|---|---|---|---|---|---|
| 1 | Load one save | R1 | `red.sav` → 8 | `Save 1: red.sav (trainer A)`, then the menu with 8 choices | ✅ As expected. |
| 2 | Load several saves in order | R1 | `yellow-2.srm yellow-3.srm` → 8 | `Save 1: yellow-2.srm (trainer A)` and `Save 2: yellow-3.srm (trainer YELLOW)`, in that order | ✅ As expected. |
| 3 | Start without files | R1 | (no files) | A usage text with an example, and the application ends | ✅ As expected. |
| 4 | Party levels, one save | R2 | `red.sav` → 1, 7 | Not asked which save. The report has *Party levels - A* with bars for EEVEE and SQUIRTLE | ✅ As expected. |
| 5 | Party levels, choose save | R2 | `yellow-2.srm yellow-3.srm` → 1, 2, 7 | Asked *Which save?* with both file names. The report has *Party levels - YELLOW* with one bar, METAPOD | ✅ As expected. |
| 6 | Stats of a Pokémon | R3 | `yellow-3.srm` → 2, 1, 7 | Asked *Which Pokémon?* listing METAPOD. *Stats of METAPOD* with HP 22, Attack 8, Defense 13, Speed 9, Special 9 | ✅ As expected. |
| 7 | IVs of a Pokémon | R3 | `yellow-3.srm` → 3, 1, 7 | *IVs of METAPOD (0-15)* with HP 1, Attack 6, Defense 12, Speed 8, Special 13 | ✅ As expected. |
| 8 | Pokédex progress | R4 | `yellow-2.srm` → 4, 7 | *Pokédex - 5 of 151 caught* with bars Caught 5, Seen, not caught 71, Not seen 75 | ✅ As expected. |
| 9 | Progress between saves | R5 | `yellow-2.srm yellow-3.srm` → 5, 7 | Two line charts labelled *Save 1*, *Save 2*: *Pokémon caught per save* 5 → 1 and *Badges per save* 3 → 0 | ✅ As expected. |
| 10 | Progress with one save | R5 | `red.sav` → 5, 8 | `Error: Start the application with at least two save files to compare them.` and the menu again | ✅ As expected. |
| 11 | Change color | R6 | `red.sav` → 1, 6, `#e4572e`, 4, 7 | `Charts added from now on will be #e4572e.` The party chart added before is blue (`#1f77b4`), the Pokédex chart added after is orange | ✅ As expected. |
| 12 | Invalid color | R6 | `red.sav` → 6, `red`, 1, 7 | `Error: Color must be a hex color like "#f00" or "#1f77b4", got "red"`. The chart added afterwards is still blue | ✅ As expected. |
| 13 | Save the report | R6 | `red.sav` → 1, 7 | `Report saved to <full path>/save-stats-report.html`. The file opens in a web browser and shows the chart | ✅ As expected. |
| 14 | Save an empty report | R6 | `red.sav` → 7 | `The report is empty, so nothing was saved.` and no file is written | ✅ As expected. |
| 15 | File does not exist | R7 | `nope.sav` | `Error: File not found: …/nope.sav` and `Error: None of the files could be loaded.` | ✅ As expected. |
| 16 | File is not a save | R7 | `README.md` (the one in the repository root, not in `samples/`) | `Error: Not a valid save file: README.md: Expected 32768 bytes but got …` and `Error: None of the files could be loaded.` | ✅ As expected. |
| 17 | One bad file among good ones | R7 | `red.sav nope.sav` → 8 | `Save 1: red.sav (trainer A)`, then the error for `nope.sav`, then the menu | ✅ As expected. |
| 18 | Invalid menu answers | R7 | `red.sav` → `abc`, 0, 9, (empty), 8 | `Error: Please enter a number from 1 to 8.` four times, then the application quits on 8 | ✅ As expected. |
| 19 | End of input | R7 | `red.sav` → 1, Ctrl+D | `Input closed. The report was not saved.` and no stack trace | ✅ As expected. |
| 20 | Quit without saving | R6 | `red.sav` → 1, 8 | `Bye! The report was not saved.` and no file is written | ✅ As expected. |
| 21 | Start the web mode | R8 | `--web`, then open <http://localhost:8080> | `Save Stats is running at http://localhost:8080`. The start page shows the file choice, four checkboxes, a color and a button | ✅ As expected. |
| 22 | Only reachable from this computer | R8 | `--web`, then `lsof -iTCP:8080 -sTCP:LISTEN` | Listens on `127.0.0.1:8080` only | ✅ As expected. |
| 23 | Port already in use | R9 | `--web` twice | The second one prints `Error: Could not start the web server on port 8080: Address already in use` and stops | ✅ As expected. |
| 24 | One save, Pokédex | R8 | `red.sav`; Pokédex; color `#e4572e` | *Progress between saves* cannot be ticked. Status 200 and one chart, *red.sav: Pokédex - 5 of 151 caught*, in orange | ✅ As expected. |
| 25 | Two saves, all charts | R8 | `yellow-2.srm yellow-3.srm`; all four; color `#e4572e` | *Progress between saves* can be ticked. 18 charts: for `yellow-2.srm` party levels, Pokédex, then stats and IVs for EXEGGUTOR, PSYDUCK, WEEDLE, VENOMOTH, STARYU; the same for `yellow-3.srm` with METAPOD; last *Pokémon caught per save* and *Badges per save*. All orange | ✅ As expected. |
| 26 | Ten saves | R8 | Ten copies of the `yellow-*.srm` files; party levels | Status 200 and ten charts, so ten files fit within the size limit | ✅ As expected. |
| 27 | No chart ticked | R9 | `red.sav`; no charts | Status 400 and an error page: *Choose at least one chart.* with a link back to the start page | ✅ As expected. |
| 28 | File is not a save | R9 | `README.md`; Pokédex | Status 400 and *Not a valid save file: README.md: Expected 32768 bytes but got …* | ✅ As expected. |
| 29 | HTML in a file name | R9 | A text file named `<img src=x onerror=alert(1)>.sav`; Pokédex | Status 400. The file name is shown as text in the message and the page has no `img` element | ✅ As expected. |
| 30 | File too large | R9 | A 20 MB file of random bytes; Pokédex | Status 413 and *The files are too large. A save file is 32 kB, and at most ten fit at a time.* | ✅ As expected. |
| 31 | Open the report address directly | R9 | `curl http://localhost:8080/report` | Status 400 and *Choose at least one save file.* | ✅ As expected. |
| 32 | Invalid color | R9 | `curl` with `red.sav`, Pokédex and `color=red` | Status 400 and *Color must be a hex color like "#f00" or "#1f77b4", got "red"* | ✅ As expected. |

## Known Issues in Third-Party Modules

None found that affect the application. The savechecker module rejected every invalid file in
these tests with a clear message, and its values matched what the charts were expected to show.

Two things made it harder to use, without being bugs:

- It is not published as a package, so it had to be copied into this repository (`vendor/`)
  instead of being added as a dependency.
- Its README says the wording of `InvalidSaveFileException` messages may change. The application
  shows the message to the user but only depends on its own text in front of it, and the unit tests
  check only that part.
