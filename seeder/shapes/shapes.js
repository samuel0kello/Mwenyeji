/**
 * Builds /route_shapes documents from the GTFS: the road each direction of a route follows.
 *
 * "outbound" and "inbound" match /route_stops: outbound is the route's first direction_id 0 trip
 * (or its first trip, if it has none), inbound the first trip the other way. Same rule as seed_gtfs.js.
 */

/** routeId → { dir0, dir1 }: the first trip each way, in trips.txt order. */
function tripsByRoute(trips) {
  const byRoute = new Map();
  for (const t of trips) {
    const entry = byRoute.get(t.route_id) || { dir0: null, dir1: null };
    const key = t.direction_id === "0" ? "dir0" : "dir1";
    if (!entry[key]) entry[key] = t;
    byRoute.set(t.route_id, entry);
  }
  return byRoute;
}

/** shapeId → [lat, lng, lat, lng, …] in sequence, rounded to about a metre. */
function flatShapes(shapePoints) {
  const byShape = new Map();
  for (const p of shapePoints) {
    const lat = parseFloat(p.shape_pt_lat);
    const lng = parseFloat(p.shape_pt_lon);
    const seq = parseInt(p.shape_pt_sequence, 10);
    if (isNaN(lat) || isNaN(lng) || isNaN(seq)) continue;
    const list = byShape.get(p.shape_id) || [];
    list.push({ seq, lat, lng });
    byShape.set(p.shape_id, list);
  }
  const round = (x) => Math.round(x * 1e5) / 1e5;
  const flat = new Map();
  for (const [id, list] of byShape) {
    list.sort((a, b) => a.seq - b.seq);
    flat.set(id, list.flatMap((p) => [round(p.lat), round(p.lng)]));
  }
  return flat;
}

/** One document per route that has at least one shape. */
function buildShapeDocs(routes, trips, shapePoints) {
  const byRoute = tripsByRoute(trips);
  const shapes = flatShapes(shapePoints);
  const docs = [];
  for (const r of routes) {
    const rt = byRoute.get(r.route_id);
    if (!rt) continue;
    const primary = rt.dir0 || rt.dir1;
    const secondary = primary === rt.dir0 ? rt.dir1 : rt.dir0;
    const outbound = shapes.get(primary?.shape_id) || [];
    const inbound = shapes.get(secondary?.shape_id) || [];
    if (outbound.length < 4 && inbound.length < 4) continue;
    docs.push({ id: r.route_id, data: { routeId: r.route_id, outbound, inbound } });
  }
  return docs;
}

module.exports = { buildShapeDocs };
