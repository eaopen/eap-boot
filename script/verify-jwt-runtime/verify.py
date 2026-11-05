"""Verify JWT signers with only an application's packaged runtime dependencies."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("server_jar", type=Path)
parser.add_argument("--java-home", required=True, type=Path)
args = parser.parse_args()
source = Path(__file__).with_name("JwtPackagedRuntimeProbe.java")
with tempfile.TemporaryDirectory(prefix="eap-jwt-runtime-") as temporary:
    root = Path(temporary)
    libraries = root / "lib"
    libraries.mkdir()
    with zipfile.ZipFile(args.server_jar) as archive:
        for name in archive.namelist():
            if name.startswith("BOOT-INF/lib/") and name.endswith(".jar"):
                if any(part in name for part in ("mapstruct-processor", "junit", "mockito")):
                    raise SystemExit("Packaged classpath contains compiler/test dependencies")
                (libraries / Path(name).name).write_bytes(archive.read(name))
    jars = sorted(libraries.glob("*.jar"))
    if not jars:
        raise SystemExit("Expected a Spring Boot JAR containing BOOT-INF/lib")
    classpath = os.pathsep.join(map(str, jars))
    classes = root / "classes"
    classes.mkdir()
    subprocess.run([str(args.java_home / "bin/javac"), "-proc:none", "-cp", classpath,
                    "-d", str(classes), str(source)], check=True)
    result = subprocess.run([str(args.java_home / "bin/java"), "-cp",
                             str(classes) + os.pathsep + classpath, "JwtPackagedRuntimeProbe"],
                            capture_output=True, text=True)
    print(result.stdout, end="")
    print(result.stderr, end="")
    if result.returncode or " ERROR " in result.stdout or " ERROR " in result.stderr:
        raise SystemExit("JWT packaged-runtime verification failed")
