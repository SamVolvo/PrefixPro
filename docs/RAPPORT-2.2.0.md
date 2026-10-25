# PrefixPro 2.2.0 — rapport en overdracht

Datum: 25 oktober 2026. Status: **draft; nog niet vrijgeven**.

De gebruiker heeft gevraagd nu te rapporteren en het probleem met de screenshotbediening in een volgende chat op te lossen.

## Repository en oplevering

- Repository: https://github.com/SamVolvo/PrefixPro
- Werkbranch: `feature/2.2.0`.
- Draft-PR: https://github.com/SamVolvo/PrefixPro/pull/1
- Geteste codecommit voor de laatste matrix: `ed55f5dee2eb8cdf7aa8c06b4e210ca7993e462b`.
- `master` is niet gewijzigd. Geen release gepubliceerd, tag aangemaakt of PR gemerged.
- De vooraf bestaande lokale broncode-/berichtenwijzigingen zijn geback-upt en geïntegreerd. Bestaande IDE-wijzigingen, dependency-reduced-pom.xml en oude Modrinth-afbeeldingen zijn buiten de commits gehouden.
- Backup op deze Windows-pc: `%LOCALAPPDATA%\PrefixPro-backups\pre-2.2.0-20261025-100731`. Bevat bronbestanden, configuratie, README, oorspronkelijke afbeeldingen en Git-diffs/indexstatus.

## Gebouwde wijzigingen

- Eén `PrefixPro-2.2.0.jar`, gecompileerd tegen Spigot 1.20.6 met Java `--release 21`. Alle classbestanden hebben major version 65. Geschikt om op Java 21 en 25 te laden; 26.x-servers vereisen zelf Java 25.
- Versie 2.2.0 en `api-version: 1.20.6`; geen NMS/Paper-specifieke runtimeafhankelijkheid.
- Beide bestaande reloadcommando's laden instellingen en berichten veilig opnieuw. Ze roepen niet langer rechtstreeks `onDisable()` en `onEnable()` aan.
- Async chat en LuckPerms-callbacks lezen snapshots/plaatsen UUID's in wachtrijen. Speler-/scoreboard-/AFK-mutaties gebeuren op de serverthread.
- Prefixen gebruiken contextuele LuckPerms-metadata. User- en groepswijzigingen worden bijgewerkt, met behoud van AFK-suffix. Ondersteuning voor hex, meerdere opmaakcodes, resets en optionele `name-color`-metadata.
- Scoreboards worden niet vervangen. Alleen eigen teams worden beheerd. Bestaande teamleden/teams/objectives van andere plugins blijven intact; bij conflict wordt PrefixPro's nametag op die scoreboard overgeslagen.
- AFK, automatische detectie, bescherming, titel en chatberichten zijn afzonderlijk configureerbaar. Eén gedeelde timer, aanvullende activiteitsdetectie en herstel van bestaande invulnerability/collision-status.
- Schade- en pistonbescherming blijven bestaan en respecteren de nieuwe schakelaars.
- Configuratiemigraties behouden eigen/ongekende waarden, maken backups en weigeren ongeldige YAML zonder actieve instellingen te vervangen.
- Uitschakelen ruimt taken, listeners, LuckPerms-subscriptions, metrics, titels, eigen teams en tabnamen op.
- README, changelog, lokale GitHub-/Modrinth-releasenotities en GitHub Actions toegevoegd/bijgewerkt.

## Werkelijk bewezen tests

**Laatste code:** 36 regressietests geslaagd op Java 21 en Java 25 in GitHub Actions; lokaal eveneens 36/36 op Java 21. Compilatie tegen Spigot 26.3 met Java 25 en release 21 geslaagd.

De laatste, uitgebreide servermatrix heeft alle 16 Paper-combinaties doorstaan met 36 servercontroles per combinatie, inclusief Paper 26.3. De vier Spigot-jobs lopen nog op het moment van overdracht. De werkelijk gedownloade resultaten staan in [latest-completed-matrix.json](test-results/latest-completed-matrix.json).

Run:

https://github.com/SamVolvo/PrefixPro/actions/runs/40181649812

De bewaarde momentopname staat in [latest-ci-status.json](test-results/latest-ci-status.json). Die is een momentopname, geen garantie dat later gestarte jobs inmiddels geslaagd zijn. Controleer deze run vóór vrijgave en actualiseer het rapport.

De eerdere matrix op commit `57fd30f` heeft aantoonbaar 31 servercontroles per onderstaande combinatie doorstaan:

- Paper: 1.20.6, 1.21, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.1, 26.1.2 en 26.2.
- Spigot: 1.20.6, 1.21.4 en 1.21.11.

Exacte serverbuilds, Java-versies, checksums en protocolclientversies staan in [initial-matrix.json](test-results/initial-matrix.json). Dit bestand is uit gedownloade Actions-resultaten samengesteld, niet uit het geplande matrixbestand.

Paper 26.3 build 222 heeft daarnaast lokaal 31/31 controles doorstaan op Java 25.0.4, met LuckPerms 5.5.71 en een 26.1-protocolclient via ViaVersion/ViaBackwards 5.12.1. Resultaat: `.test-servers/paper-26.3/prefixpro-result.json`. De vroege CI-poging werd geblokkeerd door de whitelist van de geïsoleerde testserver; de runner zet die nu expliciet uit, uitsluitend op loopback.

De laatste matrix voegt controles toe voor schade, dubbele listeners/taken en tabherstel. Eerdere 31/31-resultaten mogen niet worden voorgesteld als volledige validatie van alle latere toevoegingen.

## Testgrenzen

- Er zijn echte Spigot/Paper-serverprocessen en echte LuckPerms gebruikt, met twee verbonden protocolclients. Deze clients renderen geen Minecraft-beeld.
- De async-chattest injecteert een gebeurtenis in de echte Bukkit-eventbus; dat is geen fysieke toetsenbord-/chatinvoertest.
- Voor 26.2/26.3 gebruikt de matrix test-only ViaVersion/ViaBackwards. Deze horen niet bij PrefixPro's productafhankelijkheden. Native 26.3-clientweergave is niet visueel bevestigd.
- Niet elke tussenversie van Spigot is daadwerkelijk gestart; 1.21.2 ontbreekt in de Paper-downloadmatrix. De ondersteunde doelrange is breder dan de bewezen servercombinaties.
- Bukkit heeft één team per entry per scoreboard. Een vreemde teamindeling behouden betekent dat PrefixPro daar de nametag niet kan overnemen.

## Screenshotblokkade voor de volgende chat

Er zijn **geen nieuwe 2.2.0-screenshots gemaakt** en geen bestaande afbeeldingen als nieuwe testbewijzen gebruikt.

De Windows computer-use-skill is gelezen en via `mcp__node_repl__js` is `@oai/sky` geïnitialiseerd. `sky.list_windows()` gaf herhaaldelijk exact:

```text
computer-use request timed out: list_windows
```

Er is opnieuw geprobeerd na wachten, de JavaScript-kernel is gereset, `@oai/sky` is opnieuw geïmporteerd, en vensterdetectie faalde opnieuw. Er is geen bruikbare venster-/screenshotstatus verkregen. Volgens het herstelpad van de skill is de Windows-bediening gestopt. De gebruiker weet niet hoe de helper te herstellen en vraagt overdracht aan een volgende chat.

Relevante skill op deze pc:

`C:/Users/samvo/.codex/plugins/cache/openai-bundled/computer-use/26.1016.62342/skills/computer-use/SKILL.md`

Voorbereid voor hervatting:

- `scripts/prepare-client.py`: aparte officiële vanilla 1.20.6-client, libraries/assets met checksums; geen bestaande Minecraft-profielen of accountgegevens gebruikt.
- Clientbestanden: `.tools/client/`; eigen game-instellingen in `.tools/client/game/`.
- `scripts/launch-client.py`: start deze aparte client op de lokale testserver.
- `scripts/gallery-server.py`: start een geïsoleerde Paper 1.20.6-galerijsessie met één protocolspeler.
- `prefixprotest gallery` in de testserverconsole zet echte LuckPerms-prefixen en een eenvoudige testlocatie klaar zodra PrefixTester en PrefixViewer aanwezig zijn.
- Consoleopdrachten voor die sessie kunnen via `.build/gallery-command.txt` worden aangeleverd. Dit is alleen voor de geïsoleerde testserver.
- De gestarte galerijsessie is met `stop` afgesloten; de gestartte losse client is gesloten. Maak bij hervatten een nieuwe sessie.
- Leg vervolgens echte chat-, tablist-, nametag- en AFK-beelden vast, beoordeel ze en voeg ze toe aan README/galerijmateriaal. Publiceer niets zonder toestemming.

## Artefact en open stappen

Lokaal artefact: `target/PrefixPro-2.2.0.jar`.

SHA-256 van de laatste lokale build:

```text
06da39bfad10385d1618f6724480329f29bce70c4c3144a8d11d74c7b5cd56bc
```

CI-JARs kunnen door buildtimestamps een andere hash hebben; alle serverjobs binnen één run gebruiken hetzelfde Java 21-artefact. Gebruik de hash van het daadwerkelijk gekozen releaseartefact.

1. Controleer de volledige laatste CI-matrix en los eventuele nieuwe fouten op; werk testresultaten en PR-body bij.
2. Herstel de computer-use-helper en maak echte screenshots.
3. Controleer definitieve release-JAR/hash en Modrinth-versielijst tegen bewezen compatibiliteit.
4. Leg de afgeronde draft-PR/releasevoorbereiding voor aan de eigenaar. Geen automatische publicatie of merge.

`target/` bevat al sinds vóór deze opdracht getrackte buildbestanden. Hun lokale gegenereerde wijzigingen zijn niet gecommit. Reset of clean de werkmap niet: bestaande IDE-/gebruikerswijzigingen zijn nog aanwezig.
