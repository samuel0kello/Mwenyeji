const test = require("node:test");
const assert = require("node:assert");
const { buildShapeDocs } = require("./shapes");

const point = (shape_id, seq, lat, lng) => ({
  shape_id,
  shape_pt_sequence: String(seq),
  shape_pt_lat: String(lat),
  shape_pt_lon: String(lng),
});

test("each direction gets its own trip's shape, in sequence", () => {
  const routes = [{ route_id: "r1" }];
  const trips = [
    { route_id: "r1", trip_id: "t0", direction_id: "0", shape_id: "a" },
    { route_id: "r1", trip_id: "t1", direction_id: "1", shape_id: "b" },
  ];
  const points = [point("a", 2, -1.2, 36.81), point("a", 1, -1.2, 36.8), point("b", 1, -1.2002, 36.81), point("b", 2, -1.2002, 36.8)];

  const [doc] = buildShapeDocs(routes, trips, points);

  assert.deepEqual(doc.data.outbound, [-1.2, 36.8, -1.2, 36.81]);
  assert.deepEqual(doc.data.inbound, [-1.2002, 36.81, -1.2002, 36.8]);
});

test("a route with one direction leaves the other empty, and without shapes is skipped", () => {
  const routes = [{ route_id: "r1" }, { route_id: "r2" }];
  const trips = [
    { route_id: "r1", trip_id: "t1", direction_id: "1", shape_id: "b" },
    { route_id: "r2", trip_id: "t2", direction_id: "0", shape_id: "missing" },
  ];
  const points = [point("b", 1, -1.2, 36.8), point("b", 2, -1.2, 36.81)];

  const docs = buildShapeDocs(routes, trips, points);

  assert.equal(docs.length, 1);
  assert.deepEqual(docs[0].data.outbound, [-1.2, 36.8, -1.2, 36.81]);
  assert.deepEqual(docs[0].data.inbound, []);
});
