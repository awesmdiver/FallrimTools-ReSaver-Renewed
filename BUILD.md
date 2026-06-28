# Building ReSaver from Source

## Requirements

| Tool | Minimum version | Notes |
|------|----------------|-------|
| JDK  | **21** | Any distribution — see below |
| Apache Maven | 3.9+ | Must be on `PATH` or configured in `build-config.bat` |

### Supported JDK distributions (21+)

All mainstream JDK distributions at version 21 or later are supported:

| Distribution | Download |
|---|---|
| Eclipse Temurin (Adoptium) | https://adoptium.net |
| Amazon Corretto | https://aws.amazon.com/corretto |
| Microsoft Build of OpenJDK | https://microsoft.com/openjdk |
| Azul Zulu | https://azul.com/downloads |
| Oracle JDK | https://oracle.com/java/technologies/downloads |
| GraalVM | https://graalvm.org |

> **End users do not need Java.** The built `.exe` bundles its own JRE via
> jpackage, so only developers building from source need a JDK.

Newer JDK versions (22, 23, 24, 25, …) also work — the bytecode targets Java 21
so any later JVM runs it.

## Quick setup

### 1. Java

Install a JDK 21+ from any of the distributions above. Set `JAVA_HOME` to the
installation directory (most JDK installers do this automatically).

Verify: `java -version` should print `21` or higher.

### 2. Maven

Install [Apache Maven](https://maven.apache.org/download.cgi) and add its `bin\`
directory to your `PATH`.

Verify: `mvn -version` should print `3.9` or higher.

### 3. build-config.bat (optional)

If `JAVA_HOME` and `mvn` are already available in your environment you can skip
this step.

If you need to point to a specific JDK or Maven installation:

```bat
copy build-config.bat.template build-config.bat
```

Open `build-config.bat` in a text editor and uncomment / fill in the two paths.
This file is listed in `.gitignore` so your local paths are never committed.

## Building

### Full build — produces `dist\ReSaver\ReSaver.exe`

```bat
build-exe.bat
```

The script will:
1. Compile and package the JAR with Maven
2. Bundle the JDK and create a self-contained `app-image` with jpackage

### Development run — launch directly from `target\`

```bat
mvn package -DskipTests
ReSaver.bat
```

`ReSaver.bat` accepts the same arguments as the packaged exe:

```bat
ReSaver.bat path\to\save.ess
ReSaver.bat --help
ReSaver.bat --darktheme
```

## Notes

### Bundle size

`build-exe.bat` uses `--runtime-image` (bundles the full JDK) rather than a
jlink-based custom runtime image. This is because many JDK distributions no
longer ship `jmods/`, which jlink requires. The resulting bundle is ~300–350 MB.
If your JDK includes `jmods/` you can switch to jlink for a much smaller image.

### Windows only

The `.bat` build scripts and jpackage `--type app-image` target Windows.
Building on Linux or macOS would require changing the jpackage target type
(`--type deb`, `--type pkg`, etc.) and switching the JavaFX classifier in
`pom.xml` from `win` to `linux` or `mac`.

### Debug log

When "Write debug log to file" is enabled (Options → Settings → General),
log files are written to:

- **Packaged exe**: same directory as `ReSaver.exe`
- **Dev launcher**: project root directory

Logs rotate: up to 5 files × 10 MB each (`debug_0.log` … `debug_4.log`).
