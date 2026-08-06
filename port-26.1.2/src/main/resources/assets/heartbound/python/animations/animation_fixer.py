import os
import json
import re
from pathlib import Path

# --- Configuration ---
INPUT_DIR = r"./input"      # Folder to read from
OUTPUT_DIR = r"./output"    # Folder to write processed files
RECURSIVE = True            # Set to False if you don't want to search subfolders

# --- Helper functions ---
math_pattern = re.compile(r"^[\d\.\+\-\*/\(\)\s]+$")

def evaluate_math_string(value):
    """Evaluate simple math expressions safely."""
    try:
        if isinstance(value, str) and math_pattern.match(value):
            return round(float(eval(value)), 6)
    except Exception:
        pass
    return value

def process_json(obj):
    """Recursively evaluate all math strings in a JSON-like structure."""
    if isinstance(obj, dict):
        return {k: process_json(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [process_json(v) for v in obj]
    elif isinstance(obj, str):
        return evaluate_math_string(obj)
    else:
        return obj

def process_file(input_path, output_path):
    with open(input_path, "r", encoding="utf-8") as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError as e:
            print(f"[ERROR] Invalid JSON in {input_path}: {e}")
            return

    processed = process_json(data)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(processed, f, indent=4, ensure_ascii=False)
    print(f"[OK] Processed: {input_path} -> {output_path}")

def main():
    input_dir = Path(INPUT_DIR)
    output_dir = Path(OUTPUT_DIR)
    pattern = "**/*.animation.json" if RECURSIVE else "*.animation.json"

    files = list(input_dir.glob(pattern))
    if not files:
        print(f"[WARN] No .animation.json files found in {INPUT_DIR}")
        return

    for file_path in files:
        relative_path = file_path.relative_to(input_dir)
        output_path = output_dir / relative_path
        process_file(file_path, output_path)

if __name__ == "__main__":
    main()
