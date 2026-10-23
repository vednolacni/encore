#!/bin/bash
# A cloud session commits as Claude unless told otherwise, so commits here carry the owner's name
# instead. They go unsigned, since the session signs with a key of its own that GitHub would never
# match to the owner and would show as unverified. Local sessions keep the computer's own settings.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-.}"
git config user.name "vednolacni"
git config user.email "maticresnik13@gmail.com"
git config commit.gpgsign false
