#!/usr/bin/env bash
# Starts Valhalla, the open-source routing engine, on Kenya's OSM data at http://localhost:8002.
# learn_variants.js uses it to snap each noisy GPS trace onto the roads the matatu actually drove.
#
# The first start builds Valhalla's road graph from the .osm.pbf, which takes 10–30 minutes.
# Later starts reuse it. Needs Docker. Stop with: docker stop mwenyeji-valhalla
set -euo pipefail
cd "$(dirname "$0")"

VALHALLA_IMAGE=ghcr.io/valhalla/valhalla-scripted:3.9.1
AREA="${1:-kenya}"

mkdir -p work/valhalla
if [ ! -f "work/valhalla/$AREA.osm.pbf" ]; then
  # Reuse the extract the map build already downloaded, otherwise fetch it.
  if [ -f "../map/work/data/sources/$AREA.osm.pbf" ]; then
    cp "../map/work/data/sources/$AREA.osm.pbf" work/valhalla/
  else
    curl -fL -o "work/valhalla/$AREA.osm.pbf" "https://download.geofabrik.de/africa/$AREA-latest.osm.pbf"
  fi
fi

if docker ps -a --format '{{.Names}}' | grep -qx mwenyeji-valhalla; then
  docker start mwenyeji-valhalla
else
  docker run -d --name mwenyeji-valhalla -p 8002:8002 \
    -v "$PWD/work/valhalla:/custom_files" \
    -e serve_tiles=True -e build_admins=False -e build_time_zones=False -e build_elevation=False \
    "$VALHALLA_IMAGE"
fi

echo "Valhalla is starting. Follow the build with: docker logs -f mwenyeji-valhalla"
echo "Ready when this answers: curl -s localhost:8002/status"
