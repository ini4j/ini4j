# ini4j

[![CI](https://github.com/ini4j/ini4j/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ini4j/ini4j/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.ini4j/ini4j.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.ini4j/ini4j)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.txt)

ini4j is a Java library for reading and writing Windows `.ini` files. It also
includes an implementation of the `java.util.prefs.Preferences` API that is
backed by an `.ini` file, so you can keep application settings next to your
application instead of in a hidden per-user directory.

## Status

The latest release on Maven Central is 0.5.4. The `main` branch is
0.6.0-SNAPSHOT: it needs Java 17 and contains a fix for CVE-2022-41404 that
0.5.4 does not have (see [Security](#security)). Version 0.6.0 has not been
released yet.

## Installation

```xml
<dependency>
    <groupId>org.ini4j</groupId>
    <artifactId>ini4j</artifactId>
    <version>0.5.4</version>
</dependency>
```

To try the unreleased version, install it into your local Maven repository and
depend on `0.6.0-SNAPSHOT`. This needs JDK 17:

```sh
git clone https://github.com/ini4j/ini4j.git
cd ini4j
mvn -B clean install
```

## Quick start

Read a value from a file:

```java
import java.io.File;
import org.ini4j.Ini;

public class Example {
    public static void main(String[] args) throws Exception {
        Ini ini = new Ini(new File("config.ini"));
        int port = ini.get("server", "port", int.class);
        System.out.println("Server port: " + port);
    }
}
```

Write a file:

```java
Ini ini = new Ini();
ini.put("server", "host", "localhost");
ini.put("server", "port", 8080);
ini.store(new File("config.ini"));
```

An option value can refer to another option with `${section/option}`:

```ini
[paths]
home = /opt/app
logs = ${paths/home}/logs
```

## What it can do

- Read and write sections and options, with typed access (`int`, `boolean`,
  `File`, and so on) and multi-value options.
- Keep comments: the header comment, section comments and option comments.
- Map a section to a Java bean or interface, and back.
- Use `Options` as a replacement for `java.util.Properties`.
- Expose an `.ini` file through the standard Preferences API with
  `IniPreferences`.
- Read and write Windows `.reg` files and the Windows registry with `Reg`.

The main entry points are `Ini`, `Options`, `IniPreferences` and `Reg` in the
`org.ini4j` package.

## Security

Variable substitution follows references such as `${paths/home}`. A file with
references that point at each other used to cause a `StackOverflowError`
(CVE-2022-41404). On `main`, resolving stops after 16 levels and throws
`CircularReferenceException`. You can change the limit with
`-Dorg.ini4j.config.maxResolveDepth=N` or
`Config.getGlobal().setMaxResolveDepth(int)`.

To report a vulnerability, see [SECURITY.md](SECURITY.md).

## Building

You need JDK 17 and Maven.

```sh
mvn -B clean verify
```

This compiles, runs the tests and checks formatting with Spotless. CI also
runs SpotBugs and PMD, which you can enable with `-Pci`:

```sh
mvn -B clean verify -Pci
```

The repository has a dev container configuration in `.devcontainer` if you
prefer to work in a container.

## Roadmap

The move from SourceForge to GitHub, the Maven Standard Layout, the Java 17
build, the CI workflow and the CVE-2022-41404 fix are done. What is left for
0.6.0 is a new project site and the release to Maven Central. Progress is
tracked in [issue #1](https://github.com/ini4j/ini4j/issues/1).

## Contributing

Bug reports, ideas and pull requests are welcome. Read
[CONTRIBUTING.md](CONTRIBUTING.md) first, and note that this project follows a
[code of conduct](CODE_OF_CONDUCT.md). Changes are listed in
[CHANGELOG.md](CHANGELOG.md).

## License

ini4j is licensed under the Apache License, Version 2.0. See
[LICENSE.txt](LICENSE.txt) and [NOTICE.txt](NOTICE.txt).
