import argparse, os, pathlib, subprocess, zipfile
parser = argparse.ArgumentParser()
parser.add_argument("--java-home", type=pathlib.Path, default=pathlib.Path(os.environ.get("JAVA_HOME", "")))
args = parser.parse_args()
root = pathlib.Path(__file__).resolve().parents[1]
repo = pathlib.Path.home() / ".m2/repository"
spigot = sorted((repo / "org/spigotmc/spigot-api/1.20.6-R0.1-SNAPSHOT").glob("spigot-api-*-SNAPSHOT.jar"))[0]
luckperms = repo / "net/luckperms/api/5.4/api-5.4.jar"
classes = root / ".build/fixture-classes"
classes.mkdir(parents=True, exist_ok=True)
classpath = os.pathsep.join(map(str, [root / "target/classes", spigot, luckperms]))
subprocess.run([str(args.java_home / "bin/javac"), "--release", "21", "-encoding", "UTF-8",
                "-cp", classpath, "-d", str(classes), str(root / "integration/src/PrefixProTest.java")], check=True)
with zipfile.ZipFile(root / ".build/PrefixProTest.jar", "w", zipfile.ZIP_DEFLATED) as jar:
    for path in classes.rglob("*.class"):
        jar.write(path, path.relative_to(classes).as_posix())
    jar.write(root / "integration/plugin.yml", "plugin.yml")
print("Built isolated integration fixture")
