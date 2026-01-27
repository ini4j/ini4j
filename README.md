# ini4j 

[![Maven Central](https://img.shields.io/maven-central/v/org.ini4j/ini4j.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.ini4j/ini4j)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**The Java API for handling Windows .ini files.**

> 🚧 **MIGRATION IN PROGRESS**
>
> This project is currently migrating from [SourceForge](https://sourceforge.net/projects/ini4j/) to GitHub. 
> Active work is underway to modernize the codebase, build system, and documentation for the upcoming **v0.6.0** release.

---

## 🗺️ Roadmap to v0.6.0

The project is being revived to support modern Java versions (17+) and fix long-standing issues.

- [x] **Migration:** Move full SVN history to GitHub.
- [ ] **Modernization:** Refactor structure to Maven Standard Layout (`src/main/java`).
- [ ] **Build:** Modernize POM and enable GitHub Actions (Java 17 CI).
- [ ] **Security:** Fix CVE-2022-41404 (DoS vulnerability).
- [ ] **Documentation:** Launch new site at `ini4j.dev`.
- [ ] **Release:** Publish v0.6.0 to Maven Central.

*See the [Migration Issue](https://github.com/ini4j/ini4j/issues/1) for full details and discussion.*

---

## 📦 Installation

*(Note: v0.5.4 is the current stable release on Maven Central. v0.6.0 is coming soon.)*

```xml
<dependency>
    <groupId>org.ini4j</groupId>
    <artifactId>ini4j</artifactId>
    <version>0.5.4</version>
</dependency>
```

## 🚀 Quick Start

```java
import org.ini4j.Ini;
import java.io.File;

public class Example {
    public static void main(String[] args) throws Exception {
        Ini ini = new Ini(new File("config.ini"));
        int port = ini.get("server", "port", int.class);
        System.out.println("Server port: " + port);
    }
}
```

## 📜 License

Licensed under the **Apache License, Version 2.0**.
