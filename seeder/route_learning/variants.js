/**
 * Turns matatu ride traces into route variants: roads drivers keep taking instead of the main line.
 * Pure functions, no Firebase or network, so they can be tested on their own (variants.test.js).
 *
 * A point is { lat, lng }. A path is an array of points in the direction of travel.
 */

// Matched traces sit on the road, so this can be tighter than the app's 300 m off-route distance.
const OFF_ROUTE_M = 150;
// Shorter excursions are GPS noise, a fuel stop, or a turn into a stage.
const MIN_DETOUR_M = 300;
// Two detours are the same road when, on average, each lies within this of the other.
const SAME_ROAD_M = 75;
// A road becomes a variant once this many separate rides took it.
const MIN_TRIPS = 3;
// Variant shapes are simplified to this tolerance to keep Firestore documents small.
const SIMPLIFY_M = 10;

const EARTH_M = 6371008.8;
const toRad = (d) => (d * Math.PI) / 180;

function distanceM(a, b) {
  const dLat = toRad(b.lat - a.lat);
  const dLng = toRad(b.lng - a.lng);
  const h =
    Math.sin(dLat / 2) ** 2 + Math.cos(toRad(a.lat)) * Math.cos(toRad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 2 * EARTH_M * Math.asin(Math.sqrt(h));
}

/** Distance from p to the polyline through path, on a flat projection centred on p (fine at city scale). */
function distanceToPathM(p, path) {
  if (path.length === 0) return Infinity;
  const mPerDeg = 111320;
  const lngScale = Math.cos(toRad(p.lat));
  const x = (q) => (q.lng - p.lng) * lngScale * mPerDeg;
  const y = (q) => (q.lat - p.lat) * mPerDeg;
  if (path.length === 1) return Math.hypot(x(path[0]), y(path[0]));
  let best = Infinity;
  for (let i = 0; i < path.length - 1; i++) {
    const ax = x(path[i]);
    const ay = y(path[i]);
    const dx = x(path[i + 1]) - ax;
    const dy = y(path[i + 1]) - ay;
    const len2 = dx * dx + dy * dy;
    const t = len2 === 0 ? 0 : Math.max(0, Math.min(1, -(ax * dx + ay * dy) / len2));
    best = Math.min(best, Math.hypot(ax + t * dx, ay + t * dy));
  }
  return best;
}

function lengthM(path) {
  let total = 0;
  for (let i = 1; i < path.length; i++) total += distanceM(path[i - 1], path[i]);
  return total;
}

/** Points every stepM along the path, so paths drawn with different point densities compare fairly. */
function resample(path, stepM = 25) {
  if (path.length < 2) return path.slice();
  const out = [path[0]];
  let carry = 0;
  for (let i = 1; i < path.length; i++) {
    const a = path[i - 1];
    const b = path[i];
    const seg = distanceM(a, b);
    let at = stepM - carry;
    while (at <= seg) {
      const t = at / seg;
      out.push({ lat: a.lat + (b.lat - a.lat) * t, lng: a.lng + (b.lng - a.lng) * t });
      at += stepM;
    }
    carry = seg - (at - stepM);
  }
  const last = path[path.length - 1];
  if (distanceM(out[out.length - 1], last) > 1) out.push(last);
  return out;
}

/**
 * The stretches of a ride that left the known roads, each with the on-route point before and after it,
 * so a variant joins the main line at both ends.
 */
function findDetours(ride, knownPaths) {
  const off = ride.map((p) => Math.min(...knownPaths.map((path) => distanceToPathM(p, path))) > OFF_ROUTE_M);
  const detours = [];
  let i = 0;
  while (i < ride.length) {
    if (!off[i]) {
      i++;
      continue;
    }
    let j = i;
    while (j + 1 < ride.length && off[j + 1]) j++;
    // Only the stretch actually off the route has to be long; the links back to the line do not count.
    if (lengthM(ride.slice(i, j + 1)) >= MIN_DETOUR_M) {
      detours.push(ride.slice(Math.max(0, i - 1), Math.min(ride.length, j + 2)));
    }
    i = j + 1;
  }
  return detours;
}

/** How far apart two paths are on average, measured both ways so a short path inside a long one is not "the same". */
function pathDistanceM(a, b) {
  const ra = resample(a);
  const rb = resample(b);
  const mean = (from, to) => from.reduce((sum, p) => sum + distanceToPathM(p, to), 0) / from.length;
  return Math.max(mean(ra, b), mean(rb, a));
}

/**
 * Groups detours that follow the same road. Each detour is { traceId, hour, path }.
 * Returns clusters of detours; a cluster's first member is its seed.
 */
function clusterDetours(detours) {
  const clusters = [];
  for (const detour of detours) {
    const home = clusters.find((c) => pathDistanceM(c[0].path, detour.path) <= SAME_ROAD_M);
    if (home) home.push(detour);
    else clusters.push([detour]);
  }
  return clusters;
}

/** The member closest to all the others: a real ride, not an average that might cut through buildings. */
function medoid(cluster) {
  let best = cluster[0];
  let bestCost = Infinity;
  for (const candidate of cluster) {
    const cost = cluster.reduce((sum, other) => sum + (other === candidate ? 0 : pathDistanceM(candidate.path, other.path)), 0);
    if (cost < bestCost) {
      best = candidate;
      bestCost = cost;
    }
  }
  return best;
}

/** Douglas–Peucker: drops points that lie within toleranceM of the line through their neighbours. */
function simplify(path, toleranceM = SIMPLIFY_M) {
  if (path.length <= 2) return path.slice();
  let worst = 0;
  let index = 0;
  const ends = [path[0], path[path.length - 1]];
  for (let i = 1; i < path.length - 1; i++) {
    const d = distanceToPathM(path[i], ends);
    if (d > worst) {
      worst = d;
      index = i;
    }
  }
  if (worst <= toleranceM) return ends;
  const left = simplify(path.slice(0, index + 1), toleranceM);
  const right = simplify(path.slice(index), toleranceM);
  return left.slice(0, -1).concat(right);
}

/**
 * All the variants of one route in one direction.
 *
 * @param mainLine The route's stops, in the direction of travel.
 * @param rides    [{ traceId, hour, path }], each path already matched to roads where possible.
 * @returns        [{ points, tripCount, hours }], most-taken first.
 */
function learnVariants(mainLine, rides) {
  const detours = rides.flatMap((ride) =>
    findDetours(ride.path, [mainLine]).map((path) => ({ traceId: ride.traceId, hour: ride.hour, path })),
  );
  return clusterDetours(detours)
    .map((cluster) => ({
      cluster,
      tripCount: new Set(cluster.map((d) => d.traceId)).size,
    }))
    .filter(({ tripCount }) => tripCount >= MIN_TRIPS)
    .sort((a, b) => b.tripCount - a.tripCount)
    .map(({ cluster, tripCount }) => ({
      points: simplify(medoid(cluster).path).map(({ lat, lng }) => ({ lat, lng })),
      tripCount,
      hours: [...new Set(cluster.map((d) => d.hour))].sort((a, b) => a - b),
    }));
}

/** Decodes a Valhalla shape (Google polyline encoding at 6 decimal places). */
function decodePolyline6(encoded) {
  const points = [];
  let index = 0;
  let lat = 0;
  let lng = 0;
  const next = () => {
    let result = 0;
    let shift = 0;
    let byte;
    do {
      byte = encoded.charCodeAt(index++) - 63;
      result |= (byte & 0x1f) << shift;
      shift += 5;
    } while (byte >= 0x20);
    return result & 1 ? ~(result >> 1) : result >> 1;
  };
  while (index < encoded.length) {
    lat += next();
    lng += next();
    points.push({ lat: lat / 1e6, lng: lng / 1e6 });
  }
  return points;
}

module.exports = {
  OFF_ROUTE_M,
  MIN_DETOUR_M,
  SAME_ROAD_M,
  MIN_TRIPS,
  distanceM,
  distanceToPathM,
  lengthM,
  resample,
  findDetours,
  pathDistanceM,
  clusterDetours,
  medoid,
  simplify,
  learnVariants,
  decodePolyline6,
};
