"""Download an exact official Paper build and LuckPerms; record upstream checksums."""
import argparse, concurrent.futures, hashlib, json, pathlib, urllib.request
UA = "PrefixPro/2.2.0 (https://github.com/SamVolvo/PrefixPro)"
def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": UA}), timeout=120) as r:
        return r.read()
def download(url, path, digest=None, algorithm="sha256"):
    if not path.exists() or digest and hashlib.new(algorithm, path.read_bytes()).hexdigest() != digest:
        data = get(url)
        if digest and hashlib.new(algorithm, data).hexdigest() != digest:
            raise RuntimeError("Checksum mismatch: " + url)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
def provision(version, root):
    builds = json.loads(get(f"https://fill.papermc.io/v3/projects/paper/versions/{version}/builds"))
    build = next((b for b in builds if b["channel"] == "STABLE"), builds[0])
    artifact = build["downloads"]["server:default"]
    folder = root / ("paper-" + version)
    download(artifact["url"], folder / "server.jar", artifact["checksums"]["sha256"])
    lp = json.loads(get('https://api.modrinth.com/v2/project/luckperms/version?loaders=%5B%22bukkit%22%5D'))[0]
    lpfile = next(f for f in lp["files"] if f["primary"])
    download(lpfile["url"], folder / "plugins" / lpfile["filename"], lpfile["hashes"]["sha512"], "sha512")
    metadata = {"server": "Paper", "minecraft": version, "build": build["id"],
                "channel": build["channel"], "url": artifact["url"],
                "sha256": artifact["checksums"]["sha256"], "luckperms": lp["version_number"]}
    (folder / "download.json").write_text(json.dumps(metadata, indent=2) + "\n")
    print(json.dumps(metadata), flush=True)
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("versions", nargs="+")
    parser.add_argument("--root", type=pathlib.Path, default=pathlib.Path(".test-servers"))
    args = parser.parse_args()
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        list(pool.map(lambda v: provision(v, args.root), args.versions))
