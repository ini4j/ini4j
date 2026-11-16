# Security policy

## Reporting a vulnerability

Please do not open a public issue for a security problem. Use GitHub's private
reporting form instead:

<https://github.com/ini4j/ini4j/security/advisories/new>

Say which version you tested, what you did, and what happened. A short `.ini`
file or a few lines of Java that reproduce the problem help most. ini4j is
maintained by one person in their spare time, so a reply may not be immediate.

## Supported versions

No released version of ini4j has the fix for CVE-2022-41404, a denial of
service caused by circular variable references such as `[a] x=${b/y}` and
`[b] y=${a/x}`. Version 0.5.4, the latest release on Maven Central, is
affected.

The fix is on the `main` branch and will ship with 0.6.0, which has not been
released yet. Until then, build from `main` to get it. There are no plans to
backport it to 0.5.x.

If you must stay on 0.5.4, keep untrusted input away from option values that
contain `${...}` references.
