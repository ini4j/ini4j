# Contributing to ini4j

Bug reports, questions, ideas and pull requests are welcome. This project follows the [Code of Conduct](CODE_OF_CONDUCT.md).

## Reporting bugs and asking questions

Use [GitHub issues](https://github.com/ini4j/ini4j/issues). The old
SourceForge trackers are no longer used. For a bug, include the ini4j version,
your Java version, a minimal `.ini` file or code sample that shows the problem,
what you expected and what happened.

If you found a security problem, do not open an issue. Follow
[SECURITY.md](SECURITY.md).

## Setting up

You need JDK 17 and Maven. The repository also has a dev container in
`.devcontainer` with both installed.

```sh
git clone https://github.com/ini4j/ini4j.git
cd ini4j
mvn -B clean verify
```

`verify` compiles the code, runs the tests, builds the Javadoc and checks
formatting. To run one test class while you work:

```sh
mvn -Dtest=IniTest test
```

CI runs `mvn -B clean verify -Pci`, which adds SpotBugs and PMD. Run that
before you open a pull request, because a failing check fails the build.

## Code style

Java code follows Google Java Style, and XML files (including `pom.xml`) are
formatted by Spotless. If the build reports a formatting problem, fix it with:

```sh
mvn spotless:apply
```

Tests use JUnit 4 and live in `src/test/java`. Add a test for every bug fix and
every change in behavior. Keep public API changes small and call them out in
your pull request.

## Pull requests

Work from the `main` branch and open the pull request against `main`. Small,
focused pull requests are easier to review than large ones.

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/),
for example `fix(java): handle empty option names` or `docs: fix typo in
README`. Common scopes are `java`, `docs`, `website` and `release`. Mark
breaking changes with `!` or a `BREAKING CHANGE:` footer. Describe why the
change is needed in the commit message and the pull request, not only what it
does.

Add a line to the `Unreleased` section of [CHANGELOG.md](CHANGELOG.md) when
your change is visible to users.

## License

ini4j is licensed under the [Apache License, Version 2.0](LICENSE.txt). By
contributing, you agree that your contribution is licensed under the same
terms.
