#!/usr/bin/env bash
set -Eeuxo pipefail # https://vaneyckt.io/posts/safer_bash_scripts_with_set_euxo_pipefail/

export RELEASE=${1:-0.1.0-local}
docker build -t "swisscon-service-api:$RELEASE" .