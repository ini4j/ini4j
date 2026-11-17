# Changelog

All notable changes to this project are listed here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Releases before 0.5.0 are described in
[src/changes/changes.xml](src/changes/changes.xml). Numbers like "SourceForge
#3038645" are ids from the old SourceForge tracker.

The 0.5.2 date is the date of its tag and its Maven Central upload;
`changes.xml` says 2011-12-13.

## [Unreleased]

### Security

- Fixed CVE-2022-41404, a denial of service. Option values that refer to each
  other, such as `[a] x=${b/y}` and `[b] y=${a/x}`, caused a
  `StackOverflowError`. Resolving a variable now stops after 16 levels and
  throws the new `CircularReferenceException`. The limit is the new
  `maxResolveDepth` setting, which you can change with
  `-Dorg.ini4j.config.maxResolveDepth=N` or
  `Config.getGlobal().setMaxResolveDepth(int)`. Reported on SourceForge by
  bingdian.

### Changed

- **Breaking:** ini4j now needs Java 17 or newer.
- **Breaking:** `Profile.remove(sectionName, optionName)` is now
  `Profile.removeOption(sectionName, optionName)`, because the old name clashes
  with `Map.remove` on Java 17.
- The project moved from SourceForge (SVN) to GitHub (Git), and the POM was
  modernized.
- Source code is formatted with Google Java Style.
- The Javadoc build now fails on a broken reference or tag.

### Fixed

- Replaced APIs that are deprecated in Java 17: `Class.newInstance()` and the
  `Character`, `Double` and `Float` constructors.
- Dynamic bean proxies no longer fail when the thread has no context class
  loader.
- Fixed how `BasicProfile` handles a bean property prefix together with
  `propertyFirstUpper`.
- Fixed the SCM URL in `pom.xml` so deploying to Maven Central works. The old
  `changes.xml` lists this as version 0.5.5, but that version was never tagged
  and is not on Maven Central.

### Added

- GitHub Actions workflow that runs the build, tests, Spotless, SpotBugs and
  PMD on pushes to `main` and on pull requests against `main`, except for
  changes that only touch `docs/`.
- Javadoc for the public API of `org.ini4j` and `org.ini4j.spi`.
- Dev container configuration.

## [0.5.4] - 2015-02-16

### Fixed

- Updated the release part of the build for the new Maven Central policy.

## [0.5.3] - 2015-02-13

This version is tagged but was not published to Maven Central.

### Added

- New config option `escapeKeyOnly`. When `escape` is on, `escapeKeyOnly`
  turns off escaping of values.

### Fixed

- Operators (`:` or `=`) in property and option names are now escaped by both
  the parser and the formatter.

## [0.5.2] - 2011-07-05

### Added

- Default value support in `get` and `fetch`: pass the default as the last
  argument and it is returned for a missing option (SourceForge #3047041).
- Configurable comment handling: `Config.setHeaderComment(boolean)` switches
  the header comment on or off, and `Config.setComment(boolean)` does the same
  for all comments (SourceForge #2971952).
- Byte order mark (BOM) support, as a workaround for the Java BOM and UTF-8
  problem (SourceForge #2866427).

### Changed

- The `UnicodeLittle` and `UnicodeBig` charset references were removed because
  they broke the Java 1.4 backport (SourceForge #2778337).

### Fixed

- A backslash at the end of a line joined it with the next line even when
  escaping was off. The new `escapeNewline` option turns line joining off. It
  is on by default for `Wini` (SourceForge #3038645).
- Using more than one `${...}` macro in one option value did not work
  (SourceForge #2817403).
- Fixes for the Java 1.4 backport. The tests now also run on a Java 1.4 JVM
  (SourceForge #2908623).
- Typo in the Options tutorial (SourceForge #2817399).

## [0.5.1] - 2009-04-08

### Added

- Applet support, and a small demo applet on the project site.

### Fixed

- Removed a `java.util.NavigableMap` reference that broke Java 1.5
  compatibility.

## [0.5.0] - 2009-04-02

### Added

- `Reg` class that reads and writes the Windows registry and `.reg` files. It
  has the same data model as `Ini`.
- Sections form a tree with subsections, based on the section name and a
  separator (`/` or `\`), like in `.reg` files.
- Comment support in `Ini` and `Options`: a header comment, section comments and
  option comments.
- Bean property mapping with an upper case first letter in key names, as in
  Mozilla `.ini` files (SourceForge #2706511).
- More convenient access methods in `OptionBundle` and `OptionMap`.

### Changed

- `Ini` was refactored into the `Profile` interface and the `BasicProfile`
  superclass.
- A backslash at the end of a line joins the line with the next one.
- Removed deprecated methods and classes.

[Unreleased]: https://github.com/ini4j/ini4j/compare/ini4j-0.5.4...main
[0.5.4]: https://github.com/ini4j/ini4j/compare/ini4j-0.5.3...ini4j-0.5.4
[0.5.3]: https://github.com/ini4j/ini4j/compare/ini4j-0.5.2...ini4j-0.5.3
[0.5.2]: https://github.com/ini4j/ini4j/compare/ini4j-0.5.1...ini4j-0.5.2
[0.5.1]: https://github.com/ini4j/ini4j/compare/ini4j-0.5.0...ini4j-0.5.1
[0.5.0]: https://github.com/ini4j/ini4j/releases/tag/ini4j-0.5.0
