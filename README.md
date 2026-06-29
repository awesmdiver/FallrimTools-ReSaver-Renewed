# FallrimTools ReSaver (Renewed)

A community-maintained, modernized fork of [FallrimTools ReSaver](https://github.com/mdfairch/FallrimTools) — the save file editor for **Skyrim** (LE, SE, VR) and **Fallout 4**.

Originally developed by **Mark Fairchild**. This fork picks up where the original left off at **6.0.643** (the last release on [Nexus Mods](https://www.nexusmods.com/skyrimspecialedition/mods/5031)) and updates the project for current and future versions of Java, modernizes the UI, and adds Linux support. The (Renewed) fork starts its own versioning at **v1.0.0**.

---

## What is ReSaver?

ReSaver lets you inspect and edit Skyrim and Fallout 4 save files. It is most commonly used to:

- **Clean orphaned scripts** left behind by removed or changed mods
- **Diagnose and repair** broken or bloated saves
- **Inspect Papyrus state** — active scripts, suspended stacks, script instances
- **Browse change forms** and plugin data inside the save
- **Compare saves** to identify differences over time

---

## What's New in (Renewed)

| Change | Details |
|--------|---------|
| **Java 21+** | Recompiled for Java 21. Works with all modern JDK distributions (Temurin, Corretto, Zulu, Microsoft, Oracle, GraalVM) on Java 21 and later |
| **Modern UI** | [FlatLaf 3.4.1](https://github.com/JFormDesigner/FlatLaf) replaces the dated Nimbus look-and-feel — HiDPI-aware, clean appearance, supports dark mode (`--darktheme`) |
| **Linux support** | Builds and runs on Linux (tested via Steam + Proton) and macOS |
| **No runtime warnings** | Replaced `lz4-pure-java` (used `sun.misc.Unsafe`) with a pure-Java LZ4 block implementation — zero JVM warnings |
| **Self-contained exe** | Windows `.exe` bundles its own JRE via jpackage — no Java installation required for end users |
| **Debug log** | Options → Settings → General → "Write debug log to file" writes a rotating log alongside the executable |
| **Generic build** | Build scripts auto-detect `JAVA_HOME` and `mvn` — no hardcoded developer paths |
| **Bug fixes** | Version string `6.0.null` fixed; duplicate Settings checkbox removed; broken `logging.properties` fixed; noisy log messages at `INFO` demoted to `FINE`; load success dialog showed file size and load time values transposed |

---

## Download

Pre-built releases for Windows are available on the [Releases](../../releases) page.

Extract the zip — this creates a `ReSaver Renewed` folder — then run `ReSaver.exe` inside it. No Java installation required.

---

## Building from Source

See **[BUILD.md](BUILD.md)** for full instructions, including Linux and macOS.

**Windows (quick start):**

```bat
build-exe.bat
```

**Linux / macOS (quick start):**

```bash
chmod +x build.sh && ./build.sh
```

**Requirements:** JDK 21+ · Maven 3.9+

---

## Usage

```
ReSaver [options] [save-file]

Options:
  -r, --reopen        Reopen the most recently opened save file
  -p, --autoparse     Automatically scan plugins on open
  -d, --darktheme     Use the dark theme
  -w, --watch         Watch save directories for new saves
  -c, --clear         Clear all stored settings
  -h, --help          Show help
  -V, --version       Show version
```

---

## License

Apache License 2.0 — same as the original. See [LICENSE.TXT](LICENSE.TXT).

Original work © 2016 Mark Fairchild.  
(Renewed) fork maintained by the community.

---

## Credits and Acknowledgments

- **[Mark Fairchild](https://github.com/mdfairch)** — original FallrimTools author
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf) — modern Swing look-and-feel (Apache 2.0)
- [picocli](https://picocli.info/) — CLI argument parsing (Apache 2.0)
- [j2html](https://j2html.com/) — HTML generation for the info panel (Apache 2.0)
- [JUniversalCharDet](https://github.com/thkoch2001/juniversalchardet) — character set detection (MPL 1.1)
- [String.png icon](https://commons.wikimedia.org/wiki/File:San_blas_kordoiak_001.jpg) — Nando Quintana, CC-BY-SA-2.0
- [Disk icon](https://commons.wikimedia.org/wiki/File:Winchester-Festplatte.jpg) — public domain
