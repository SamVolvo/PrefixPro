"""Launch a separate official vanilla client against the isolated gallery server."""
import argparse, json, os, pathlib, subprocess, uuid, hashlib
root = pathlib.Path(__file__).resolve().parents[1]
dest = root / ".tools/client"
parser = argparse.ArgumentParser()
parser.add_argument("--java-home", required=True, type=pathlib.Path)
parser.add_argument("--port", type=int, default=25580)
args = parser.parse_args()
meta = json.loads((dest / "version.json").read_text())
game = dest / "game"
game.mkdir(exist_ok=True)
(game / "options.txt").write_text("""fullscreen:false
overrideWidth:1280
overrideHeight:720
guiScale:2
renderDistance:4
simulationDistance:4
maxFps:30
enableVsync:false
clouds:false
onboardAccessibility:false
skipMultiplayerWarning:true
tutorialStep:none
soundCategory_master:0.0
""")
digest = bytearray(hashlib.md5(b"OfflinePlayer:PrefixTester").digest())
digest[6] = (digest[6] & 15) | 48
digest[8] = (digest[8] & 63) | 128
player_uuid = str(uuid.UUID(bytes=bytes(digest)))
command = [str(args.java_home / "bin/javaw.exe"), "-Xms256M", "-Xmx1024M", "-XX:ActiveProcessorCount=2",
           "-Djava.library.path=" + str(dest / "natives"), "-cp",
           os.pathsep.join((dest / "classpath.txt").read_text().splitlines()), meta["mainClass"],
           "--username", "PrefixTester", "--version", meta["id"], "--gameDir", str(game),
           "--assetsDir", str(dest / "assets"), "--assetIndex", meta["assetIndex"]["id"],
           "--uuid", player_uuid, "--accessToken", "0", "--userType", "legacy",
           "--versionType", "release", "--width", "1280", "--height", "720",
           "--quickPlayMultiplayer", "127.0.0.1:" + str(args.port)]
with (dest / "client.log").open("w", encoding="utf-8") as log:
    process = subprocess.Popen(command, cwd=dest, stdout=log, stderr=subprocess.STDOUT)
print("CLIENT_PID=" + str(process.pid), flush=True)
