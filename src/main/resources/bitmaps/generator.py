import os
import sys
from os import path
from pathlib import Path

out_dir = sys.argv[1]

files: list[str] = []
for file in os.listdir("./"):
    file: str = file
    if not file.endswith(".txt"): continue
    files.append(file)

for file in files:
    file: Path = Path(file)
    lines: list[str]
    with open(file, "r") as f:
        lines = f.readlines()

    keys: list[str] = []
    local_bit: int = 1
    global_bit: int = 1
    bottom_drawer: bool = False
    out: list[str] = ["{\n"]
    for i in range(len(lines)):
        line: str = lines[i]
        if len(line) == 0: continue
        split: list[str] = line.split('\t')
        if len(split) == 1:
            split.append("")
        split[0] = split[0].strip()
        split[1] = split[1].strip()

        # Counting the bit ID
        if len(split[0]) != 0:
            local_bit = int(split[0])
            if local_bit < global_bit:
                bottom_drawer = True
        if bottom_drawer:
            global_bit = local_bit + 150
        else:
            global_bit = local_bit

        name: str = split[1].strip().lower()
        if len(name) == 0: continue
        if name == "blank" or "n/a" in name: continue
        name = (
            name
            .replace(" - ", ".")
            .replace(" ", "_")
            .replace("/", "_")
            .replace("_#", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
        )
        end = (',' if i != len(lines)-1 else "")
        if name in keys:
            print(f"{file.name}: Name '{name}' has a duplicate at {local_bit} ({global_bit}). Ignoring..")
            continue
        else:
            keys.append(name)
        out.append(f"\t\"{name}\": {global_bit}{end}\n")
    out.append("}\n")

    out_path: str = path.join(out_dir, f"{file.stem}.json")
    with open(out_path, "w") as f:
        f.writelines(out)
