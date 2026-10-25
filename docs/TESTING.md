# PrefixPro 2.2.0 validation

Local and GitHub Actions results are updated as runs finish. A configured matrix or successful compilation is not a completed player test.

## Current evidence

- Verified on 25 October 2026: [Actions run 40181649812](https://github.com/SamVolvo/PrefixPro/actions/runs/40181649812) completed successfully: **23/23 jobs**.
- Tested product-code commit: `ed55f5dee2eb8cdf7aa8c06b4e210ca7993e462b`. Later documentation commits do not change that product code.
- **36 regression tests pass on Java 21 and Java 25**. Latest Spigot 26.3 API compilation with release 21 passes.
- **All 20 real-server combinations pass 36 assertions each**: 16 Paper and 4 Spigot targets, 720 passing server assertions in total.
- Every server job used the same Java 21 JAR: SHA-256 `c6c944a28af260d6b5bf61efadbd17ce3efee08e0a41953f0d1f7a1d5ff1d36f`.
- [latest-ci-status.json](test-results/latest-ci-status.json) contains the completed job status; [latest-completed-matrix.json](test-results/latest-completed-matrix.json) contains all downloaded server results, exact builds and provenance. Assertions below were read from those result files, not inferred from the workflow matrix.
- Native game rendering is **not verified**. The isolated vanilla client connected and received gallery chat, but the current Windows helper times out during image capture after successful window discovery. No new screenshots were captured or substituted. See [the current helper diagnosis](COMPUTER-USE-DIAGNOSE-2026-10-25.md).

## Actual server matrix

| Server | Exact build | Java | Protocol client | Assertions |
| --- | --- | --- | --- | --- |
| paper-1.20.6 | 1.20.6-151-a4f0f5c (MC: 1.20.6) | 21.0.12.1 | 1.20.6 | 36/36 |
| paper-1.21 | 1.21-130-b1b5d4c (MC: 1.21) | 21.0.12.1 | 1.21.1 | 36/36 |
| paper-1.21.1 | 1.21.1-133-3cb8529 (MC: 1.21.1) | 21.0.12.1 | 1.21.1 | 36/36 |
| paper-1.21.10 | 1.21.10-130-8043efd (MC: 1.21.10) | 21.0.12.1 | 1.21.9 | 36/36 |
| paper-1.21.11 | 1.21.11-132-c5eb079 (MC: 1.21.11) | 21.0.12.1 | 1.21.11 | 36/36 |
| paper-1.21.3 | 1.21.3-83-d6c81da (MC: 1.21.3) | 21.0.12.1 | 1.21.3 | 36/36 |
| paper-1.21.4 | 1.21.4-232-12d8fe0 (MC: 1.21.4) | 21.0.12.1 | 1.21.4 | 36/36 |
| paper-1.21.5 | 1.21.5-114-a1b3058 (MC: 1.21.5) | 21.0.12.1 | 1.21.5 | 36/36 |
| paper-1.21.6 | 1.21.6-48-4d854e6 (MC: 1.21.6) | 21.0.12.1 | 1.21.6 | 36/36 |
| paper-1.21.7 | 1.21.7-32-e792779 (MC: 1.21.7) | 21.0.12.1 | 1.21.8 | 36/36 |
| paper-1.21.8 | 1.21.8-60-29c8822 (MC: 1.21.8) | 21.0.12.1 | 1.21.8 | 36/36 |
| paper-1.21.9 | 1.21.9-59-0ff899d (MC: 1.21.9) | 21.0.12.1 | 1.21.9 | 36/36 |
| paper-26.1.1 | 26.1.1-29-77c0866 (MC: 26.1.1) | 25.0.4.1 | 26.1 | 36/36 |
| paper-26.1.2 | 26.1.2-74-e4e17fc (MC: 26.1.2) | 25.0.4.1 | 26.1 | 36/36 |
| paper-26.2 | 26.2-134-3e24be0 (MC: 26.2) | 25.0.4.1 | 26.1 | 36/36 |
| paper-26.3 | 26.3-222-e3ba7b1 (MC: 26.3) | 25.0.4.1 | 26.1 | 36/36 |
| spigot-1.20.6 | 4195-b-Spigot-f6a4805-6524758 (MC: 1.20.6) | 21.0.12.1 | 1.20.6 | 36/36 |
| spigot-1.21.11 | 4598-Spigot-56165ca-d74c5d8 (MC: 1.21.11 Unobfuscated) | 21.0.12.1 | 1.21.11 | 36/36 |
| spigot-1.21.4 | 4458-Spigot-73860e0-fb3f2b6 (MC: 1.21.4) | 21.0.12.1 | 1.21.4 | 36/36 |
| spigot-26.3 | 4663-Spigot-bed90fc-c4db4c3 (MC: 26.3) | 25.0.4.1 | 26.1 | 36/36 |

The 26.2/26.3 player runs use the documented test-only protocol bridge. No native 26.3 client rendering is claimed. Spigot intermediate releases outside this table were not individually started. Earlier iteration results remain in [initial-matrix.json](test-results/initial-matrix.json), but the completed matrix above supersedes the earlier partial status.

See [the Dutch report](RAPPORT-2.2.0.md) for completed scope and remaining screenshot/release gates.

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
