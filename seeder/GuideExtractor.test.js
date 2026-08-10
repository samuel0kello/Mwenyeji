const test = require('node:test');
const assert = require('node:assert/strict');
const { isContributorRouteGuideCandidate } = require('./GuideExtractor');

test('treats contributor-backed routes as guide candidates', () => {
  const routeData = {
    contributorId: 'user-123',
    fareKsh: 30,
    bestTimeOfDay: 'MIDDAY',
    steps: [{ instruction: 'Board at CBD' }],
    tags: ['RELIABLE']
  };

  assert.equal(isContributorRouteGuideCandidate(routeData), true);
});

test('ignores system-seeded routes', () => {
  const routeData = {
    contributorId: 'gtfs_seed',
    fareKsh: 30,
    bestTimeOfDay: 'MIDDAY'
  };

  assert.equal(isContributorRouteGuideCandidate(routeData), false);
});
