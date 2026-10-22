/**
 * Learns matatu shortcuts from riders' traces and publishes them for the app.
 *
 *   ./route_learning/start_valhalla.sh          # once; the first run builds Kenya's road graph
 *   node route_learning/learn_variants.js --dry-run
 *   node route_learning/learn_variants.js
 *
 * For every route and direction with traces in /ride_traces, each trace is snapped to OSM roads by
 * Valhalla, the stretches that left the route's line are grouped by road, and roads taken on at least
 * MIN_TRIPS separate rides replace that route's documents in /route_variants. Traces older than
 * --keep-days are then deleted: only the learned roads are kept long term.
 *
 * Options:
 *   --dry-run          print what would be written, change nothing
 *   --no-match         skip Valhalla and use the raw GPS points (noisier, for trying it out)
 *   --valhalla=URL     default http://localhost:8002
 *   --keep-days=N      default 90
 */
const admin = require("firebase-admin");
const { learnVariants, decodePolyline6, MIN_TRIPS } = require("./variants");

const serviceAccount = require("../serviceAccountKey.json");
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

const args = process.argv.slice(2);
const flag = (name) => args.includes(`--${name}`);
const option = (name, fallback) => {
  const hit = args.find((a) => a.startsWith(`--${name}=`));
  return hit ? hit.slice(name.length + 3) : fallback;
};
const DRY_RUN = flag("dry-run");
const NO_MATCH = flag("no-match");
const VALHALLA = option("valhalla", "http://localhost:8002");
const KEEP_DAYS = Number(option("keep-days", "90"));

/** The route's stops in the direction of travel, as the app orders them (inbound is outbound reversed). */
async function mainLine(routeId, direction) {
  const doc = await db.collection("route_stops").doc(routeId).get();
  const outbound = (doc.get("outbound") || [])
    .filter((s) => typeof s.lat === "number" && typeof s.lng === "number")
    .sort((a, b) => a.sequence - b.sequence)
    .map(({ lat, lng }) => ({ lat, lng }));
  return direction === "inbound" ? outbound.reverse() : outbound;
}

/** The road the matatu drove, from Valhalla's map matching, or null if Valhalla could not match it. */
async function matchToRoads(path) {
  try {
    const response = await fetch(`${VALHALLA}/trace_route`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        shape: path.map((p) => ({ lat: p.lat, lon: p.lng })),
        costing: "bus",
        shape_match: "map_snap",
        directions_type: "none",
      }),
    });
    if (!response.ok) return null;
    const legs = (await response.json()).trip?.legs || [];
    // Each leg starts where the last one ended; drop the repeated point.
    return legs.flatMap((leg, i) => decodePolyline6(leg.shape).slice(i === 0 ? 0 : 1));
  } catch {
    return null;
  }
}

async function publish(routeId, direction, variants) {
  const existing = await db
    .collection("route_variants")
    .where("routeId", "==", routeId)
    .where("direction", "==", direction)
    .get();
  const batch = db.batch();
  existing.docs.forEach((doc) => batch.delete(doc.ref));
  variants.forEach((variant, i) => {
    batch.set(db.collection("route_variants").doc(`${routeId}_${direction}_${i}`), {
      routeId,
      direction,
      points: variant.points,
      tripCount: variant.tripCount,
      hours: variant.hours,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });
  await batch.commit();
}

async function deleteOldTraces() {
  const cutoff = new Date(Date.now() - KEEP_DAYS * 24 * 60 * 60 * 1000);
  const old = await db.collection("ride_traces").where("createdAt", "<", cutoff).get();
  if (DRY_RUN) return old.size;
  for (let i = 0; i < old.docs.length; i += 400) {
    const batch = db.batch();
    old.docs.slice(i, i + 400).forEach((doc) => batch.delete(doc.ref));
    await batch.commit();
  }
  return old.size;
}

async function main() {
  if (!NO_MATCH) {
    const status = await fetch(`${VALHALLA}/status`).catch(() => null);
    if (!status?.ok) {
      throw new Error(`Valhalla is not answering at ${VALHALLA}. Run route_learning/start_valhalla.sh, or pass --no-match.`);
    }
  }

  const traces = await db.collection("ride_traces").get();
  const groups = new Map();
  for (const doc of traces.docs) {
    const t = doc.data();
    if (!t.routeId || !t.direction || !Array.isArray(t.points) || t.points.length < 2) continue;
    const key = `${t.routeId}|${t.direction}`;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key).push({ traceId: doc.id, hour: t.hour, path: t.points.map(({ lat, lng }) => ({ lat, lng })) });
  }
  console.log(`${traces.size} traces on ${groups.size} route directions. A road needs ${MIN_TRIPS} rides to count.`);

  for (const [key, rides] of groups) {
    const [routeId, direction] = key.split("|");
    const line = await mainLine(routeId, direction);
    if (line.length < 2) {
      console.log(`  ${key}: no stops, skipped`);
      continue;
    }

    let unmatched = 0;
    for (const ride of rides) {
      if (NO_MATCH) continue;
      const matched = await matchToRoads(ride.path);
      if (matched && matched.length >= 2) ride.path = matched;
      else unmatched++;
    }

    const variants = learnVariants(line, rides);
    const summary = variants.map((v) => `${v.tripCount} rides, hours ${v.hours.join(",")}`).join("; ") || "none";
    console.log(`  ${key}: ${rides.length} rides (${unmatched} unmatched), variants: ${summary}`);
    if (!DRY_RUN) await publish(routeId, direction, variants);
  }

  const deleted = await deleteOldTraces();
  console.log(`${DRY_RUN ? "Would delete" : "Deleted"} ${deleted} traces older than ${KEEP_DAYS} days.`);
}

main()
  .then(() => process.exit(0))
  .catch((e) => {
    console.error(e.message);
    process.exit(1);
  });
