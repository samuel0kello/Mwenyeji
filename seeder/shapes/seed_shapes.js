/**
 * Writes /route_shapes/{routeId}: the road each direction of a GTFS route follows, so the app draws
 * the route on the road (and on the right side of it) instead of straight lines between stops.
 *
 *   node shapes/seed_shapes.js --dry-run
 *   node shapes/seed_shapes.js
 *
 * Only reads ./gtfs and only writes /route_shapes. Safe to run again.
 */
const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");
const { parse } = require("csv-parse/sync");
const { buildShapeDocs } = require("./shapes");

const DRY_RUN = process.argv.includes("--dry-run");
const GTFS_DIR = path.join(__dirname, "..", "gtfs");
const BATCH_SIZE = 400;

const readCsv = (name) =>
  parse(fs.readFileSync(path.join(GTFS_DIR, name), "utf8").replace(/^﻿/, ""), {
    columns: true,
    skip_empty_lines: true,
    trim: true,
    relax_column_count: true,
  });

async function main() {
  const docs = buildShapeDocs(readCsv("routes.txt"), readCsv("trips.txt"), readCsv("shapes.txt"));
  const points = docs.reduce((n, d) => n + (d.data.outbound.length + d.data.inbound.length) / 2, 0);
  const oneWay = docs.filter((d) => d.data.inbound.length === 0 || d.data.outbound.length === 0).length;
  console.log(`${docs.length} routes, ${points} points, ${oneWay} with a road one way only`);
  if (DRY_RUN) return;

  admin.initializeApp({ credential: admin.credential.cert(require("../serviceAccountKey.json")) });
  const db = admin.firestore();
  for (let i = 0; i < docs.length; i += BATCH_SIZE) {
    const batch = db.batch();
    for (const doc of docs.slice(i, i + BATCH_SIZE)) batch.set(db.collection("route_shapes").doc(doc.id), doc.data);
    await batch.commit();
  }
  console.log(`Wrote ${docs.length} documents to /route_shapes`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
