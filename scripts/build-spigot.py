"""Build an official Spigot test server from source using Spigot BuildTools."""
import argparse, hashlib, json, os, pathlib, shutil, subprocess, urllib.request
parser = argparse.ArgumentParser()
parser.add_argument("version")
parser.add_argument("--java", required=True)
args = parser.parse_args()
root = pathlib.Path(__file__).resolve().parents[1]
work = root / ".tools" / ("spigot-" + args.version)
work.mkdir(parents=True, exist_ok=True)
buildtools = root / ".tools/BuildTools.jar"
if not buildtools.exists():
    urllib.request.urlretrieve("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar", buildtools)
env = os.environ.copy()
env["JAVA_HOME"] = str(pathlib.Path(args.java).resolve().parent.parent)
if os.name == "nt": env["PATH"] = r"C:\Program Files\Git\bin;" + env["PATH"]
with (work / "build.log").open("w") as log:
    subprocess.run([args.java, "-Xmx1024M", "-XX:ActiveProcessorCount=2", "-jar", str(buildtools),
                    "--rev", args.version, "--compile", "SPIGOT"], cwd=work, env=env,
                   stdout=log, stderr=subprocess.STDOUT, check=True)
jar = work / ("spigot-" + args.version + ".jar")
if not jar.exists(): raise RuntimeError("BuildTools did not produce " + str(jar))
target = root / ".test-servers" / ("spigot-" + args.version)
target.mkdir(parents=True, exist_ok=True)
shutil.copy2(jar, target / "server.jar")
# Use the same verified LuckPerms binary as the corresponding Paper server.
plugins = target / "plugins"
plugins.mkdir(exist_ok=True)
for lp in (root / ".test-servers" / ("paper-" + args.version) / "plugins").glob("LuckPerms*.jar"):
    shutil.copy2(lp, plugins)
(target / "download.json").write_text(json.dumps({
    "server": "Spigot", "minecraft": args.version,
    "sha256": hashlib.sha256(jar.read_bytes()).hexdigest(),
    "source": "Official Spigot BuildTools --rev " + args.version
}, indent=2) + "\n")
print("Built Spigot " + args.version)
