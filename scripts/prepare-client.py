"""Prepare a separate vanilla client from Mojang's official manifest, without account data."""
import concurrent.futures, hashlib, json, pathlib, platform, time, urllib.request, zipfile
ROOT = pathlib.Path(__file__).resolve().parents[1]
DEST = ROOT / ".tools/client"
VERSION = "1.20.6"
def get(url):
    for attempt in range(4):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "PrefixPro-test/2.2.0"}), timeout=45) as r:
                return r.read()
        except OSError:
            if attempt == 3: raise
            time.sleep(attempt + 1)
def fetch(item):
    url, path, checksum = item
    if path.exists() and (not checksum or hashlib.sha1(path.read_bytes()).hexdigest() == checksum): return
    data = get(url)
    if checksum and hashlib.sha1(data).hexdigest() != checksum: raise RuntimeError("Checksum: " + url)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)
manifest = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
entry = next(v for v in manifest["versions"] if v["id"] == VERSION)
metadata = json.loads(get(entry["url"]))
DEST.mkdir(parents=True, exist_ok=True)
(DEST / "version.json").write_text(json.dumps(metadata, indent=2))
classpath = [DEST / "client.jar"]
downloads = [(metadata["downloads"]["client"]["url"], DEST / "client.jar", metadata["downloads"]["client"]["sha1"])]
natives = []
for library in metadata["libraries"]:
    allowed = True
    if "rules" in library:
        allowed = False
        for rule in library["rules"]:
            os_rule = rule.get("os", {})
            matches = os_rule.get("name", "windows") == "windows" and os_rule.get("arch", "x86_64") in ("x86_64", "amd64")
            if "features" in rule: matches = False
            if matches: allowed = rule["action"] == "allow"
    if not allowed: continue
    artifact = library["downloads"].get("artifact")
    if artifact:
        path = DEST / "libraries" / artifact["path"]
        downloads.append((artifact["url"], path, artifact.get("sha1")))
        classpath.append(path)
        if "natives-windows" in artifact["path"]: natives.append(path)
    native_name = library.get("natives", {}).get("windows", "").replace("${arch}", "64")
    artifact = library["downloads"].get("classifiers", {}).get(native_name)
    if artifact:
        path = DEST / "libraries" / artifact["path"]
        downloads.append((artifact["url"], path, artifact.get("sha1")))
        natives.append(path)
asset_index = json.loads(get(metadata["assetIndex"]["url"]))
index_path = DEST / "assets/indexes" / (metadata["assetIndex"]["id"] + ".json")
index_path.parent.mkdir(parents=True, exist_ok=True)
index_path.write_text(json.dumps(asset_index))
for asset in asset_index["objects"].values():
    key = asset["hash"]
    downloads.append((f"https://resources.download.minecraft.net/{key[:2]}/{key}",
                      DEST / "assets/objects" / key[:2] / key, key))
print(f"Downloading {len(downloads)} official client/library/asset files", flush=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
    list(pool.map(fetch, downloads))
native_dir = DEST / "natives"
native_dir.mkdir(exist_ok=True)
for path in natives:
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if name.endswith(".dll"):
                (native_dir / pathlib.PurePosixPath(name).name).write_bytes(archive.read(name))
(DEST / "classpath.txt").write_text("\n".join(str(p) for p in classpath))
print("Client prepared in " + str(DEST), flush=True)
