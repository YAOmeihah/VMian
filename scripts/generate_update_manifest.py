import argparse
import json
from pathlib import Path


def build_manifest(args: argparse.Namespace) -> dict:
    release_payload = json.loads(Path(args.release_json).read_text(encoding="utf-8"))
    return {
        "versionCode": int(args.version_code),
        "versionName": args.version_name,
        "tagName": args.tag,
        "apkUrl": args.apk_url,
        "notes": release_payload["body"].strip(),
        "publishedAt": release_payload["publishedAt"],
        "sha256": args.sha256.lower(),
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version-code", required=True)
    parser.add_argument("--version-name", required=True)
    parser.add_argument("--tag", required=True)
    parser.add_argument("--apk-url", required=True)
    parser.add_argument("--sha256", required=True)
    parser.add_argument("--release-json", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    output_path = Path(args.output)
    output_path.write_text(
        json.dumps(build_manifest(args), ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
