/**
 * Publishes the map built by build_tiles.sh to Firebase Storage at map/<area>.pmtiles and prints the
 * URL to put in gradle.properties as MWENYEJI_MAP_TILES_URL.
 *
 *   node map/upload_tiles.js            # map/work/kenya.pmtiles
 *   node map/upload_tiles.js tanzania
 *
 * The file gets a download token, so the app can read it without changing Storage security rules.
 * Re-running keeps the same token, so the URL in the app stays valid after a monthly rebuild.
 */
const admin = require("firebase-admin");
const crypto = require("crypto");
const path = require("path");
const fs = require("fs");

const serviceAccount = require("../serviceAccountKey.json");
const BUCKET = "mwenyeji-d7ad3.firebasestorage.app";

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  storageBucket: BUCKET,
});

async function main() {
  const area = process.argv[2] || "kenya";
  const local = path.join(__dirname, "work", `${area}.pmtiles`);
  if (!fs.existsSync(local)) throw new Error(`${local} not found. Run ./map/build_tiles.sh ${area} first.`);

  const destination = `map/${area}.pmtiles`;
  const file = admin.storage().bucket().file(destination);

  const [exists] = await file.exists();
  const existingToken = exists
    ? (await file.getMetadata())[0].metadata?.firebaseStorageDownloadTokens
    : undefined;
  const token = existingToken || crypto.randomUUID();

  console.log(`Uploading ${local} (${(fs.statSync(local).size / 1e6).toFixed(0)} MB) to ${destination}…`);
  await admin.storage().bucket().upload(local, {
    destination,
    resumable: true,
    metadata: {
      contentType: "application/octet-stream",
      // Tiles are read in small ranges and change monthly; let phones cache them for a day.
      cacheControl: "public, max-age=86400",
      metadata: { firebaseStorageDownloadTokens: token },
    },
  });

  const url =
    `https://firebasestorage.googleapis.com/v0/b/${BUCKET}/o/${encodeURIComponent(destination)}` +
    `?alt=media&token=${token}`;
  console.log("\nDone. Put this in gradle.properties:\n");
  console.log(`MWENYEJI_MAP_TILES_URL=${url}\n`);
}

main().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
