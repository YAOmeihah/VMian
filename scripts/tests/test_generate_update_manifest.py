import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


class GenerateUpdateManifestTest(unittest.TestCase):
    def test_generates_expected_manifest(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            temp_path = Path(temp_dir)
            release_json = temp_path / "release.json"
            output_json = temp_path / "update.json"

            release_json.write_text(
                json.dumps(
                    {
                        "body": "Bug fixes and progress improvements",
                        "publishedAt": "2026-04-24T12:00:00Z",
                    }
                ),
                encoding="utf-8",
            )

            result = subprocess.run(
                [
                    sys.executable,
                    "scripts/generate_update_manifest.py",
                    "--version-code",
                    "2",
                    "--version-name",
                    "1.1.0",
                    "--tag",
                    "v1.1.0",
                    "--apk-url",
                    "https://github.com/YAOmeihah/VMian/releases/download/v1.1.0/vmian-v1.1.0.apk",
                    "--sha256",
                    "abc123",
                    "--release-json",
                    str(release_json),
                    "--output",
                    str(output_json),
                ],
                capture_output=True,
                text=True,
            )

            self.assertEqual(result.returncode, 0, msg=result.stderr)
            manifest = json.loads(output_json.read_text(encoding="utf-8"))
            self.assertEqual(manifest["versionCode"], 2)
            self.assertEqual(manifest["versionName"], "1.1.0")
            self.assertEqual(manifest["tagName"], "v1.1.0")
            self.assertEqual(
                manifest["apkUrl"],
                "https://github.com/YAOmeihah/VMian/releases/download/v1.1.0/vmian-v1.1.0.apk",
            )
            self.assertEqual(manifest["notes"], "Bug fixes and progress improvements")
            self.assertEqual(manifest["publishedAt"], "2026-04-24T12:00:00Z")
            self.assertEqual(manifest["sha256"], "abc123")


if __name__ == "__main__":
    unittest.main()
