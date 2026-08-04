#!/usr/bin/env python3
"""Remap leftover intermediary tokens in decompiled mixin sources (v5):
- $class_XXXX inner-class segments in descriptors
- method_XXXX(full-desc) strings incl. slashed descriptors
- bare method_XXXX/field_XXXX identifiers (@Shadow decls + call sites) via target-class lookup"""
import os, re, collections

ROOT = r"D:\Project\Heart\Heartbound-Port"
TINY = os.path.join(ROOT, "analysis", "tools", "mappings.tiny")
SRC = os.path.join(ROOT, "restore-1.21.1", "src", "main", "java")

cls_o2i, cls_o2n, cls_i2n = {}, {}, {}
seg_map = {}    # "$class_XXXX" (incl. $) -> named inner segment
mth = collections.defaultdict(dict)   # owner_named(dots) -> (name_inter, desc_named) -> name_named
fld = collections.defaultdict(dict)   # owner_named(dots) -> name_inter -> name_named
all_mth = {}
all_fld = collections.defaultdict(set)

def remap_desc(desc, o2x):
    out, i = [], 0
    while i < len(desc):
        if desc[i] == 'L':
            j = desc.find(';', i)
            if j == -1:
                out.append(desc[i:]); break
            name = desc[i+1:j]
            out.append('L' + o2x.get(name, name) + ';')
            i = j + 1
        else:
            out.append(desc[i]); i += 1
    return ''.join(out)

with open(TINY, encoding="utf-8") as f:
    lines = f.readlines()

for ln in lines:
    if ln.startswith("c\t"):
        parts = ln.rstrip("\n").split("\t")
        if len(parts) == 4:
            off, inter, named = parts[1], parts[2], parts[3]
            cls_o2i[off], cls_o2n[off], cls_i2n[inter] = inter, named, named
            if "$class_" in inter and "$" in named:
                seg_map["$" + inter.rsplit("$", 1)[1]] = "$" + named.rsplit("$", 1)[1]

cur = None
for ln in lines:
    if ln.startswith("c\t"):
        parts = ln.rstrip("\n").split("\t")
        if len(parts) == 4:
            cur = parts[1]
    elif ln.startswith("\tm\t"):
        parts = ln.rstrip("\n").split("\t")
        if len(parts) >= 5:
            desc_off, n_inter = parts[2], parts[4]
            n_named = parts[5] if len(parts) > 5 else n_inter
            owner_named = cls_o2n.get(cur)
            if owner_named:
                desc_named = remap_desc(desc_off, cls_o2n)
                owner = owner_named.replace("/", ".")
                mth[owner][(n_inter, desc_named)] = n_named
                all_mth[(n_inter, desc_named)] = all_mth.get((n_inter, desc_named), set()) | {n_named}
    elif ln.startswith("\tf\t"):
        parts = ln.rstrip("\n").split("\t")
        if len(parts) >= 5:
            n_inter = parts[4]
            n_named = parts[5] if len(parts) > 5 else n_inter
            owner_named = cls_o2n.get(cur)
            if owner_named:
                owner = owner_named.replace("/", ".")
                fld[owner][n_inter] = n_named
                all_fld[n_inter].add(n_named)

print(f"classes: {len(cls_i2n)}  methods: {len(all_mth)}  fields: {len(all_fld)}  inner segs: {len(seg_map)}")

mc_simple = collections.defaultdict(list)
for inter, named in cls_i2n.items():
    simple = named.rsplit("/", 1)[-1].rsplit("$", 1)[-1]
    mc_simple[simple].append(named.replace("/", "."))

mixroot = os.path.join(SRC, "com", "cuddly", "heartbound", "mixins")
files = [os.path.join(dp, fn) for dp, _, fns in os.walk(mixroot) for fn in fns if fn.endswith(".java")]
print("mixin files:", len(files))

inner_pat = re.compile(r"\$class_(\d+)")
cls_pat = re.compile(r"L?net/minecraft/class_(\d+);?")
# descriptor: params (any chars up to ')') + return type (ident chars incl. / $ ;)
mth_pat = re.compile(r"method_(\d+)(\([^)]*\)[A-Za-z0-9_/$\[\];]+)?")
fld_pat = re.compile(r"field_(\d+)(?::[A-Za-z0-9_/$\[\];]+)?")

def resolve_simple(name, imports, pkg):
    if "." in name:
        return name
    if name in imports:
        return imports[name]
    # vineflower sometimes omits the import for @Mixin(...) class refs; fall back to a
    # unique MC simple-name lookup, then to the same-package guess.
    cands = mc_simple.get(name, [])
    if len(cands) == 1:
        return cands[0]
    return pkg + "." + name

total = {"inner": 0, "class": 0, "method": 0, "field": 0, "bareshadow": 0}
for path in files:
    with open(path, encoding="utf-8") as f:
        src = f.read()
    orig = src

    pkg_m = re.search(r"^package\s+([\w.]+);", src, re.M)
    pkg = pkg_m.group(1) if pkg_m else ""
    imports = dict(re.findall(r"^import\s+([\w.]+)\.(\w+);", src, re.M))

    targets = []
    for m in re.finditer(r"@Mixin\s*\(([^)]*)\)", src, re.S):
        arg = m.group(1)
        for cm in re.finditer(r"([\w.]+)\.class", arg):
            targets.append(resolve_simple(cm.group(1), imports, pkg))
        for sm in re.finditer(r'targets\s*=\s*"([^"]+)"', arg):
            targets.append(sm.group(1).replace("/", "."))
        for vm in re.finditer(r'value\s*=\s*"([^"]+)"', arg):
            targets.append(vm.group(1).replace("/", "."))

    def repl_inner(m):
        nn = seg_map.get("$class_" + m.group(1))
        if nn:
            total["inner"] += 1
            return nn
        return m.group(0)
    src = inner_pat.sub(repl_inner, src)

    def repl_cls(m):
        named = cls_i2n.get("net/minecraft/class_" + m.group(1))
        if not named:
            return m.group(0)
        total["class"] += 1
        return ("L" if m.group(0).startswith("L") else "") + named + (";" if m.group(0).endswith(";") else "")
    src = cls_pat.sub(repl_cls, src)

    def repl_mth(m):
        inter_name = "method_" + m.group(1)
        desc = m.group(2) if m.lastindex and m.lastindex >= 2 else ""
        # exact (name, desc) lookup in target classes
        for t in targets:
            nn = mth.get(t, {}).get((inter_name, desc))
            if nn:
                total["method"] += 1
                return nn + desc
        # global exact fallback (unique across all classes)
        if desc:
            names = all_mth.get((inter_name, desc), set())
            if len(names) == 1:
                total["method"] += 1
                return next(iter(names)) + desc
        # bare-name fallback (declarations/call sites): unique within a target class,
        # then unique across all classes. Only replace when unambiguous.
        for t in targets:
            names = {nn for (mi, _), nn in mth.get(t, {}).items() if mi == inter_name}
            if len(names) == 1:
                total["bareshadow"] += 1
                return next(iter(names)) + desc
        global_names = {nn for (mi, _), nn in all_mth.items() if mi == inter_name}
        if len(global_names) == 1:
            total["bareshadow"] += 1
            return next(iter(global_names)) + desc
        return m.group(0)
    src = mth_pat.sub(repl_mth, src)

    def repl_fld(m):
        inter_name = "field_" + m.group(1)
        suffix = m.group(2) if m.lastindex and m.lastindex >= 2 else ""
        for t in targets:
            nn = fld.get(t, {}).get(inter_name)
            if nn:
                total["field"] += 1
                return nn + suffix
        names = all_fld.get(inter_name, set())
        if len(names) == 1:
            total["field"] += 1
            return next(iter(names)) + suffix
        return m.group(0)
    src = fld_pat.sub(repl_fld, src)

    if src != orig:
        with open(path, "w", encoding="utf-8", newline="") as f:
            f.write(src)

print("replacements:", total)
