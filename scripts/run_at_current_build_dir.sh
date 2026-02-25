#!/bin/bash

set -e

cd "${MESON_BUILD_ROOT}/${MESON_SUBDIR}"
echo "Current working directory: $(pwd)"

exec "$@"
