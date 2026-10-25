"""Local gallery session. Console input is read from .build/gallery-command.txt."""
import pathlib, subprocess, time
root = pathlib.Path(__file__).resolve().parents[1]
folder = root / ".test-servers/paper-1.20.6"
java = pathlib.Path.home() / "AppData/Local/PrefixPro-tools/jdk-21.0.12.1+1/bin/java.exe"
import shutil
shutil.copy2(root / ".build/PrefixProTest.jar", folder / "plugins/PrefixProTest.jar")
shutil.copy2(root / "target/PrefixPro-2.2.0.jar", folder / "plugins/PrefixPro-2.2.0.jar")
commands = root / ".build/gallery-command.txt"
commands.write_text("")
log = (folder / "gallery-console.log").open("w", encoding="utf-8")
server = subprocess.Popen([str(java), "-Xms128M", "-Xmx768M", "-XX:ActiveProcessorCount=2", "-jar", "server.jar", "--nogui"],
                          cwd=folder, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, text=True)
botlog = (folder / "gallery-bot.log").open("w", encoding="utf-8")
bot = None
try:
    deadline = time.monotonic() + 600
    while time.monotonic() < deadline:
        if "Done (" in (folder / "gallery-console.log").read_text(encoding="utf-8",errors="replace"): break
        if server.poll() is not None: raise RuntimeError("Gallery server exited")
        time.sleep(1)
    else: raise RuntimeError("Gallery startup timeout")
    bot = subprocess.Popen(["node", str(root / "scripts/bots.cjs"), "25580", "1.20.6", "gallery"],
                           cwd=root, stdout=botlog, stderr=subprocess.STDOUT)
    print("GALLERY_SERVER_READY 127.0.0.1:25580", flush=True)
    previous = ""
    while server.poll() is None:
        text = commands.read_text()
        if text != previous:
            previous = text
            server.stdin.write(text + "\n"); server.stdin.flush()
        time.sleep(0.5)
finally:
    if bot: bot.terminate()
    if server.poll() is None:
        server.stdin.write("stop\n"); server.stdin.flush()
        server.wait(timeout=45)
    log.close()
