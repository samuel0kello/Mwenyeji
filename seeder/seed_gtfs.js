/**
 * Mwenyeji — GTFS Seeder (Production)
 *
 * Reads Digital Matatus GTFS data and writes to Firestore:
 *   /stops/{stopId}              — all 4,284 stops
 *   /routes/{routeId}            — 136 GTFS routes (does NOT touch existing community routes)
 *   /route_stops/{routeId}       — ordered stop list, loaded on demand
 *
 * Shapes (/route_shapes) are intentionally skipped — map view is future work.
 *
 * Safety guarantees:
 *   - Validates ALL data before writing ANYTHING to Firestore
 *   - Never overwrites documents where source != "digital_matatus"
 *   - Dry run mode (DRY_RUN=true) logs everything without writing
 *   - Idempotent — safe to run multiple times
 *
 * Usage:
 *   npm install firebase-admin csv-parse ngeohash
 *   Place serviceAccountKey.json in this directory
 *   Place all GTFS .txt files in ./gtfs/
 *   node seed_gtfs.js              (dry run by default)
 *   DRY_RUN=false node seed_gtfs.js (write to Firestore)
 */

"use strict";

const admin    = require("firebase-admin");
const fs       = require("fs");
const path     = require("path");
const { parse } = require("csv-parse/sync");
const ngeohash = require("ngeohash");

// ── Config ────────────────────────────────────────────────────────────────────

const DRY_RUN       = process.env.DRY_RUN !== "false"; // safe by default
const GTFS_DIR      = process.env.GTFS_DIR || "./gtfs";
const BATCH_SIZE    = 400; // Firestore max is 500, use 400 for safety
const GEO_PRECISION = 5;   // ~5km — appropriate for Nairobi route proximity

// Defaults for 4 routes missing frequency data
const DEFAULT_PEAK_HEADWAY    = 10;
const DEFAULT_OFFPEAK_HEADWAY = 20;

// ── Firebase init ─────────────────────────────────────────────────────────────

const serviceAccount = require("./serviceAccountKey.json");
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const db = admin.firestore();

// ── Logging ───────────────────────────────────────────────────────────────────

const log = {
info:    (msg) => console.log(`  ℹ  ${msg}`),
ok:      (msg) => console.log(`  ✓  ${msg}`),
warn:    (msg) => console.warn(`  ⚠  ${msg}`),
error:   (msg) => console.error(`  ✗  ${msg}`),
section: (msg) => console.log(`\n${"─".repeat(60)}\n  ${msg}\n${"─".repeat(60)}`),
};

// ── CSV reader — handles quoted fields ("Bypass Ruiru, City Cabanas") ─────────

function readCsv(filename) {
const filePath = path.join(GTFS_DIR, filename);

if (!fs.existsSync(filePath)) {
throw new Error(`GTFS file not found: ${filePath}`);
}

const content = fs
.readFileSync(filePath, "utf8")
.replace(/^\uFEFF/, "")  // strip UTF-8 BOM
.replace(/\r\n/g, "\n")  // normalise Windows line endings
.replace(/\r/g, "\n");

return parse(content, {
columns: true,
skip_empty_lines: true,
trim: true,
relax_column_count: true,
});
}

// ── Parse from / via / to from GTFS long name ─────────────────────────────────
// Uses the long name because direction_id conventions are inconsistent in this dataset.
// csv-parse handles quoted fields so longName arrives already clean.

function parseFromViaTo(longName) {
const segments = longName
.split("-")
.map((s) => s.trim())
.filter(Boolean);

if (segments.length === 0) return { from: longName, via: "", to: longName };
if (segments.length === 1) return { from: segments[0], via: "", to: segments[0] };

return {
from: segments[0],
to:   segments[segments.length - 1],
via:  segments.slice(1, -1).join(" - "),
};
}

// ── Build lookup maps ─────────────────────────────────────────────────────────

function buildLookupMaps(trips, stopTimes, stops, frequencies) {
const stopMap = new Map();
for (const s of stops) {
const lat = parseFloat(s.stop_lat);
const lng = parseFloat(s.stop_lon);
if (isNaN(lat) || isNaN(lng)) continue;
stopMap.set(s.stop_id, {
stopId: s.stop_id,
name:   s.stop_name.trim(),
lat,
lng,
isHub:  s.location_type === "1",
});
}

const stopsByTrip = new Map();
for (const st of stopTimes) {
const list = stopsByTrip.get(st.trip_id) || [];
list.push({ stopId: st.stop_id, sequence: parseInt(st.stop_sequence, 10) });
stopsByTrip.set(st.trip_id, list);
}
for (const [, list] of stopsByTrip) {
list.sort((a, b) => a.sequence - b.sequence);
}

// routeId → { dir0: trip, dir1: trip }
const tripsByRoute = new Map();
for (const t of trips) {
const entry = tripsByRoute.get(t.route_id) || { dir0: null, dir1: null };
const key   = t.direction_id === "0" ? "dir0" : "dir1";
if (!entry[key]) entry[key] = t;
tripsByRoute.set(t.route_id, entry);
}

// tripId → { peakHeadwayMins, offPeakHeadwayMins }
const headwayByTrip = new Map();
for (const f of frequencies) {
const secs = parseInt(f.headway_secs, 10);
if (isNaN(secs)) continue;
const mins     = Math.round(secs / 60);
const start    = f.start_time;
const existing = headwayByTrip.get(f.trip_id) || {};
if (start === "06:00:00" || start === "15:00:00") {
existing.peakHeadwayMins = mins;
} else if (start === "09:00:00") {
existing.offPeakHeadwayMins = mins;
}
headwayByTrip.set(f.trip_id, existing);
}

return { stopMap, stopsByTrip, tripsByRoute, headwayByTrip };
}

// ── Validation ────────────────────────────────────────────────────────────────

function validateData(routes, stops, stopTimes, maps) {
const errors   = [];
const warnings = [];

let badStops = 0;
for (const s of stops) {
if (isNaN(parseFloat(s.stop_lat)) || isNaN(parseFloat(s.stop_lon))) badStops++;
}
if (badStops > 0) warnings.push(`${badStops} stops with invalid coordinates — will be skipped`);

for (const r of routes) {
const rt = maps.tripsByRoute.get(r.route_id);
if (!rt || (!rt.dir0 && !rt.dir1)) {
errors.push(`Route ${r.route_id} (${r.route_short_name}) has no trips`);
}
}

const unknownStopCount = new Set(
stopTimes
.filter((st) => !maps.stopMap.has(st.stop_id))
.map((st) => st.stop_id)
).size;
if (unknownStopCount > 0) {
warnings.push(`${unknownStopCount} stop_times reference unknown stops — will be omitted`);
}

const routesNoFreq = routes
.filter((r) => {
const rt = maps.tripsByRoute.get(r.route_id);
if (!rt) return false;
const trip = rt.dir0 || rt.dir1;
return trip && !maps.headwayByTrip.has(trip.trip_id);
})
.map((r) => r.route_short_name);

if (routesNoFreq.length > 0) {
warnings.push(`${routesNoFreq.length} routes missing frequency data (using defaults): ${routesNoFreq.join(", ")}`);
}

return { valid: errors.length === 0, errors, warnings };
}

// ── Build stop documents ──────────────────────────────────────────────────────

function buildStopDocs(stops) {
return stops
.filter((s) => !isNaN(parseFloat(s.stop_lat)) && !isNaN(parseFloat(s.stop_lon)))
.map((s) => {
const lat = parseFloat(s.stop_lat);
const lng = parseFloat(s.stop_lon);
return {
id:   sanitiseDocId(s.stop_id),
data: {
stopId:  s.stop_id,
name:    s.stop_name.trim(),
lat,
lng,
geohash: ngeohash.encode(lat, lng, GEO_PRECISION),
isHub:   s.location_type === "1",
},
};
});
}

// ── Build route + route_stop documents ────────────────────────────────────────

function buildRouteDocs(routes, maps) {
const routeDocs     = [];
const routeStopDocs = [];

for (const r of routes) {
const rt = maps.tripsByRoute.get(r.route_id);
if (!rt) continue;

const primaryTrip   = rt.dir0 || rt.dir1;
const secondaryTrip = primaryTrip === rt.dir0 ? rt.dir1 : rt.dir0;

const { from, via, to } = parseFromViaTo(r.route_long_name);

// Resolve stops for both directions
const resolve = (tripId) => {
if (!tripId) return [];
return (maps.stopsByTrip.get(tripId) || [])
.map((st) => {
const stop = maps.stopMap.get(st.stopId);
if (!stop) return null;
return { stopId: stop.stopId, name: stop.name, lat: stop.lat, lng: stop.lng, sequence: st.sequence };
})
.filter(Boolean);
};

const outboundStops = resolve(primaryTrip.trip_id);
const inboundStops  = resolve(secondaryTrip?.trip_id);

const firstStop = outboundStops[0];
const lastStop  = outboundStops[outboundStops.length - 1];

if (!firstStop || !lastStop) {
log.warn(`Route ${r.route_short_name}: no valid stops — skipping`);
continue;
}

// Headways
const hw = maps.headwayByTrip.get(primaryTrip.trip_id)
|| maps.headwayByTrip.get(secondaryTrip?.trip_id)
|| {};
const peakHeadwayMins    = hw.peakHeadwayMins    ?? DEFAULT_PEAK_HEADWAY;
const offPeakHeadwayMins = hw.offPeakHeadwayMins ?? DEFAULT_OFFPEAK_HEADWAY;

// Deduplicated lowercase search terms
const searchTerms = [...new Set([
r.route_short_name.toLowerCase(),
from.toLowerCase(),
to.toLowerCase(),
`${from.toLowerCase()} ${to.toLowerCase()}`,
`${to.toLowerCase()} ${from.toLowerCase()}`,
])];

routeDocs.push({
id: r.route_id,
data: {
// Identity
routeId:     r.route_id,
routeNumber: r.route_short_name.trim(),
longName:    r.route_long_name.trim(),

// Human readable
from,
via,
to,

// Both termini stored — proximity sorting uses whichever is closer to user
terminus1Lat:      firstStop.lat,
terminus1Lng:      firstStop.lng,
terminus1Geohash:  ngeohash.encode(firstStop.lat, firstStop.lng, GEO_PRECISION),
terminus2Lat:      lastStop.lat,
terminus2Lng:      lastStop.lng,
terminus2Geohash:  ngeohash.encode(lastStop.lat, lastStop.lng, GEO_PRECISION),

// Stop summary — full list in /route_stops
firstStopId: firstStop.stopId,
lastStopId:  lastStop.stopId,
stopCount:   outboundStops.length,

// Shape references — for future map view
outboundShapeId:  primaryTrip.shape_id    ?? null,
inboundShapeId:   secondaryTrip?.shape_id ?? null,

// Frequency
peakHeadwayMins,
offPeakHeadwayMins,

// Search
searchTerms,

// Community fields — empty until contributors fill them
fareKsh:       null,
bestTimeOfDay: "ANYTIME",
timingReason:  "",
steps:         [],
warnings:      "",
tags:          [],
sacco:         "",

// Trust signals
confirmedCount:  0,
didntWorkCount:  0,
outdatedCount:   0,
lastConfirmedAt: null,
contributorId:   "gtfs_seed",

// Provenance
source:     "digital_matatus",
isEnriched: false,
createdAt:  admin.firestore.FieldValue.serverTimestamp(),
},
});

routeStopDocs.push({
id: r.route_id,
data: {
routeId:  r.route_id,
outbound: outboundStops,
inbound:  inboundStops,
},
});
}

return { routeDocs, routeStopDocs };
}

// ── Batch writer ──────────────────────────────────────────────────────────────
// checkSource=true: skips documents where source != "digital_matatus"
// This protects the 12 existing community routes from being overwritten.

async function writeBatches(collectionName, docs, options = {}) {
const { checkSource = false } = options;
let written = 0;
let skipped = 0;

for (let i = 0; i < docs.length; i += BATCH_SIZE) {
const chunk = docs.slice(i, i + BATCH_SIZE);
const batch = db.batch();
let batchCount = 0;

for (const doc of chunk) {
const ref = db.collection(collectionName).doc(doc.id);

if (checkSource && !DRY_RUN) {
const existing = await ref.get();
if (existing.exists && existing.data().source !== "digital_matatus") {
skipped++;
continue;
}
}

if (!DRY_RUN) batch.set(ref, doc.data);
written++;
batchCount++;
}

if (!DRY_RUN && batchCount > 0) await batch.commit();

const batchNum     = Math.floor(i / BATCH_SIZE) + 1;
const totalBatches = Math.ceil(docs.length / BATCH_SIZE);
log.info(`${collectionName}: batch ${batchNum}/${totalBatches} — ${Math.min(i + BATCH_SIZE, docs.length)}/${docs.length}`);
}

return { written, skipped };
}

// ── Main ──────────────────────────────────────────────────────────────────────

async function main() {
console.log("\n🚌  Mwenyeji GTFS Seeder");
console.log(`   Mode: ${DRY_RUN ? "DRY RUN (no writes)" : "LIVE — writing to Firestore"}`);
console.log(`   GTFS: ${path.resolve(GTFS_DIR)}\n`);

log.section("Step 1: Reading GTFS files");
let routes, stops, trips, stopTimes, frequencies;
try {
routes      = readCsv("routes.txt");
stops       = readCsv("stops.txt");
trips       = readCsv("trips.txt");
stopTimes   = readCsv("stop_times.txt");
frequencies = readCsv("frequencies.txt");
} catch (err) {
log.error(`Failed to read GTFS files: ${err.message}`);
process.exit(1);
}

log.ok(`routes.txt      → ${routes.length} routes`);
log.ok(`stops.txt       → ${stops.length} stops`);
log.ok(`trips.txt       → ${trips.length} trips`);
log.ok(`stop_times.txt  → ${stopTimes.length} entries`);
log.ok(`frequencies.txt → ${frequencies.length} entries`);

log.section("Step 2: Building lookup maps");
const maps = buildLookupMaps(trips, stopTimes, stops, frequencies);
log.ok(`Stop map:       ${maps.stopMap.size} stops indexed`);
log.ok(`Stops by trip:  ${maps.stopsByTrip.size} trips with stop data`);
log.ok(`Trips by route: ${maps.tripsByRoute.size} routes`);
log.ok(`Headway data:   ${maps.headwayByTrip.size} trips`);

log.section("Step 3: Validating data");
const { valid, errors, warnings } = validateData(routes, stops, stopTimes, maps);
warnings.forEach((w) => log.warn(w));
errors.forEach((e) => log.error(e));
if (!valid) {
log.error("Validation failed — aborting");
process.exit(1);
}
log.ok("Validation passed — safe to write");

log.section("Step 4: Building documents");
const stopDocs = buildStopDocs(stops);
const { routeDocs, routeStopDocs } = buildRouteDocs(routes, maps);
log.ok(`Stop documents:       ${stopDocs.length}`);
log.ok(`Route documents:      ${routeDocs.length}`);
log.ok(`Route stop documents: ${routeStopDocs.length}`);

// Show a sample document so you can verify before running live
if (routeDocs.length > 0) {
const s = routeDocs[3];
log.info(`\nSample — route ${s.data.routeNumber}:`);
console.log(JSON.stringify({
routeNumber:       s.data.routeNumber,
from:              s.data.from,
via:               s.data.via,
to:                s.data.to,
stopCount:         s.data.stopCount,
terminus1Geohash:  s.data.terminus1Geohash,
terminus2Geohash:  s.data.terminus2Geohash,
peakHeadwayMins:   s.data.peakHeadwayMins,
offPeakHeadwayMins:s.data.offPeakHeadwayMins,
searchTerms:       s.data.searchTerms,
}, null, 4));
}

if (DRY_RUN) {
log.section("Dry run complete — nothing written");
log.info("Run with: DRY_RUN=false node seed_gtfs.js");
return;
}

log.section("Step 5: Writing to Firestore");

log.info("Writing /stops...");
const stopsResult = await writeBatches("stops", stopDocs);
log.ok(`/stops: ${stopsResult.written} written`);

log.info("Writing /routes (community routes protected)...");
const routesResult = await writeBatches("routes", routeDocs, { checkSource: true });
log.ok(`/routes: ${routesResult.written} written, ${routesResult.skipped} community routes untouched`);

log.info("Writing /route_stops...");
const routeStopsResult = await writeBatches("route_stops", routeStopDocs);
log.ok(`/route_stops: ${routeStopsResult.written} written`);

log.section("Complete");
console.log(`  /stops        → ${stopsResult.written} documents`);
console.log(`  /routes       → ${routesResult.written} written, ${routesResult.skipped} community routes protected`);
console.log(`  /route_stops  → ${routeStopsResult.written} documents`);
console.log("\n  Add these Firestore indexes:");
console.log("    routes: terminus1Geohash ASC, confirmedCount DESC");
console.log("    routes: terminus2Geohash ASC, confirmedCount DESC");
console.log("    routes: source ASC, confirmedCount DESC");
}

main().catch((err) => {
log.error(`Unhandled error: ${err.message}`);
console.error(err.stack);
process.exit(1);
});

function sanitiseDocId(id) {
  // Firestore document IDs cannot contain '/'
  // Replace with '_' — original stopId is preserved inside the document data
  return id.replace(/\//g, "_");
}