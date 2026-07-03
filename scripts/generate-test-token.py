#!/usr/bin/env python3
"""
scripts/generate-test-token.py

Mints an RS256 JWT that backend-v2's docker profile will accept.
The private key (scripts/dev-private.pem) is dev-only — production
swaps in a real Authorization Server.

Usage:
  python3 scripts/generate-test-token.py
  python3 scripts/generate-test-token.py --user-id 1 --is-admin true

Outputs the token to stdout. Pipe it into curl:
  TOKEN=$(python3 scripts/generate-test-token.py)
  curl -H "Authorization: Bearer $TOKEN" http://localhost:8092/api/wallet/1
"""
import argparse
import json
import sys
import time
from pathlib import Path

import jwt as pyjwt  # PyJWT

DEFAULT_KEY = Path(__file__).parent / "dev-private.pem"


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--user-id", type=int, default=1)
    p.add_argument("--username", default="alice")
    p.add_argument("--is-admin", default="false")
    p.add_argument("--key", default=str(DEFAULT_KEY))
    p.add_argument("--ttl", type=int, default=3600)
    args = p.parse_args()

    private_key = Path(args.key).read_text()
    now = int(time.time())
    claims = {
        "iss": "ypat-dev",
        "sub": args.username,
        "aud": "backend-v2",
        "user_id": args.user_id,
        "preferred_username": args.username,
        "is_admin": args.is_admin.lower() == "true",
        "scope": "wallet:read wallet:write identity:read",
        "iat": now,
        "nbf": now,
        "exp": now + args.ttl,
        "jti": f"dev-{now}-{args.user_id}",
    }
    token = pyjwt.encode(claims, private_key, algorithm="RS256")
    print(token)


if __name__ == "__main__":
    main()