#!/bin/bash

set -e
cd "${MESON_SOURCE_ROOT}"
echo "Current working directory: $(pwd)"

exec "$@"
