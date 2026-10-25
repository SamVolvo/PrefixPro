# PrefixPro 2.2.0

LuckPerms prefixes in chat, the tab list and nametags, with an optional AFK system.

## Compatibility

One universal JAR targets **Java 21** and the **Spigot 1.20.6 API**, with an intended server range of Spigot/Paper 1.20.6 through 26.3. Use Java 21 for 1.20.6/1.21 servers and Java 25 for 26.x. The same plugin bytecode runs on both runtimes; a 26.x server itself still requires Java 25.

**See [docs/TESTING.md](docs/TESTING.md) for exact builds actually tested and remaining limits.** A configured CI matrix is not proof that every job passed. Folia and server-wide hot-reload tools are outside the supported range.

[LuckPerms](https://luckperms.net/) is required. No Paper-only API, NMS, ProtocolLib or packet library is used by PrefixPro.

## Installation and updates

1. Stop the server and back up the old JAR and PrefixPro data directory.
2. Replace the old JAR with `PrefixPro-2.2.0.jar`; keep LuckPerms installed.
3. Start the server. Existing values and unknown keys are preserved. Migrations add missing defaults and create a uniquely named `.bak` file before rewriting an existing file.
4. Review `plugins/PrefixPro/config.yml` and `Customization/messages.yml`.

Invalid YAML/settings reject a reload and leave both active documents unchanged. Correct the files and retry. A JAR update requires a server restart.

## Commands and permissions

| Command | Permission | Behavior |
| --- | --- | --- |
| `/afk` | `prefixpro.afk` (default: everyone) | Toggle AFK |
| `/prefixpro config reload` | `prefixpro.admin` (default: operators) | Reload settings/messages, restore AFK, refresh displays |
| `/prefixpro plugin reload` | `prefixpro.admin` | Backward-compatible alias for the same safe reload |

Both reloads work from console. Tab completion respects permissions. Modrinth update checks log to console and can be disabled with `update-check.enabled: false`. The `prefixpro.version` join notification mentioned in older documentation was not implemented.

## Prefixes and colors

Examples:

```text
/lp group admin meta setprefix 100 "&6[Admin]"
/lp user Steve meta set name-color "&#55ccff&l"
```

Supports legacy colors/formatting, resets, `&#RRGGBB`, `#RRGGBB` and `&x&R&R&G&G&B&B`. Names inherit prefix colors and formatting unless LuckPerms `name-color` metadata overrides them. Set `display.inherit-prefix-color: false` to reset the name instead.

Context-specific metadata is respected. User/group changes refresh on the next tick, retaining AFK suffixes. Percent signs in prefixes and names cannot break chat formatting. Chat uses Bukkit's legacy AsyncPlayerChatEvent; another plugin replacing its format or event may require its own integration.

## Scoreboard coexistence

PrefixPro keeps the player's existing scoreboard and objectives. It updates only its own UUID-based teams, on the main board and current viewer boards. Foreign teams and entries are left intact.

Bukkit allows one team per entry per scoreboard. If another plugin owns an entry, PrefixPro skips that nametag on that board. Both formats cannot appear simultaneously. Viewer board/context changes reconcile once per second. Owned teams are removed on quit, display disable and shutdown. Tab names restore when still equal to PrefixPro's last applied value.

Team prefix/suffix strings are limited to 64 UTF-16 units without splitting color codes or surrogate pairs for baseline compatibility.

## AFK configuration

The full commented defaults are in [config.yml](src/main/resources/config.yml).

| Setting | Purpose |
| --- | --- |
| `afk.enabled` | Switch the entire AFK system off |
| `afk.auto-enabled`, `auto-time` | Automatic detection and timeout in seconds (default 300) |
| `afk.detect-look` | Count camera movement as activity |
| `afk.return-on-activity` | Exit on deliberate movement, chat, commands or interaction; sneak always exits |
| `afk.protection.enabled` | Switch protection off independently |
| `afk.protection.invulnerable`, `prevent-pushing` | Control the two protection flags |
| `afk.title.enabled`, `fade-in`, `stay`, `fade-out` | Title visibility and tick timings |
| `afk.chat-messages`, `afk.suffix` | Notifications and displayed suffix |

One shared timer checks inactivity every second. Protection preserves and restores previous invulnerability/collision flags on AFK exit, reload, permission loss, quit and shutdown. Passive pushing is blocked while enabled; sneak, sprint or camera input can exit AFK. Plugin teleports refresh context without counting as player activity.

Titles, subtitles, notifications, permission/command responses and join/leave text are in [messages.yml](src/main/resources/Customization/messages.yml). Empty AFK messages/titles suppress output. Hex colors are supported everywhere.

Schemas 3/1 migrate to 4/2 while retaining custom settings/messages. Listeners read current settings after reload. Existing bStats integration remains; opt out in `plugins/bStats/config.yml`.

## Build, test and release

With JDK 21 or 25 and Maven 3.9+: `mvn -B -ntp clean verify`.

The distributable is `target/PrefixPro-2.2.0.jar`. Bukkit, LuckPerms and Gson are server-provided. Unused shaded dependencies have been removed.

[GitHub Actions](.github/workflows/ci.yml) runs Java 21/25 regression tests, latest-API compilation and real-server tests using the same Java 21 artifact. `integration/` contains a test-only fixture, never a production plugin. [TESTING.md](docs/TESTING.md) distinguishes protocol clients, rendered Minecraft gameplay and boot checks.

See [CHANGELOG.md](CHANGELOG.md) and [draft release notes](release/2.2.0.md). Releases and gallery uploads require owner approval; the workflow does not publish or merge.

## Support

[GitHub Issues](https://github.com/SamVolvo/PrefixPro/issues) · [Discord](https://discord.gg/BejfuCsSUB)

Include the exact server build, Java/LuckPerms versions and relevant logs when reporting a problem.
