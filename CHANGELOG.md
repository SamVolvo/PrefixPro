# Changelog

## 2.2.0 — unpublished release candidate

- Build one Java 21 JAR against Spigot 1.20.6; target Spigot/Paper through 26.3 on the appropriate Java runtime.
- Replace lifecycle reload with safe configuration/message reload, preserving both existing command forms.
- Queue async chat activity and LuckPerms notifications for the server thread; publish immutable chat snapshots.
- Refresh user/group/context prefixes without losing AFK suffixes.
- Preserve foreign scoreboards, teams and entries; update owned teams in place.
- Restore tab names, invulnerability/collision flags and owned titles; close subscriptions, listeners, timers and metrics.
- Preserve custom configuration and unknown keys, back up migrations and reject invalid reloads.
- Make AFK, automatic detection, protection, titles and notifications independently configurable.
- Detect deliberate activity with optional look detection and one shared timer.
- Reload AFK titles/messages, command responses and join/leave text correctly.
- Preserve hex colors and combined formatting; support LuckPerms name-color metadata and escape chat percent signs.
- Handle Paper's uppercase color round-trip when comparing/restoring displays.
- Avoid update notifications for older published releases and remove unused shaded dependencies.
- Add Java 21/25 regressions, real-server integration fixtures and GitHub Actions.

See docs/TESTING.md for completed checks and release gates. The configured matrix is not itself a passing result.
