#!/usr/bin/env bash
# Builds Mwenyeji's own vector map from OpenStreetMap as a single PMTiles file, in the OpenMapTiles
# schema the app's style (app/src/main/assets/map/style.json) is written for.
#
#   ./build_tiles.sh            # Kenya
#   ./build_tiles.sh tanzania   # any Geofabrik area name
#
# Needs Java 21 and about 3 GB of free disk. Rebuild monthly to pick up OSM edits, then run
# `node upload_tiles.js` to publish it.
set -euo pipefail
cd "$(dirname "$0")"

PLANETILER_VERSION=0.10.2
AREA="${1:-kenya}"

mkdir -p work
cd work
if [ ! -f planetiler.jar ]; then
  curl -fL -o planetiler.jar \
    "https://github.com/onthegomap/planetiler/releases/download/v${PLANETILER_VERSION}/planetiler.jar"
fi

# --download fetches the Geofabrik extract plus the water, lake and Natural Earth data it needs.
java -Xmx4g -jar planetiler.jar \
  --download \
  --area="$AREA" \
  --output="$AREA.pmtiles" \
  --force

ls -lh "$AREA.pmtiles"
