# Third-party modules

This folder holds modules written by other people. They are copied in **unchanged** and are built
as separate Gradle subprojects, so the application depends on them but they know nothing about the
application. Do not edit the code here; report problems to the module's own repository instead.

| Folder | Module | Source | Copied from | License |
|---|---|---|---|---|
| `savechecker/` | Pokémon Red/Blue/Yellow save file reader | [Akistolol88/Pokemon-Red-Blue-Savechecker-Module](https://github.com/Akistolol88/Pokemon-Red-Blue-Savechecker-Module) | commit `1a5de64` (version 1.1.0) | Unlicense, see `savechecker/LICENSE` |

The module is not published as a package, and its README says to copy the `savechecker` folder into
your own Gradle build, which is what is done here.
