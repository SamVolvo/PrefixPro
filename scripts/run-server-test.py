"""Run ONLY an isolated loopback server; no existing production configuration is read."""
import argparse, hashlib, json, pathlib, shutil, subprocess, sys, threading, time
ROOT = pathlib.Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument("folder", type=pathlib.Path)
parser.add_argument("--java", required=True)
parser.add_argument("--port", type=int, default=25580)
parser.add_argument("--client-version")
parser.add_argument("--boot-only", action="store_true")
parser.add_argument("--keep-open", action="store_true")
parser.add_argument("--accept-eula", action="store_true", help="Accept Minecraft EULA for this disposable test server")
args = parser.parse_args()
if not args.accept_eula:
    parser.error("--accept-eula is required for a new test server")
folder = args.folder.resolve()
if folder == ROOT or ROOT not in folder.parents or ".test-servers" not in folder.parts:
    parser.error("Server folder must be within this workspace's .test-servers")
folder.mkdir(parents=True, exist_ok=True)
plugins = folder / "plugins"
plugins.mkdir(exist_ok=True)
shutil.copy2(ROOT / "target/PrefixPro-2.2.0.jar", plugins)
shutil.copy2(ROOT / ".build/PrefixProTest.jar", plugins)
(folder / "eula.txt").write_text("eula=true\n")
(folder / "server.properties").write_text(f"""server-ip=127.0.0.1
server-port={args.port}
online-mode=false
enforce-secure-profile=false
spawn-protection=0
view-distance=2
simulation-distance=2
max-players=4
level-type=minecraft:flat
generator-settings={{"layers":[{{"block":"minecraft:bedrock","height":1}},{{"block":"minecraft:dirt","height":2}},{{"block":"minecraft:grass_block","height":1}}],"biome":"minecraft:plains"}}
generate-structures=false
sync-chunk-writes=false
motd=PrefixPro isolated integration server
""")
config = plugins / "PrefixPro/config.yml"
if not config.exists():
    config.parent.mkdir(parents=True, exist_ok=True)
    # Migration coverage: a real old config with custom settings must survive startup.
    config.write_text('config-version: 3\nmessages:\n  prefix: "&8[&6PrefixPro test&8] &7"\ncustom-key: preserve-me\nupdate-check:\n  enabled: false\n')
bstats = plugins / "bStats/config.yml"
bstats.parent.mkdir(parents=True, exist_ok=True)
bstats.write_text("enabled: false\n")
result_file = folder / "prefixpro-result.json"
if result_file.exists():
    # Keep previous evidence rather than overwriting it.
    shutil.move(str(result_file), str(folder / f"previous-result-{time.time_ns()}.json"))
output = []
ready = threading.Event()
complete = threading.Event()
log = (folder / "test-console.log").open("w", encoding="utf-8")
command = [args.java, "-Xms128M", "-Xmx768M", "-XX:ActiveProcessorCount=2",
           "-jar", "server.jar", "--nogui"]
server = subprocess.Popen(command, cwd=folder, stdin=subprocess.PIPE,
                          stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace")
def read():
    for line in server.stdout:
        log.write(line); log.flush(); output.append(line)
        if 'Done (' in line: ready.set()
        if "PREFIXPRO_TEST_COMPLETE" in line: complete.set()
threading.Thread(target=read, daemon=True).start()
bots = None
try:
    if not ready.wait(600):
        raise RuntimeError("Server startup timed out\n" + "".join(output[-30:]))
    if not any("PrefixPro has been enabled!" in line for line in output):
        raise RuntimeError("PrefixPro did not enable\n" + "".join(output[-50:]))
    if args.boot_only:
        result = {"scope": "boot-only", "server_folder": folder.name,
                  "java": subprocess.check_output([args.java, "-version"], stderr=subprocess.STDOUT, text=True).splitlines()[0],
                  "enabled": True}
        result_file.write_text(json.dumps(result, indent=2) + "\n")
        print(json.dumps(result), flush=True)
    else:
        if not args.client_version: parser.error("--client-version is required for player tests")
        botlog = (folder / "clients.log").open("w", encoding="utf-8")
        bots = subprocess.Popen(["node", str(ROOT / "scripts/bots.cjs"), str(args.port), args.client_version],
                                cwd=ROOT, stdout=botlog, stderr=subprocess.STDOUT)
        deadline = time.monotonic() + 180
        while time.monotonic() < deadline:
            if sum("joined the game" in line for line in output) >= 2: break
            time.sleep(0.2)
        else: raise RuntimeError("Protocol clients did not join: " + (folder / "clients.log").read_text())
        time.sleep(2)
        server.stdin.write("prefixprotest\n"); server.stdin.flush()
        if not complete.wait(100): raise RuntimeError("Integration tests timed out\n" + "".join(output[-30:]))
        result = json.loads(result_file.read_text())
        result.update({"scope": "real-server-with-protocol-clients", "client_version": args.client_version,
                       "jar_sha256": hashlib.sha256((plugins / "PrefixPro-2.2.0.jar").read_bytes()).hexdigest()})
        result_file.write_text(json.dumps(result, indent=2) + "\n")
        print(json.dumps(result), flush=True)
        if result["failures"]: sys.exit(1)
    if args.keep_open:
        print("KEEP_OPEN: use server console input; Ctrl+C stops this local test", flush=True)
        while server.poll() is None: time.sleep(1)
finally:
    if bots is not None:
        bots.terminate()
        try: bots.wait(timeout=10)
        except subprocess.TimeoutExpired: bots.kill()
    if server.poll() is None:
        try:
            server.stdin.write("stop\n"); server.stdin.flush()
            server.wait(timeout=45)
        except (BrokenPipeError, subprocess.TimeoutExpired):
            server.terminate()
            server.wait(timeout=15)
    log.close()
