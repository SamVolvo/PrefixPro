# Windows computer-use: diagnose op 25 oktober 2026

Status: **vensterdetectie werkt; beeldopname blijft geblokkeerd**. Geen nieuwe screenshots gemaakt.

## Uitgevoerde herstelprocedure

De geïnstalleerde skill is `computer-use/26.1021.31345/skills/computer-use/SKILL.md`. Ook de bijbehorende `guidance.md`, `api.md` en `confirmations.md` zijn gelezen. De vorige poging gebruikte versie 26.1016.62342.

1. JavaScript-kernel gereset met `mcp__node_repl__js_reset`.
2. `@oai/sky` opnieuw geïmporteerd volgens de Initialize-instructie.
3. `sky.list_windows()` slaagt en blijft slagen bij verdere vensterdetectie. De oude `list_windows`-time-out is in deze sessie niet gereproduceerd.
4. De voorbereide, afzonderlijke Paper 1.20.6-testserver met LuckPerms en de afzonderlijke vanilla 1.20.6-client gestart. De server bindt alleen op `127.0.0.1:25580`.
5. Eén exact teruggegeven Minecraft-venster geselecteerd: `Minecraft 1.20.6 - Multiplayer (3rd-party Server)`. `sky.get_window(...)` slaagt.
6. Eerste `sky.get_window_state({window: mcWindow})` faalt met:

```text
computer-use request timed out: get_window_state
```

7. Volgens het herstelpad opnieuw `list_windows`, de unieke Minecraft-window selecteren en `get_window` uitvoeren. `sky.activate_window(...)` keert terug zonder fout. Daarna dezelfde `get_window_state`-opname opnieuw geprobeerd.
8. De herhaalde opname faalt met exact dezelfde time-out. Er komt geen screenshot, toegankelijkheidsboom of native foutcode terug. Vervolgens geen verdere Windows UI-acties uitgevoerd.

De herstelregel voor deze stap luidt: “Refresh the app/window selection and retry once; report the exact error if recovery fails.” Dit staat onder Recovery in `../../docs/guidance.md`, waar de skill naar verwijst. Er is geen eigen helperprotocol gebouwd en geen helper-executable gezocht of rechtstreeks gestart.

## Wat hiermee is vastgesteld

- De oude volledige blokkade van vensterdetectie is na een schone sessie met de huidige installatie niet meer aanwezig.
- De resterende blokkade zit op de `get_window_state`-grens, nadat het Minecraft-venster gevonden en opnieuw geactiveerd is. Dit is geen bewijs dat de hele helper hersteld is.
- De serverlog bevestigt beide spelers, `GALLERY_READY` om 11:06:48 Belgische tijd en twee chatregels met LuckPerms-prefixen om 11:06:50. De clientlog bevestigt ontvangst van de chatregels.
- Dit bewijst server/client-communicatie, niet hoe tekst, tablist, nametags of AFK op het scherm renderen.
- De clientlog bevat om 11:06:37 een OpenGL-melding: `id=1280`, `GL_INVALID_ENUM error generated.` Dat valt binnen de beeldopnamepoging. **Een oorzakelijk verband met de helper is niet bewezen.**
- Er is geen bevestigde diagnose voor driverfout, ontbrekende appgoedkeuring, desktopstatus of een defecte captureservice. De API geeft alleen de time-out terug. Een wijziging van skillversie of reset mag daarom niet als bewezen oorzaak/oplossing worden beschreven.

## Officiële documentatie en grens

[OpenAI Computer Use-documentatie](https://learn.chatgpt.com/docs/computer-use) beschrijft dat Windows de actieve desktop gebruikt, het doelvenster zichtbaar moet blijven en appgoedkeuringen afzonderlijk beheerd worden via Settings > Computer use. Er is daarom ook een herhaalde poging met `activate_window` uitgevoerd. Er is geen betrouwbare beeldstatus waarmee verdere UI-bediening geverifieerd kan worden; appgoedkeuring of Windows-instellingen zijn niet gewijzigd.

De openbare documentatie bevat geen specifieke reparatie voor deze `sky.get_window_state`-time-out. Een volgende herstelstap vereist diagnostiek van de lokale computer-use-host/capturefunctie of controle van de Minecraft-appgoedkeuring door de eigenaar. De oorzaak blijft onbevestigd; die controle is een vervolgstap, geen bewezen oplossing.

## Behouden omgeving en resultaat

- Bestaande Minecraft-installaties, accountbestanden en credentials zijn niet gelezen of aangepast.
- Alleen de voorbereide bestanden onder `.tools/client` en `.test-servers/paper-1.20.6` gebruikt.
- Voor wijzigingen aan de geïsoleerde instellingen is een extra backup gemaakt onder `.build/gallery-backup-20261025-110539`.
- De afzonderlijk gestarte client is na de mislukte herstelpoging gesloten. De galerijserver is via zijn eigen console met `stop` afgesloten.
- Er zijn geen nieuwe PNG's als galerijmateriaal toegevoegd en geen beelden nagemaakt. Bestaande `modrinth/`-bestanden zijn ongemoeid gelaten.
- README, CI-rapport en Draft PR #1 zijn bijgewerkt: alle 23 Actions-jobs zijn geslaagd; screenshots blijven een vrijgavevoorwaarde.

Logs voor lokaal vervolgonderzoek: `.tools/client/client.log` en `.test-servers/paper-1.20.6/gallery-console.log`. Deze volledige logs zijn niet naar GitHub gekopieerd.
