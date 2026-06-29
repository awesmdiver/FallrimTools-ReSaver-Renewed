# Building ReSaver from Source

## Requirements

| Tool | Minimum | Recommended |
|------|---------|-------------|
| JDK  | 21      | Latest LTS  |
| Apache Maven | 3.9+ | 3.9.x |

### Supported JDK distributions

All mainstream JDK distributions at version 21 or later are supported on all platforms:

| Distribution | Download |
|---|---|
| Eclipse Temurin (Adoptium) | https://adoptium.net |
| Amazon Corretto | https://aws.amazon.com/corretto |
| Microsoft Build of OpenJDK | https://microsoft.com/openjdk |
| Azul Zulu | https://azul.com/downloads |
| Oracle JDK | https://oracle.com/java/technologies/downloads |
| GraalVM | https://graalvm.org |

> **End users do not need Java installed.** The packaged app bundles its own
> JRE via jpackage. Only developers building from source need a JDK.

Newer versions (22, 23, 24, 25 …) also work — the bytecode targets Java 21 so
any later JVM runs it without modification.

---

## Windows

### 1. Install JDK 21+

Download any distribution from the table above. Most installers will set
`JAVA_HOME` automatically. Verify:

```bat
java -version
```

### 2. Install Maven

Download from https://maven.apache.org/download.cgi, extract to a folder, and
add its `bin\` directory to your `PATH`. Verify:

```bat
mvn -version
```

### 3. Configure paths (optional)

If `JAVA_HOME` and `mvn` are already on your `PATH` you can skip this step.

**For cmd / batch:**
```bat
copy build-config.bat.template build-config.bat
```
Open `build-config.bat` and uncomment/fill in `JPACKAGE_JDK` and/or `MVN`.

**For PowerShell:**
```powershell
Copy-Item build-config.ps1.template build-config.ps1
```
Open `build-config.ps1` and uncomment/fill in `$JPACKAGE_JDK` and/or `$MVN`.

Both files are git-ignored so your local paths are never committed.

### 4. Build

**Full build** — produces `dist\ReSaver_Renewed\ReSaver_Renewed.exe`:

```bat
build-exe.bat
```

Or from PowerShell:

```powershell
.\build-exe.ps1
```

**Development run** — launch directly from `target\` without packaging:

```bat
mvn package -DskipTests
ReSaver_Renewed.bat [save-file]
```

---

## Linux

### 1. Install JDK 21+

**Temurin (recommended)** — via package manager:

```bash
# Ubuntu / Debian
sudo apt install temurin-21-jdk

# Fedora / RHEL
sudo dnf install temurin-21-jdk

# Or via SDKMAN (works on any distro):
curl -s "https://get.sdkman.io" | bash
sdk install java 21.0.7-tem
```

Set `JAVA_HOME` if your package manager doesn't do it automatically:

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-amd64   # adjust path as needed
export PATH="$JAVA_HOME/bin:$PATH"
```

Verify: `java -version`

### 2. Install Maven

```bash
# Ubuntu / Debian
sudo apt install maven

# Fedora / RHEL
sudo dnf install maven

# Or via SDKMAN:
sdk install maven
```

Verify: `mvn -version`

### 3. Configure paths (optional)

If `JAVA_HOME` and `mvn` are already on your `PATH` you can skip this step.

```bash
cp build-config.sh.template build-config.sh
```

Open `build-config.sh` and uncomment/set `JPACKAGE_JDK` and/or `MVN`.
`build-config.sh` is git-ignored.

### 4. Make scripts executable (first time only)

```bash
chmod +x build.sh ReSaver_Renewed.sh
```

### 5. Build

**Full build** — produces `dist/ReSaver_Renewed/bin/ReSaver_Renewed`:

```bash
./build.sh
```

**Development run** — launch directly from `target/` without packaging:

```bash
mvn package -DskipTests
./ReSaver_Renewed.sh [save-file]
```

### Notes for Linux users

**Save file location** — Skyrim on Linux runs via Steam + Proton. Saves are
stored inside the Proton prefix, typically at a path like:

```
~/.steam/steam/steamapps/compatdata/489830/pfx/drive_c/users/steamuser/
    My Documents/My Games/Skyrim Special Edition/Saves/
```

The app won't auto-navigate there on first launch; use the file chooser to
open a save once and ReSaver will remember the directory.

**Mod Organizer 2** — MO2 on Linux is available natively via Flatpak
(`com.modorganizer.ModOrganizer2`) or through Wine. The MO2 integration in
ReSaver requires pointing it at the MO2 ini file manually via
Options → Settings → Mod Organizer 2.

**Display scaling** — ReSaver uses FlatLaf with HiDPI support. If text appears
small on a high-DPI display, set the font scale under Options → Settings →
General → Font scaling.

---

## macOS

The process is the same as Linux. Use `build.sh` and `ReSaver_Renewed.sh`.

For jpackage on macOS, `--type app-image` produces a `dist/ReSaver_Renewed/` directory.
To produce a `.dmg` or `.pkg` change the `--type` flag in `build.sh`.

Install JDK via Homebrew: `brew install --cask temurin@21`

---

## Notes (all platforms)

### Bundle size

The build scripts use `--runtime-image` (bundles the full JDK) because many
JDK distributions no longer ship `jmods/`, which jlink requires. The resulting
bundle is ~300–350 MB. If your JDK has a `jmods/` directory you can switch to
`--jlink-options` for a much smaller image.

### JavaFX platform selection

`pom.xml` uses OS-activated Maven profiles to select the correct JavaFX native
classifier automatically (`win`, `linux`, or `mac`). No manual flag is needed —
Maven detects the build platform. You can override manually if needed:

```bash
mvn package -Djavafx.platform=linux
```

### Debug log

When "Write debug log to file" is enabled (Options → Settings → General),
log files are written to:

- **Packaged build**: same directory as the launcher executable
- **Dev launcher**: project root directory

Logs rotate: up to 5 files × 10 MB each (`debug_0.log` … `debug_4.log`).
