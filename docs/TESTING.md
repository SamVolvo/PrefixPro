# PrefixPro 2.2.0 validation

Local and GitHub Actions results are updated as runs finish. A configured matrix or successful compilation is not a completed player test.

## Current evidence

- Java 21: 27 regression tests pass, including the Paper color round-trip correction.
- Java 25: 27 tests passed before that correction; final rerun pending.
- Paper 26.3 build 222 + LuckPerms 5.5.71 enabled successfully on Java 25.0.4 (boot-only so far).
- Initial player runs on Paper 1.20.6 build 151 and 1.21.4 build 232 exposed uppercase Paper hex serialization. The test and tab restoration/comparison logic were corrected. Those failed initial runs are not counted as passes; final player runs are in progress.

## Coverage and boundaries

JUnit/Mockito covers colors/reset/format inheritance, safe truncation, update ordering, both reload commands, permission/completion/console branches, AFK toggle/detection/disable/permission loss/restoration, migrations/unknown keys/quotes/backups/invalid YAML, and scoreboard/tab ownership.

The separate PrefixProTest plugin runs on real Bukkit servers, actual LuckPerms and two offline localhost protocol clients. It checks commands, metadata, tab/team state, AFK transitions/protection, message/config reloads, invalid YAML retention, async event handoff and disable cleanup. Protocol clients do not render frames or prove physical keyboard input. The async chat check injects an event into the real Bukkit event system.

For 26.2/26.3, the current protocol library supports 26.1. Test-only ViaVersion/ViaBackwards 5.12.1 bridges to the actual newer server. Results must disclose that boundary. These plugins are not PrefixPro dependencies. Native 26.3 client rendering requires a separate visual check.

## Reproduce

Use disposable servers only. The runner binds to 127.0.0.1; offline mode is for deterministic local clients. No existing server/world is read or changed.

```sh
mvn -B -ntp clean verify
python scripts/build-fixture.py
npm ci --prefix scripts --ignore-scripts --no-audit --no-fund
python scripts/download-paper.py 1.20.6
python scripts/run-server-test.py .test-servers/paper-1.20.6 --java /path/to/jdk21/bin/java --client-version 1.20.6 --accept-eula
```

Read the [Minecraft EULA](https://aka.ms/MinecraftEULA) before passing --accept-eula. Results, logs, exact builds and checksums remain in .test-servers.

```sh
python scripts/download-paper.py 26.3
python scripts/download-bridge.py .test-servers/paper-26.3
python scripts/run-server-test.py .test-servers/paper-26.3 --java /path/to/jdk25/bin/java --client-version 26.1 --accept-eula
```

For Spigot, first download its matching Paper target to obtain the verified LuckPerms binary, then run scripts/build-spigot.py VERSION --java /path/to/java and test .test-servers/spigot-VERSION. BuildTools compiles official Spigot sources; no server mirror is used.

## API references

- [Paper Java requirements](https://docs.papermc.io/paper/getting-started/)
- [Official download API](https://docs.papermc.io/misc/downloads-service/)
- [Spigot Team API](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scoreboard/Team.html)
- [Bukkit async restrictions](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/scheduler/BukkitScheduler.html)

Latest-API compilation is additional evidence. The shipped JAR is compiled against the oldest supported API with --release 21.
