// Run with: node --test route_learning/*.test.js
const test = require("node:test");
const assert = require("node:assert/strict");
const v = require("./variants");

// A route heading east along one latitude, stops ~1.1 km apart.
const LAT = -1.28;
const M = 1 / 111320; // degrees of latitude per metre
const mainLine = [0, 1, 2, 3].map((i) => ({ lat: LAT, lng: 36.82 + i * 0.01 }));

/** A ride along the route, leaving it [northM] metres north between [fromM] and [toM] east of the start. */
function ride(fromM, toM, northM, jitterM = 0) {
  const path = [];
  for (let east = 0; east <= 3300; east += 50) {
    const north = east >= fromM && east <= toM ? northM : 0;
    path.push({ lat: LAT + (north + jitterM) * M, lng: 36.82 + (east * M) / Math.cos((LAT * Math.PI) / 180) });
  }
  return path;
}

test("a ride on the main line has no detours", () => {
  assert.deepEqual(v.findDetours(ride(0, 0, 0), [mainLine]), []);
});

test("a ride that leaves the line has one detour joined to the line at both ends", () => {
  const detours = v.findDetours(ride(1000, 2000, 400), [mainLine]);
  assert.equal(detours.length, 1);
  const [detour] = detours;
  assert.ok(v.distanceToPathM(detour[0], mainLine) < 1);
  assert.ok(v.distanceToPathM(detour[detour.length - 1], mainLine) < 1);
});

test("a short excursion is noise, not a detour", () => {
  assert.deepEqual(v.findDetours(ride(1000, 1050, 400), [mainLine]), []);
});

test("three rides on the same back road make a variant; two do not", () => {
  const rides = [0, 10, -10].map((jitter, i) => ({ traceId: `t${i}`, hour: 7 + i, path: ride(1000, 2000, 400, jitter) }));

  assert.deepEqual(v.learnVariants(mainLine, rides.slice(0, 2)), []);

  const variants = v.learnVariants(mainLine, rides);
  assert.equal(variants.length, 1);
  assert.equal(variants[0].tripCount, 3);
  assert.deepEqual(variants[0].hours, [7, 8, 9]);
  // Simplified to its corners, still 400 m north in the middle.
  assert.ok(variants[0].points.length < 10);
  const middle = { lat: LAT + 400 * M, lng: 36.82 + (1500 * M) / Math.cos((LAT * Math.PI) / 180) };
  assert.ok(v.distanceToPathM(middle, variants[0].points) < 20);
});

test("different back roads stay separate variants", () => {
  const north = [0, 1, 2].map((i) => ({ traceId: `n${i}`, hour: 8, path: ride(1000, 2000, 400) }));
  const south = [0, 1, 2].map((i) => ({ traceId: `s${i}`, hour: 17, path: ride(1000, 2000, -400) }));

  const variants = v.learnVariants(mainLine, [...north, ...south]);
  assert.equal(variants.length, 2);
  assert.deepEqual(variants.map((x) => x.hours[0]).sort(), [17, 8]);
});

test("the same rider's trip counted twice is still one trip", () => {
  const rides = [0, 1, 2].map(() => ({ traceId: "same", hour: 8, path: ride(1000, 2000, 400) }));
  assert.deepEqual(v.learnVariants(mainLine, rides), []);
});

test("resampling spaces points evenly", () => {
  const points = v.resample(ride(0, 0, 0), 100);
  for (let i = 1; i < points.length - 1; i++) {
    assert.ok(Math.abs(v.distanceM(points[i - 1], points[i]) - 100) < 1);
  }
});

test("Valhalla shapes decode at six decimal places", () => {
  // "_izlhA~rlgdF_{geC~ywl@_kwzCn`{nI" is Google's polyline example, re-encoded at precision 6.
  const points = v.decodePolyline6("_izlhA~rlgdF_{geC~ywl@_kwzCn`{nI");
  assert.deepEqual(points, [
    { lat: 38.5, lng: -120.2 },
    { lat: 40.7, lng: -120.95 },
    { lat: 43.252, lng: -126.453 },
  ]);
});
