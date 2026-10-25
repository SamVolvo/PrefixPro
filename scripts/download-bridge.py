"""Test-only protocol translation for newer servers than the protocol client supports."""
import argparse, pathlib
from importlib.machinery import SourceFileLoader
download = SourceFileLoader("paper_download", str(pathlib.Path(__file__).with_name("download-paper.py"))).load_module().download
parser = argparse.ArgumentParser()
parser.add_argument("folder", type=pathlib.Path)
args = parser.parse_args()
artifacts = [
    ("ViaVersion", "b8033807e7182c35003a45962352dd642c1e91880ca281da3a43cb6f481a95a2"),
    ("ViaBackwards", "29d94d3a26c85c8a327574b2322db4078992ae457f0173325431a12aac384911"),
]
for name, checksum in artifacts:
    download(f"https://github.com/ViaVersion/{name}/releases/download/5.12.1/{name}-5.12.1.jar",
             args.folder / "plugins" / f"{name}-5.12.1.jar", checksum)
print("Test-only ViaVersion/ViaBackwards 5.12.1 downloaded and verified")
