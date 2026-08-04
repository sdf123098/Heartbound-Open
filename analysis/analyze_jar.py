#!/usr/bin/env python3
"""Phase-1 static analysis of Heartbound-0.6.9+1.21.1_WIP3.jar (Fabric, MC 1.21.1)."""
import zipfile, json, collections, struct, os

JAR = r"D:\Project\Heart\Heartbound-Port\original\Heartbound-0.6.9+1.21.1_WIP3.jar"
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "jar_list.txt")

z = zipfile.ZipFile(JAR)
names = z.namelist()
total_size = sum(z.getinfo(n).file_size for n in names)
print("TOTAL ENTRIES:", len(names), "| uncompressed size: %.1f MB" % (total_size / 1e6))

with open(OUT, "w", encoding="utf-8") as f:
    for n in names:
        f.write(n + "\n")

tops = collections.Counter(n.split("/", 1)[0] if "/" in n else "(root)" for n in names)
print("\n=== TOP-LEVEL ===")
for k, v in tops.most_common():
    print(f"{k:30s} {v}")

print("\n=== fabric.mod.json ===")
data = json.loads(z.read("fabric.mod.json").decode("utf-8"))
print(json.dumps(data, indent=2, ensure_ascii=False))

configs = [n for n in names if n.endswith(".mixins.json")]
print("\n=== MIXIN CONFIGS ===")
for c in configs:
    print(f"\n--- {c} ---")
    print(z.read(c).decode("utf-8"))

refmaps = [n for n in names if ".refmap.json" in n]
print("\n=== REFMAPS ===")
for r in refmaps:
    print(f"\n--- {r} ---")
    rm = json.loads(z.read(r).decode("utf-8"))
    print("top-level keys:", list(rm.keys()))
    for k in rm:
        v = rm[k]
        if isinstance(v, dict):
            print(f"  {k}: {len(v)} entries; samples:")
            for i, (kk, vv) in enumerate(v.items()):
                if i >= 10:
                    print("    ...")
                    break
                print(f"    {kk} -> {vv}")

aws = [n for n in names if n.endswith(".accesswidener")]
print("\n=== ACCESS WIDENER ===")
for a in aws:
    print(f"--- {a} ---")
    print(z.read(a).decode("utf-8"))

jij = [n for n in names if n.startswith("META-INF/jars/")]
print("\n=== JAR-IN-JAR ===")
for j in jij:
    print(" ", j, z.getinfo(j).file_size, "bytes")

langs = [n for n in names if "/lang/" in n]
print("\n=== LANG FILES ===", len(langs))
for l in langs:
    print(" ", l)

cls = [n for n in names if n.endswith(".class")]
b = z.read(cls[0])
major = struct.unpack(">H", b[6:8])[0]
jv = {45: "1.1", 46: "1.2", 47: "1.3", 48: "1.4", 49: "1.5", 50: "1.6", 51: "1.7",
      52: "1.8", 53: "9", 54: "10", 55: "11", 56: "12", 57: "13", 58: "14", 59: "15",
      60: "16", 61: "17", 62: "18", 63: "19", 64: "20", 65: "21", 66: "22", 67: "23",
      68: "24", 69: "25"}.get(major, f"unknown({major})")
print("\nCLASS FILES:", len(cls))
print("First class:", cls[0], "| class file major:", major, "=> Java", jv)

dep_scan = {
    b"software/bernie/geckolib": "GeckoLib",
    b"net/tslat/smartbrainlib": "SmartBrainLib",
    b"vazkii/patchouli": "Patchouli",
    b"me/shedaniel/clothconfig2": "ClothConfig",
    b"mezz/jei": "JEI",
    b"com/terraformersmc/modmenu": "ModMenu",
    b"dev/architectury": "Architectury",
}
dep_usage = {k: [] for k in dep_scan}
inter = yarn = methods = fields = 0
for n in cls:
    b = z.read(n)
    inter += b.count(b"net/minecraft/class_")
    yarn += b.count(b"net/minecraft/world/entity") + b.count(b"net/minecraft/client/render")
    methods += b.count(b"method_")
    fields += b.count(b"field_")
    for tok in dep_scan:
        if tok in b:
            dep_usage[tok].append(n)

print("\n=== NAMESPACE SCAN (all classes) ===")
print("intermediary class refs (net/minecraft/class_):", inter)
print("intermediary method refs (method_):", methods)
print("intermediary field refs (field_):", fields)
print("yarn-ish refs (net/minecraft/world/entity|client/render):", yarn)

for tok, label in dep_scan.items():
    print(f"\n=== {label} usage ({len(dep_usage[tok])} classes) ===")
    for c in dep_usage[tok][:80]:
        print(" ", c)

mod_cls = [n for n in cls if not n.startswith("net/minecraft/") and not n.startswith("com/mojang/")]
pkg = collections.Counter("/".join(n.split("/")[:-1]) for n in mod_cls)
print("\n=== MOD CLASS COUNT:", len(mod_cls), "===")
for k, v in pkg.most_common(80):
    print(f"{v:5d}  {k}")

print("\n=== FULL CLASS LISTS for key packages ===")
for n in mod_cls:
    low = n.lower()
    if any(s in low for s in ["/mixin/", "/network/", "/net/", "/packet", "/config", "/compat", "/integration", "/jei", "/modmenu"]):
        print(" ", n)

print("\n=== ASSETS (top-2-level) ===")
asset_dirs = collections.Counter()
for n in names:
    if n.startswith("assets/"):
        parts = n.split("/")
        if len(parts) >= 3:
            asset_dirs[parts[1] + "/" + parts[2]] += 1
for k, v in sorted(asset_dirs.items()):
    print(f"{v:5d}  {k}")

print("\n=== DATA (top-2-level) ===")
data_dirs = collections.Counter()
for n in names:
    if n.startswith("data/"):
        parts = n.split("/")
        if len(parts) >= 3:
            data_dirs[parts[1] + "/" + parts[2]] += 1
for k, v in sorted(data_dirs.items()):
    print(f"{v:5d}  {k}")

print("\n=== ROOT + META-INF FILES ===")
for n in names:
    if "/" not in n or n.startswith("META-INF/"):
        print(" ", n, z.getinfo(n).file_size)
