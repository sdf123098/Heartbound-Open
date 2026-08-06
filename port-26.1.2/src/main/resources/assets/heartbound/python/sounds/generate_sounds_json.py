import os
import json
import re

MOD_ID = "pleasurecraft"
GIRLS_PATH = f"{MOD_ID}:girls"
MISC_PATH = f"{MOD_ID}:misc"

def numeric_sort_key(filename: str):
    """Sorts filenames numerically (e.g. moan2 < moan10)."""
    name, _ = os.path.splitext(filename)
    match = re.search(r'(\d+)$', name)
    return int(match.group(1)) if match else float('inf')

def generate_girl_sounds(girls_dir):
    """Scans sounds/girls/ and builds JSON entries for each girl's folders."""
    sounds_json = {}
    girl_dirs = sorted(
        [d for d in os.listdir(girls_dir) if os.path.isdir(os.path.join(girls_dir, d))]
    )

    for girl in girl_dirs:
        girl_path = os.path.join(girls_dir, girl)
        for sound_type in sorted(os.listdir(girl_path)):
            sound_type_path = os.path.join(girl_path, sound_type)
            if not os.path.isdir(sound_type_path):
                continue

            key = f"{girl}.{sound_type}"
            sound_files = sorted(
                [
                    f for f in os.listdir(sound_type_path)
                    if os.path.isfile(os.path.join(sound_type_path, f))
                ],
                key=numeric_sort_key
            )

            sound_entries = [
                {
                    "name": f"{GIRLS_PATH}/{girl}/{sound_type}/{os.path.splitext(f)[0]}",
                    "stream": False
                }
                for f in sound_files
            ]

            sounds_json[key] = {
                "subtitle": f"subtitles.{girl}.{sound_type}",
                "sounds": sound_entries
            }

    return sounds_json


def generate_misc_sounds(misc_dir):
    """Scans sounds/misc/ and builds JSON entries for each sound folder."""
    sounds_json = {}
    if not os.path.exists(misc_dir):
        return sounds_json  # skip if no misc folder

    for sound_type in sorted(os.listdir(misc_dir)):
        sound_type_path = os.path.join(misc_dir, sound_type)
        if not os.path.isdir(sound_type_path):
            continue

        key = f"misc.{sound_type}"
        sound_files = sorted(
            [
                f for f in os.listdir(sound_type_path)
                if os.path.isfile(os.path.join(sound_type_path, f))
            ],
            key=numeric_sort_key
        )

        sound_entries = [
            {
                "name": f"{MISC_PATH}/{sound_type}/{os.path.splitext(f)[0]}",
                "stream": False
            }
            for f in sound_files
        ]

        sounds_json[key] = {
            "subtitle": f"subtitles.{sound_type}",
            "sounds": sound_entries
        }

    return sounds_json


if __name__ == "__main__":
    base_dir = os.getcwd()  # expected to be sounds/
    girls_dir = os.path.join(base_dir, "girls")
    misc_dir = os.path.join(base_dir, "misc")

    print(f"📂 Scanning base directory: {base_dir}")

    combined_json = {}

    # Merge girls and misc
    if os.path.exists(girls_dir):
        print("🔎 Found girls directory — generating entries...")
        combined_json.update(generate_girl_sounds(girls_dir))

    if os.path.exists(misc_dir):
        print("🔎 Found misc directory — generating entries...")
        combined_json.update(generate_misc_sounds(misc_dir))

    # Save to sounds.json in base directory
    output_file = os.path.join(base_dir, "sounds.json")
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(combined_json, f, indent=2, ensure_ascii=False)

    print(f"\n✅ sounds.json generated successfully!")
    print(f"📁 Output: {output_file}")
    print(f"🔢 Total entries: {len(combined_json)}")
